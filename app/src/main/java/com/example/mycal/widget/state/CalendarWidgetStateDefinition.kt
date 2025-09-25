package com.example.mycal.widget.state

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import androidx.datastore.dataStoreFile
import androidx.glance.state.GlanceStateDefinition
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.io.OutputStream

object CalendarWidgetStateDefinition : GlanceStateDefinition<CalendarWidgetState> {

    private const val DATA_STORE_FILENAME = "calendar_widget_state"
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val Context.datastore by dataStore(
        DATA_STORE_FILENAME,
        CalendarWidgetStateSerializer
    )

    override suspend fun getDataStore(
        context: Context,
        fileKey: String
    ): DataStore<CalendarWidgetState> {
        return context.datastore
    }

    override fun getLocation(
        context: Context,
        fileKey: String
    ): File {
        return context.dataStoreFile(DATA_STORE_FILENAME)
    }

    object CalendarWidgetStateSerializer : Serializer<CalendarWidgetState> {
        override val defaultValue: CalendarWidgetState
            get() = CalendarWidgetState.default()

        override suspend fun readFrom(input: InputStream): CalendarWidgetState {
            return try {
                json.decodeFromString(
                    CalendarWidgetState.serializer(),
                    input.readBytes().decodeToString()
                )
            } catch (e: SerializationException) {
                throw CorruptionException("Could not read widget state", e)
            }
        }

        override suspend fun writeTo(t: CalendarWidgetState, output: OutputStream) {
            output.write(
                json.encodeToString(
                    CalendarWidgetState.serializer(),
                    t
                ).toByteArray()
            )
        }
    }
}