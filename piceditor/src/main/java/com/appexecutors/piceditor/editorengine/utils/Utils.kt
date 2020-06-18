package com.appexecutors.piceditor.editorengine.utils

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat


object Utils {

    fun tintDrawable(context: Context, @DrawableRes drawableRes: Int, colorCode: Int): Drawable? {
        val drawable = ContextCompat.getDrawable(context, drawableRes)
        if (drawable != null) {
            drawable.mutate()
            DrawableCompat.setTint(drawable, colorCode)
        }
        return drawable
    }

}