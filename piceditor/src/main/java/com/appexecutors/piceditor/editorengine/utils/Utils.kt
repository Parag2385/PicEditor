package com.appexecutors.piceditor.editorengine.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Environment
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import java.io.File
import java.io.FileOutputStream


object Utils {

    fun tintDrawable(context: Context, @DrawableRes drawableRes: Int, colorCode: Int): Drawable? {
        val drawable = ContextCompat.getDrawable(context, drawableRes)
        if (drawable != null) {
            drawable.mutate()
            DrawableCompat.setTint(drawable, colorCode)
        }
        return drawable
    }

    fun saveImage(finalBitmap: Bitmap, context: Context): String? {
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
        return file.absolutePath
    }
}