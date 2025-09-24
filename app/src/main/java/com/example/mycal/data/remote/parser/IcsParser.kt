package com.example.mycal.data.remote.parser

import com.example.mycal.data.local.entity.EventEntity
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.Component
import net.fortuna.ical4j.model.Property
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.*
import org.threeten.bp.*
import java.io.InputStream
import java.util.*
import javax.inject.Inject

class IcsParser @Inject constructor() {

    fun parseIcsStream(inputStream: InputStream, sourceId: String, sourceColor: Int): List<EventEntity> {
        val events = mutableListOf<EventEntity>()

        try {
            val builder = CalendarBuilder()
            val calendar: Calendar = builder.build(inputStream)

            val vEvents = calendar.getComponents<VEvent>(Component.VEVENT)

            for (vEvent in vEvents) {
                val event = parseVEvent(vEvent, sourceId, sourceColor)
                event?.let { events.add(it) }
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

            val startDate = parseDateTime(vEvent.startDate)
            val endDate = parseDateTime(vEvent.endDate)

            if (startDate == null || endDate == null) {
                return null
            }

            val isAllDay = vEvent.startDate?.date is net.fortuna.ical4j.model.Date &&
                    !(vEvent.startDate?.date is net.fortuna.ical4j.model.DateTime)

            val rrule = vEvent.getProperty<RRule>(Property.RRULE)?.value

            return EventEntity(
                id = "${sourceId}_${uid}",
                title = summary,
                description = description,
                startTime = startDate.toInstant(ZoneOffset.UTC).toEpochMilli(),
                endTime = endDate.toInstant(ZoneOffset.UTC).toEpochMilli(),
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

    private fun parseDateTime(dateProperty: DtStart?): LocalDateTime? {
        if (dateProperty == null) return null

        return try {
            val date = dateProperty.date
            val instant = Instant.ofEpochMilli(date.time)

            val timeZone = dateProperty.timeZone?.let {
                try {
                    ZoneId.of(it.id)
                } catch (e: Exception) {
                    ZoneId.systemDefault()
                }
            } ?: ZoneId.systemDefault()

            LocalDateTime.ofInstant(instant, timeZone)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseDateTime(dateProperty: DtEnd?): LocalDateTime? {
        if (dateProperty == null) return null

        return try {
            val date = dateProperty.date
            val instant = Instant.ofEpochMilli(date.time)

            val timeZone = dateProperty.timeZone?.let {
                try {
                    ZoneId.of(it.id)
                } catch (e: Exception) {
                    ZoneId.systemDefault()
                }
            } ?: ZoneId.systemDefault()

            LocalDateTime.ofInstant(instant, timeZone)
        } catch (e: Exception) {
            null
        }
    }
}

class IcsParseException(message: String, cause: Throwable? = null) : Exception(message, cause)