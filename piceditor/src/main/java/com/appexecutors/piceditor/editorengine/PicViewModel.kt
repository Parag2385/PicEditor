package com.appexecutors.piceditor.editorengine

import androidx.lifecycle.ViewModel
import com.appexecutors.piceditor.EditOptions
import com.appexecutors.piceditor.editorengine.models.MediaFinal
import com.appexecutors.piceditor.editorengine.models.MediaPreview

class PicViewModel : ViewModel() {

    var mMediaPreviewList: ArrayList<MediaPreview>? = null
    var mMediaFinalList: ArrayList<MediaFinal>? = null

    var mCurrentMediaPosition: Int = 0

    var mEditOptions: EditOptions? = null
}