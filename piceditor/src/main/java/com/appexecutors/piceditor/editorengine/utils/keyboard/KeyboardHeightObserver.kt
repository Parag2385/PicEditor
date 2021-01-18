package com.appexecutors.piceditor.editorengine.utils.keyboard

interface KeyboardHeightObserver {
    fun onKeyboardHeightChanged(height: Int, orientation: Int)
}