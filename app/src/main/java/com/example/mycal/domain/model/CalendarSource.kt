package com.example.mycal.domain.model

data class CalendarSource(
    val id: String,
    val url: String,
    val name: String,
    val color: Int,
    val syncEnabled: Boolean,
    val lastSyncTime: Long?
)