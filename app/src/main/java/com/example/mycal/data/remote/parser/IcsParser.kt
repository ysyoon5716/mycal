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
                    Log.d(TAG, "Added event: ${it.title} at ${it.startTime}")
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

            val startDateTime = parseDateTime(vEvent.startDate as? DateProperty)
            val endDateTime = vEvent.endDate?.let { parseDateTime(it as? DateProperty) } ?: startDateTime?.plusHours(1)

            if (startDateTime == null || endDateTime == null) {
                Log.w(TAG, "Skipping event with invalid dates: $summary")
                return null
            }

            val isAllDay = vEvent.startDate?.date is net.fortuna.ical4j.model.Date &&
                    !(vEvent.startDate?.date is net.fortuna.ical4j.model.DateTime)

            val rrule = vEvent.getProperty<RRule>(Property.RRULE)?.value

            // Convert to epoch millis preserving the original time zone
            val startMillis = startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endMillis = endDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            Log.d(TAG, "Event: $summary, Start: $startDateTime ($startMillis), End: $endDateTime ($endMillis)")

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
            return null
        }
    }

    private fun parseDateTime(dateProperty: DateProperty?): LocalDateTime? {
        if (dateProperty == null) return null

        return try {
            val date = dateProperty.date
            val instant = Instant.ofEpochMilli(date.time)

            // Get the time zone from the property, or use system default
            val timeZone = when {
                dateProperty.timeZone != null -> {
                    try {
                        ZoneId.of(dateProperty.timeZone.id)
                    } catch (e: Exception) {
                        Log.w(TAG, "Invalid timezone: ${dateProperty.timeZone.id}, using system default")
                        ZoneId.systemDefault()
                    }
                }
                dateProperty.isUtc -> ZoneOffset.UTC
                else -> ZoneId.systemDefault()
            }

            val result = LocalDateTime.ofInstant(instant, timeZone)
            Log.d(TAG, "Parsed date: ${date.time} -> $result (TZ: $timeZone)")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing date", e)
            null
        }
    }
}

class IcsParseException(message: String, cause: Throwable? = null) : Exception(message, cause)