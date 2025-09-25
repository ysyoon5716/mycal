package com.example.mycal.widget.model

import kotlinx.serialization.Serializable

@Serializable
data class WidgetEvent(
    val id: String,
    val title: String,
    val time: String? = null,
    val color: Int = 0xFF1976D2.toInt(),
    val isAllDay: Boolean = false
) {
    fun getTruncatedTitle(maxLength: Int = 10): String {
        return if (title.length > maxLength) {
            title.take(maxLength - 1) + "…"
        } else {
            title
        }
    }

    fun getDisplayTime(): String? {
        return if (!isAllDay && time != null) {
            time
        } else {
            null
        }
    }
}