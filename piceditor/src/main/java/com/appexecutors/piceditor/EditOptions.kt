package com.appexecutors.piceditor

import com.appexecutors.piceditor.editorengine.AddMoreImagesListener
import com.appexecutors.piceditor.editorengine.models.MediaFinal
import com.appexecutors.piceditor.editorengine.utils.WatermarkType
import java.io.Serializable

class EditOptions: Serializable {

    var mSelectedImageList = ArrayList<MediaFinal>()
    var mRequestCode = 0
    var mAddMoreImplementedListener: AddMoreImagesListener? = null

    var showDeleteOption: Boolean = true
    var showCropOption: Boolean = true
    var showTextOption: Boolean = true
    var showDrawOption: Boolean = true
    var showCaption: Boolean = false
    var isCaptionCompulsory: Boolean = false
    var showThumbnail: Boolean = true
    var mWatermarkType: WatermarkType = WatermarkType.NONE

    //if WatermarkType.TEXT
    var mWatermarkText: String = ""

    companion object{
        @JvmStatic
        fun init(): EditOptions{
            return EditOptions()
        }
    }

}