package com.appexecutors.piceditor.editorengine

import androidx.lifecycle.ViewModel
import com.appexecutors.piceditor.editorengine.models.MediaPreview

class PicViewModel : ViewModel() {

    var mMediaPreviewList: ArrayList<MediaPreview>? = null

    var mCurrentMediaPosition: Int = 0
}