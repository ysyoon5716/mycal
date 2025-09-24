package com.example.mycal.data.remote.parser

import com.example.mycal.data.local.entity.EventEntity
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.Component
import net.fortuna.ical4j.model.Property
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.*
import net.fortuna.ical4j.model.property.DateProperty
import android.util.Log
import org.threeten.bp.*
import java.io.InputStream
import java.util.*
import javax.inject.Inject

class IcsParser @Inject constructor() {

    companion object {
        private const val TAG = "IcsParser"
    }

    fun parseIcsStream(inputStream: InputStream, sourceId: String, sourceColor: Int): List<EventEntity> {
        val events = mutableListOf<EventEntity>()

        try {
            val builder = CalendarBuilder()
            val calendar: Calendar = builder.build(inputStream)

            val vEvents = calendar.getComponents<VEvent>(Component.VEVENT)

            Log.d(TAG, "Parsing ${vEvents.size} events from ICS for source: $sourceId")
            for (vEvent in vEvents) {
                val event = parseVEvent(vEvent, sourceId, sourceColor)
                event?.let {
                    events.add(it)
                    val startDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(it.startTime), ZoneId.systemDefault())
                    val endDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(it.endTime), ZoneId.systemDefault())
                    Log.d(TAG, "Added event: ${it.title}, isAllDay: ${it.isAllDay}, Start: $startDate, End: $endDate")
                }
            }
        } catch (e: Exception) {
            throw IcsParseException("Failed to parse ICS file: ${e.message}", e)
        }

        return events
    }

    private fun parseVEvent(vEvent: VEvent, sourceId: String, sourceColor: Int): EventEntity? {
        try {
            val uid = vEvent.uid?.value ?: return null
            val summary = vEvent.summary?.value ?: "No Title"
            val description = vEvent.description?.value
            val location = vEvent.location?.value

            // Check if this is an all-day event
            val isAllDay = vEvent.startDate?.date is net.fortuna.ical4j.model.Date &&
                    !(vEvent.startDate?.date is net.fortuna.ical4j.model.DateTime)

            Log.d(TAG, "Processing event: $summary, isAllDay: $isAllDay")

            // Parse dates with all-day event handling
            val startDateTime = if (isAllDay) {
                parseAllDayDate(vEvent.startDate as? DateProperty)
            } else {
                parseDateTime(vEvent.startDate as? DateProperty)
            }

            val endDateTime = if (isAllDay) {
                // For all-day events, handle end date properly
                if (vEvent.endDate != null) {
                    val parsedEndDate = parseAllDayDate(vEvent.endDate as? DateProperty)
                    // ICS standard: end date is exclusive for all-day events
                    // So we need to subtract one day and set to end of day
                    parsedEndDate?.minusDays(1)?.withHour(23)?.withMinute(59)?.withSecond(59)
                } else {
                    // If no end date, it's a single day event - set to end of same day
                    startDateTime?.withHour(23)?.withMinute(59)?.withSecond(59)
                }
            } else {
                vEvent.endDate?.let { parseDateTime(it as? DateProperty) } ?: startDateTime?.plusHours(1)
            }

            if (startDateTime == null || endDateTime == null) {
                Log.w(TAG, "Skipping event with invalid dates: $summary")
                return null
            }

            val rrule = vEvent.getProperty<RRule>(Property.RRULE)?.value

            // Convert to epoch millis preserving the original time zone
            val startMillis = startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endMillis = endDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            Log.d(TAG, "Event: $summary, isAllDay: $isAllDay, Start: $startDateTime ($startMillis), End: $endDateTime ($endMillis)")

            return EventEntity(
                id = "${sourceId}_${uid}",
                title = summary,
                description = description,
                startTime = startMillis,
                endTime = endMillis,
                isAllDay = isAllDay,
                location = location,
                color = sourceColor,
                sourceId = sourceId,
                recurrenceRule = rrule
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing event", e)
            return null
        }
    }

    private fun parseAllDayDate(dateProperty: DateProperty?): LocalDateTime? {
        if (dateProperty == null) return null

        return try {
            val date = dateProperty.date

            // For all-day events, parse as local date without timezone conversion
            // All-day events should be treated as occurring on the same calendar day regardless of timezone
            val instant = Instant.ofEpochMilli(date.time)
            val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()

            // Set to start of day (00:00:00)
            val result = localDate.atStartOfDay()

            Log.d(TAG, "Parsed all-day date: ${date.time} -> $result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing all-day date", e)
            null
        }
    }

    private fun parseDateTime(dateProperty: DateProperty?): LocalDateTime? {
        if (dateProperty == null) return null

        return try {
            val date = dateProperty.date
            val instant = Instant.ofEpochMilli(date.time)

            // ICS 파일의 시간대 정보 확인
            val sourceTimeZone = when {
                dateProperty.timeZone != null -> {
                    try {
                        ZoneId.of(dateProperty.timeZone.id)
                    } catch (e: Exception) {
                        Log.w(TAG, "Invalid timezone: ${dateProperty.timeZone.id}, using UTC")
                        ZoneOffset.UTC
                    }
                }
                dateProperty.isUtc -> ZoneOffset.UTC
                else -> ZoneOffset.UTC  // 기본값을 UTC로 설정
            }

            // UTC 시간을 한국 시간대로 변환
            val utcDateTime = LocalDateTime.ofInstant(instant, sourceTimeZone)
            val koreaTimeZone = ZoneId.of("Asia/Seoul")
            val result = if (sourceTimeZone == ZoneOffset.UTC) {
                // UTC에서 한국 시간으로 변환
                utcDateTime.atZone(ZoneOffset.UTC)
                    .withZoneSameInstant(koreaTimeZone)
                    .toLocalDateTime()
            } else {
                // 이미 특정 시간대가 지정된 경우
                utcDateTime
            }

            Log.d(TAG, "Parsed date/time: ${date.time} -> UTC: $utcDateTime -> KST: $result (Source TZ: $sourceTimeZone)")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing date/time", e)
            null
        }
    }
}

class IcsParseException(message: String, cause: Throwable? = null) : Exception(message, cause)