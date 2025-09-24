package com.example.mycal.data.mapper

import com.example.mycal.data.local.entity.EventEntity
import com.example.mycal.domain.model.CalendarEvent
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId

object EventMapper {
    fun toDomain(entity: EventEntity): CalendarEvent {
        return CalendarEvent(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            startTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(entity.startTime), ZoneId.systemDefault()),
            endTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(entity.endTime), ZoneId.systemDefault()),
            isAllDay = entity.isAllDay,
            location = entity.location,
            color = entity.color,
            sourceId = entity.sourceId,
            recurrenceRule = entity.recurrenceRule
        )
    }

    fun toEntity(domain: CalendarEvent): EventEntity {
        return EventEntity(
            id = domain.id,
            title = domain.title,
            description = domain.description,
            startTime = domain.startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            endTime = domain.endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            isAllDay = domain.isAllDay,
            location = domain.location,
            color = domain.color,
            sourceId = domain.sourceId,
            recurrenceRule = domain.recurrenceRule
        )
    }
}