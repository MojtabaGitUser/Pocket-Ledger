package com.mojtaba.folentra.core.data.receipt

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MlKitReceiptTextRecognizer(context: Context) : ReceiptTextRecognizer {
    private val context = context.applicationContext

    override suspend fun recognize(imageUri: String): ReceiptText = withContext(Dispatchers.IO) {
        val uri = Uri.parse(imageUri)
        require(uri.scheme == "content") { "Unsupported image URI" }
        val directory = File(context.cacheDir, "receipts").apply { mkdirs() }
        val file = File.createTempFile("ocr-", ".image", directory)
        var bitmap: Bitmap? = null
        try {
            requireNotNull(context.contentResolver.openInputStream(uri)).use { input ->
                file.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var total = 0L
                    while (true) {
                        ensureActive()
                        val count = input.read(buffer)
                        if (count < 0) break
                        total += count
                        require(total <= 20L * 1024 * 1024) { "Image exceeds size limit" }
                        output.write(buffer, 0, count)
                    }
                }
            }
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.path, bounds)
            require(bounds.outWidth > 0 && bounds.outHeight > 0)
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 2400) sample *= 2
            bitmap = requireNotNull(BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply {
                inSampleSize = sample
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }))
            val orientation = try { ExifInterface(file.path).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1) }
                catch (_: java.io.IOException) { 1 }
            val matrix = Matrix().apply {
                when (orientation) {
                    2 -> setScale(-1f, 1f)
                    3 -> setRotate(180f)
                    4 -> { setRotate(180f); postScale(-1f, 1f) }
                    5 -> { setRotate(90f); postScale(-1f, 1f) }
                    6 -> setRotate(90f)
                    7 -> { setRotate(-90f); postScale(-1f, 1f) }
                    8 -> setRotate(-90f)
                }
            }
            if (!matrix.isIdentity) {
                val oriented = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                if (oriented !== bitmap) bitmap.recycle()
                bitmap = oriented
            }
            ensureActive()
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            try {
                // Wait for ML Kit ownership to finish before recycling bitmap, including on cancellation.
                val result = suspendCoroutine { continuation ->
                    recognizer.process(InputImage.fromBitmap(bitmap, 0))
                        .addOnSuccessListener { continuation.resume(it) }
                        .addOnFailureListener { continuation.resumeWithException(it) }
                        .addOnCanceledListener { continuation.resumeWithException(kotlinx.coroutines.CancellationException()) }
                }
                ensureActive()
                ReceiptText(result.textBlocks.flatMapIndexed { index, block -> block.lines.map { line ->
                    ReceiptLine(line.text, line.boundingBox?.let { ReceiptBounds(it.left, it.top, it.right, it.bottom) }, index)
                } })
            } finally { recognizer.close() }
        } finally {
            bitmap?.recycle()
            file.delete()
        }
    }
}
