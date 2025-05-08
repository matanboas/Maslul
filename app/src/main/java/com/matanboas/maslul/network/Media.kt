package com.matanboas.maslul.network

import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigInteger

class Media(private val cacheDir: File, val cause: Throwable? = null) {

    init {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }

    private fun String.toMD5(): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
    }

    suspend fun getImage(url: String): String {
        val cleanUrl = url.substringBefore('?')
        val fileName = cleanUrl.toMD5()
        val cachedFile = File(cacheDir, fileName)

        if (cachedFile.exists() && cachedFile.length() > 0) {
            return cachedFile.absolutePath
        }

        return withContext(Dispatchers.IO) {
            try {
                val connection = URL(url).openConnection()
                connection.connect()

                connection.getInputStream().use { inputStream ->
                    FileOutputStream(cachedFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                cachedFile.absolutePath
            } catch (e: Exception) {
                if (cachedFile.exists()) {
                    cachedFile.delete()
                }
                throw e
            }
        }
    }
}
