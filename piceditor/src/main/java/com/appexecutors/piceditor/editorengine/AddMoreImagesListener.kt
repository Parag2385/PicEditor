package com.appexecutors.piceditor.editorengine

import androidx.appcompat.app.AppCompatActivity
import java.io.Serializable

interface AddMoreImagesListener: Serializable {
    fun addMoreImages(context: AppCompatActivity, selectedImageList: ArrayList<String>, requestCodePix: Int)
}