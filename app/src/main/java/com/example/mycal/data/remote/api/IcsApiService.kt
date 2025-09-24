package com.example.mycal.data.remote.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Streaming
import retrofit2.http.Url

interface IcsApiService {

    @Streaming
    @GET
    suspend fun downloadIcsFile(
        @Url url: String,
        @Header("If-None-Match") etag: String? = null
    ): Response<ResponseBody>
}