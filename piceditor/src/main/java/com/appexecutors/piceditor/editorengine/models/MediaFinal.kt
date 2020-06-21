package com.appexecutors.piceditor.editorengine.models

import java.io.Serializable

data class MediaFinal(
    var mMediaUri: String
): Serializable{
    var mCaption: String = ""
}