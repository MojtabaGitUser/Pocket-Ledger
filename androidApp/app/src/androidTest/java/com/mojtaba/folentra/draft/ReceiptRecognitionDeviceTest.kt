package com.mojtaba.folentra.draft

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.media.ExifInterface
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.mojtaba.folentra.core.data.receipt.MlKitReceiptTextRecognizer
import com.mojtaba.folentra.core.data.receipt.ReceiptImages
import com.mojtaba.folentra.core.data.receipt.ReceiptParser
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ReceiptRecognitionDeviceTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test fun exifRotatedImageIsReadUpright() = runBlocking {
        val images = ReceiptImages(context)
        val uri = Uri.parse(images.createCameraUri())
        val upright = Bitmap.createBitmap(1200, 600, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(upright)
        canvas.drawColor(Color.WHITE)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK; textSize = 64f }
        canvas.drawText("MAPLE MARKET", 80f, 130f, paint)
        canvas.drawText("TOTAL CAD 42.10", 80f, 290f, paint)
        val rotated = Bitmap.createBitmap(upright, 0, 0, upright.width, upright.height, Matrix().apply { setRotate(-90f) }, true)
        try {
            context.contentResolver.openOutputStream(uri)!!.use { rotated.compress(Bitmap.CompressFormat.JPEG, 95, it) }
            context.contentResolver.openFileDescriptor(uri, "rw")!!.use {
                ExifInterface(it.fileDescriptor).apply {
                    setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_ROTATE_90.toString())
                    saveAttributes()
                }
            }
            val text = MlKitReceiptTextRecognizer(context).recognize(uri.toString())
            assertTrue(text.lines.any { it.bounds != null })
            assertEquals(4210L, ReceiptParser().parse(text).amountMinor.value)
        } finally {
            upright.recycle(); rotated.recycle(); images.deleteCameraUri(uri.toString())
        }
    }

    @Test fun unreadableImageFailsWithoutCreatingATransaction() = runBlocking {
        val images = ReceiptImages(context)
        val uri = images.createCameraUri()
        try {
            context.contentResolver.openOutputStream(Uri.parse(uri))!!.use { it.write("not an image".toByteArray()) }
            var rejected = false
            try { MlKitReceiptTextRecognizer(context).recognize(uri) }
            catch (_: IllegalArgumentException) { rejected = true }
            assertTrue(rejected)
        } finally { images.deleteCameraUri(uri) }
    }
}
