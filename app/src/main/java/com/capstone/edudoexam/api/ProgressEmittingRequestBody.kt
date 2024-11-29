package com.capstone.edudoexam.api

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream

class ProgressEmittingRequestBody(
    private val contentType: String,
    private val file: File,
    private val listener: ProgressListener
): RequestBody() {

    override fun contentType() = contentType.toMediaTypeOrNull()

    override fun contentLength() = file.length()

    override fun writeTo(sink: BufferedSink) {
        val totalBytes = contentLength()
        var bytesWritten = 0L

        file.inputStream().use { inputStream ->
            val buffer = ByteArray(2048)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                sink.write(buffer, 0, read)
                bytesWritten += read
                listener.onProgressUpdate((100 * bytesWritten / totalBytes).toInt())
            }
        }
    }

    interface ProgressListener {
        fun onProgressUpdate(percentage: Int)
    }
}