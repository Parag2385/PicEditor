package com.appexecutors.piceditor.editorengine.utils

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


object Utils {

    fun tintDrawable(context: Context, @DrawableRes drawableRes: Int, colorCode: Int): Drawable? {
        val drawable = ContextCompat.getDrawable(context, drawableRes)
        if (drawable != null) {
            drawable.mutate()
            DrawableCompat.setTint(drawable, colorCode)
        }
        return drawable
    }

    suspend fun saveImage(finalBitmap: Bitmap, context: Context): String? = suspendCoroutine  {
        val myDir =
            File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + File.separator + "Edited")

        if (!myDir.exists()) myDir.mkdirs()

        val mName = "Image_${System.currentTimeMillis()}.jpg"
        val file = File(myDir, mName)
        if (file.exists()) file.delete()
        try {
            val out = FileOutputStream(file)
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        it.resume(file.absolutePath)
    }

    fun getStringDate(mFinalFormat: String, mDate: Date?): String {
        if (mDate == null) return "NA"
        return SimpleDateFormat(mFinalFormat, Locale.ENGLISH).format(mDate)
    }

    fun waterMark(
        src: Bitmap,
        watermark: String?,
        color: Int,
        alpha: Int,
        size: Float
    ): Bitmap? {
        //get source image width and height
        val w = src.width
        val h = src.height
        val result = Bitmap.createBitmap(w, h, src.config)
        //create canvas object
        val canvas = Canvas(result)
        //draw bitmap on canvas
        canvas.drawBitmap(src, 0f, 0f, null)
        //create paint object
        val paint = Paint()
        //apply color
        paint.color = color

        //set transparency
        paint.alpha = alpha
        //set text size
        paint.textSize = size
        paint.isAntiAlias = true

       /* val stkPaint = Paint()
        stkPaint.style = Paint.Style.STROKE
        stkPaint.strokeWidth = 1f
        stkPaint.textSize = size
        stkPaint.alpha = alpha
        stkPaint.color = Color.BLACK
        stkPaint.isAntiAlias = true*/
        //set should be underlined or not

        //draw text on given location
        canvas.save()
        var width: Float = paint.measureText(watermark)
        if (width > w) {
            var i = 2
            while (width > w/3) {
                paint.textSize = size - i
                /*stkPaint.textSize = size - i*/
                width = paint.measureText(watermark)
                i++
            }
        } else {
            var i = 2
            while (width < w/3) {
                paint.textSize = size + i
                /*stkPaint.textSize = size + i*/
                width = paint.measureText(watermark)
                i++
            }
        }

        val margin = 5f

        val xPos = 8f
        val yPos = canvas.height.toFloat() - 8f

        //background
        val fm: Paint.FontMetrics = Paint.FontMetrics()
        paint.color = Color.WHITE
        paint.getFontMetrics(fm)


        canvas.drawRect(xPos - margin, yPos + fm.top - margin, xPos + width + margin, yPos + fm.bottom+ margin, paint)

        paint.color = color
        val tf = Typeface.create("Helvetica", Typeface.BOLD)
        paint.typeface = tf
        paint.textAlign = Paint.Align.LEFT

        canvas.drawText(watermark!!, xPos, yPos, paint)
        /*canvas.drawText(watermark, xPos, yPos, stkPaint)*/
        canvas.restore()
        return result
    }


    fun getMimeType(url: String?): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }

    fun disableEnableControls(enable: Boolean, vg: ViewGroup) {
        for (i in 0 until vg.childCount) {
            val child: View = vg.getChildAt(i)
            child.isEnabled = enable
            if (child is ViewGroup) {
                disableEnableControls(enable, child)
            }
        }
    }
}