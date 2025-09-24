package com.example.mycal.data.remote.datasource

import com.example.mycal.data.local.entity.EventEntity
import com.example.mycal.data.remote.api.IcsApiService
import com.example.mycal.data.remote.parser.IcsParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class IcsRemoteDataSource @Inject constructor(
    private val icsApiService: IcsApiService,
    private val icsParser: IcsParser
) {

    suspend fun fetchAndParseIcs(
        url: String,
        sourceId: String,
        sourceColor: Int,
        etag: String? = null
    ): IcsResult = withContext(Dispatchers.IO) {
        try {
            val response = icsApiService.downloadIcsFile(url, etag)

            when {
                response.code() == 304 -> {
                    IcsResult.NotModified
                }
                response.isSuccessful -> {
                    val responseBody = response.body()
                        ?: return@withContext IcsResult.Error("Empty response body")

                    val newEtag = response.headers()["ETag"]

                    responseBody.byteStream().use { inputStream ->
                        val events = icsParser.parseIcsStream(inputStream, sourceId, sourceColor)
                        IcsResult.Success(events, newEtag)
                    }
                }
                else -> {
                    IcsResult.Error("Failed to download ICS file: ${response.code()}")
                }
            }
        } catch (e: Exception) {
            IcsResult.Error("Network error: ${e.message}")
        }
    }
}

sealed class IcsResult {
    data class Success(val events: List<EventEntity>, val etag: String?) : IcsResult()
    object NotModified : IcsResult()
    data class Error(val message: String) : IcsResult()
}