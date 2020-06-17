package com.appexecutors.piceditor

import com.appexecutors.piceditor.editorengine.AddMoreImagesListener
import java.io.Serializable

class EditOptions: Serializable {

    var mSelectedImageList = ArrayList<String>()
    var mRequestCode = 0
    var mAddMoreImplementedListener: AddMoreImagesListener? = null

    var showDeleteOption: Boolean = true
    var showCropOption: Boolean = true
    var showTextOption: Boolean = true
    var showDrawOption: Boolean = true
    var showCaption: Boolean = false
    var isCaptionCompulsory: Boolean = false
    var showThumbnail: Boolean = true

    companion object{
        @JvmStatic
        fun init(): EditOptions{
            return EditOptions()
        }
    }

}