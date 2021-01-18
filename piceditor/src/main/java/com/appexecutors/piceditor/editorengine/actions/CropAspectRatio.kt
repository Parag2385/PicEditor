package com.appexecutors.piceditor.editorengine.actions

data class CropAspectRatio(
    val xCoordinate: Int,
    val yCoordinate: Int,
    val mLabel: String,
    val mIcon: Int
){
    var isSelected: Boolean = false
}