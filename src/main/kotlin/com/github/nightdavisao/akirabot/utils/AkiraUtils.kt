package com.github.nightdavisao.akirabot.utils

import com.grack.nanojson.JsonParser
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

object AkiraUtils {
    suspend fun retrieveImageFromDrive(url: String, httpClient: HttpClient): DriveImage? {
        val request = httpClient.get<HttpResponse>(url)

        if (request.status == HttpStatusCode.OK) {
            val array = JsonParser.array().from(
                request.readText()
                    .substringAfter("window.viewerData = ")
                    .substringBefore("};")
                    .substringAfter("itemJson: ")
            )
            return DriveImage(array[10] as String, array[11] as String)
        }
        return null
    }

    data class DriveImage(
        val url: String,
        val mimeType: String
    )
}