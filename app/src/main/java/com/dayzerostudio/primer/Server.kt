package com.dayzerostudio.primer

import android.content.Context
import android.widget.Toast
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.File

fun sendZipToServer(context: Context, url: String, zip: File) {
    val client = OkHttpClient()
    val body = RequestBody.create(MediaType.get("application/zip"), zip)
    val request = Request.Builder()
        .url("$url/habits-sync?name=${zip.name}")
        .post(body)
        .build()
    val req = client.newCall(request)
    req.execute().use { response ->
        response.body()!!.string()
    }
}