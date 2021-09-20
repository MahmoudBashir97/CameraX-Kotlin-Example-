package com.mahmoudbashir.cameraxapp

import android.Manifest

object Constants {

    const val TAG = "CamerX"
    const val FILE_NAME_FORMATE = "yy-MM-dd-HH-mm-ss-SSS"
    const val REQUEST_CODE_PERMISSION = 123
    val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
}