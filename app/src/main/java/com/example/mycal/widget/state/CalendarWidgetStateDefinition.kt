package com.example.mycal.widget.state

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import androidx.glance.state.GlanceStateDefinition
import kotlinx.serialization.json.Json
import kotlinx.serialization.SerializationException
import java.io.File
import java.io.InputStream
import java.io.OutputStream

object CalendarWidgetStateDefinition : GlanceStateDefinition<CalendarWidgetState> {

    private const val TAG = "WidgetStateDefinition"
    private const val DATA_STORE_FILENAME = "calendar_widget_state"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val Context.widgetDataStore by dataStore(
        fileName = DATA_STORE_FILENAME,
        serializer = CalendarWidgetStateSerializer
    )

    override suspend fun getDataStore(
        context: Context,
        fileKey: String
    ): DataStore<CalendarWidgetState> {
        return context.widgetDataStore
    }

    override fun getLocation(context: Context, fileKey: String): File {
        return File(context.applicationContext.filesDir, "datastore/$DATA_STORE_FILENAME")
    }

    object CalendarWidgetStateSerializer : Serializer<CalendarWidgetState> {
        override val defaultValue: CalendarWidgetState = CalendarWidgetState()

        override suspend fun readFrom(input: InputStream): CalendarWidgetState {
            return try {
                val bytes = input.readBytes()
                if (bytes.isEmpty()) {
                    defaultValue
                } else {
                    json.decodeFromString(
                        CalendarWidgetState.serializer(),
                        bytes.decodeToString()
                    )
                }
            } catch (e: SerializationException) {
                Log.e(TAG, "Failed to deserialize widget state", e)
                defaultValue
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error reading widget state", e)
                defaultValue
            }
        }

        override suspend fun writeTo(t: CalendarWidgetState, output: OutputStream) {
            try {
                val bytes = json.encodeToString(
                    CalendarWidgetState.serializer(),
                    t
                ).encodeToByteArray()
                output.write(bytes)
            } catch (e: SerializationException) {
                Log.e(TAG, "Failed to serialize widget state", e)
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error writing widget state", e)
            }
        }
    }
}