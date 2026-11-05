package com.mojtaba.folentra.core.data.receipt

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Camera files are scoped to one private cache directory, never inserted in MediaStore. */
class ReceiptImages(context: Context) {
    private val context = context.applicationContext
    private val directory get() = File(context.cacheDir, "receipts").apply { mkdirs() }

    suspend fun createCameraUri(): String = withContext(Dispatchers.IO) {
        prune()
        val file = File.createTempFile("capture-", ".jpg", directory)
        FileProvider.getUriForFile(context, "${context.packageName}.receipts", file).toString()
    }

    suspend fun deleteCameraUri(value: String?) = withContext(Dispatchers.IO) {
        val uri = value?.let(Uri::parse) ?: return@withContext
        if (uri.authority == "${context.packageName}.receipts") {
            val name = uri.lastPathSegment ?: return@withContext
            val file = File(directory, name)
            if (file.canonicalFile.parentFile == directory.canonicalFile) file.delete()
            context.revokeUriPermission(uri, android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION or android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    suspend fun cleanup() = withContext(Dispatchers.IO) { prune() }
    private fun prune() {
        val cutoff = System.currentTimeMillis() - 24 * 60 * 60 * 1000L
        directory.listFiles()?.filter { it.isFile && it.lastModified() < cutoff }?.forEach { it.delete() }
    }
}
