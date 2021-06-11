package com.ericktijerou.audioeffects.library

interface FFMpegCallback {
    fun onSuccess(commands: Array<String>)
    fun onCancel(commands: Array<String>)
    fun onFailed(commands: Array<String>, rc: Int, out: String?)
}
