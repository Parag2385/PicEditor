package com.appexecutors.piceditor.editorengine

import androidx.appcompat.app.AppCompatActivity
import com.appexecutors.piceditor.editorengine.models.MediaFinal
import java.io.Serializable

interface AddMoreImagesListener: Serializable {
    fun addMoreImages(context: AppCompatActivity, selectedImageList: ArrayList<MediaFinal>, requestCodePix: Int)
}