package com.appexecutors.piceditor.editorengine.models

import android.graphics.Bitmap

data class MediaPreview(
    var mMediaUri: String
){
    var mCaption: String = ""

    var isSelected: Boolean = false

    var mProcessedBitmap: Bitmap? = null

    var mOldMediaUri: String = ""
}