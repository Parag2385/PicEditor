package com.appexecutors.piceditor.editorengine.utils

object AppConstants {

    const val EDITOR_OPTIONS = "EDITOR_OPTIONS"

    const val MEDIA_POSITION = "MEDIA_POSITION"

    const val ADD_TEXT_ACTION = "ADD_TEXT_ACTION"
    const val EDIT_TEXT_ACTION_START = "EDIT_TEXT_ACTION_START"
    const val EDIT_TEXT_ACTION_DONE = "EDIT_TEXT_ACTION_DONE"
    const val ADD_BRUSH_ACTION = "ADD_BRUSH_ACTION"
    const val DISABLE_BRUSH_ACTION = "DISABLE_BRUSH_ACTION"
    const val UNDO_REDO_ACTION = "UNDO_REDO_ACTION"

    const val SAVE_BITMAP_FOR_CROP_ACTION_DONE = "SAVE_BITMAP_FOR_CROP_ACTION_DONE"

    const val ACTION_STARTED = "ACTION_STARTED"
    const val ACTION_STOPPED = "ACTION_STOPPED"

    const val INTENT_FROM_PIC_EDITOR = 1
    const val INTENT_FROM_PIC_EDITOR_FRAGMENT = 2

    const val UNDO = 1

    const val EDITED_MEDIA_LIST = "EDITED_MEDIA_LIST"

    const val PIC_IMAGE_EDITOR_CODE = 101

    const val IMAGE = 1
    const val VIDEO = 2


    //----------------------------------------------------
    //              VideoPlayerConfig
    //----------------------------------------------------

    //Minimum Video you want to buffer while Playing
    const val MIN_BUFFER_DURATION = 3000
    //Max Video you want to buffer during PlayBack
    const val MAX_BUFFER_DURATION = 5000
    //Min Video you want to buffer before start Playing it
    const val MIN_PLAYBACK_START_BUFFER = 1500
    //Min video You want to buffer when user resumes video
    const val MIN_PLAYBACK_RESUME_BUFFER = 5000

}