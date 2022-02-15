package com.dayzerostudio.primer

import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.File

fun sendZipToServer(zip: File) {
    val client = OkHttpClient()
    val body = RequestBody.create(MediaType.get("application/zip"), zip)
    val request = Request.Builder()
        .url("http://192.168.1.42:7777/habits-sync?name=${zip.name}")
        .post(body)
        .build()
        client.newCall(request).execute().use { response ->
            response.body()!!.string()
        }
}