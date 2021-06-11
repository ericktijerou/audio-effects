package com.ericktijerou.audioeffects.library

import com.arthenica.ffmpegkit.FFmpegKit
import java.io.File

class FFMpegMediaConverter constructor(callBack: FFMpegCallback) {
    private val mFFMepgCallback: FFMpegCallback = callBack

    fun execute(option: Options) {
        this.execute(option.get())
    }

    fun execute(commands: Array<String>) {
        val rc = FFmpegKit.execute(commands)
        when {
            rc.returnCode.isSuccess -> {
                mFFMepgCallback.onSuccess(commands)
            }
            rc.returnCode.isCancel -> {
                mFFMepgCallback.onCancel(commands)
            }
            else -> {
                mFFMepgCallback.onFailed(commands, rc.returnCode.value, "")
            }
        }
    }

    fun mergeFiles(
        voiceFile: String,
        effect: String,
        outFile: String
    ) {
        val file = File(outFile)
        if (file.exists()) {
            file.delete()
        }
        val commands =
            "-i $voiceFile -i $effect -filter_complex amix=inputs=2:duration=first:dropout_transition=2 $outFile"
        val cmd1: Array<String> = commands.split(" ").toTypedArray()
        val rc = FFmpegKit.execute(cmd1)
        when {
            rc.returnCode.isSuccess -> {
                mFFMepgCallback.onSuccess(cmd1)
            }
            rc.returnCode.isCancel -> {
                mFFMepgCallback.onCancel(cmd1)
            }
            else -> {
                mFFMepgCallback.onFailed(cmd1, rc.returnCode.value, "")
            }
        }
    }

    fun mergeFiles(
        source1: String,
        effect1: String,
        startOffset: Int,
        effect2: String,
        startOffset2: Int,
        destination: String
    ) {
        val file = File(destination)
        if (file.exists()) {
            file.delete()
        }
        val commands: String =
            ("-i " + source1 + " -i " + effect1 + " -i " + effect2 +
                    " -filter_complex [1]adelay=" + startOffset + "|" + startOffset + "[s1];" +
                    "[2]adelay=" + startOffset2 + "|" + startOffset2 + "[s2];" +
                    "[0][s1][s2]amix=3[mixout]" +
                    " -map [mixout] -c:v copy " + destination)
        val cmd1: Array<String> =
            commands.split(" ").toTypedArray()
        val rc = FFmpegKit.execute(cmd1)
        if (rc.returnCode.isSuccess) {
            mFFMepgCallback.onSuccess(cmd1)
        } else if (rc.returnCode.isCancel) {
            mFFMepgCallback.onCancel(cmd1)
        } else {
            mFFMepgCallback.onFailed(cmd1, rc.returnCode.value, "")
        }
    }

    fun mergeFiles(
        sourceRecording: AudioFile,
        destination: String,
        vararg effects: AudioFile
    ) {
        val file = File(destination)
        if (file.exists()) {
            file.delete()
        }
        val commands: String =
            ("-i " + sourceRecording.filePath + getFileNamesMerged(*effects) +
                    " -filter_complex " + getDelayParamsMerged(*effects) +
                    getMixParamsMerged(*effects) +
                    " -map [mixout] -c:v copy -c:a aac -b:a 192k " + destination)
        val cmd1: Array<String> = commands.split(" ").toTypedArray()
        val rc = FFmpegKit.execute(cmd1)
        when {
            rc.returnCode.isSuccess -> {
                mFFMepgCallback.onSuccess(cmd1)
            }
            rc.returnCode.isCancel -> {
                mFFMepgCallback.onCancel(cmd1)
            }
            else -> {
                mFFMepgCallback.onFailed(cmd1, rc.returnCode.value, "")
            }
        }
    }

    fun trimFile(
        sourceFilePath: String,
        outFilePath: String,
        startOffset: Int,
        endOffset: Int
    ) {
        val file = File(outFilePath)
        if (file.exists()) {
            file.delete()
        }
        val commands = "-i $sourceFilePath -ss $startOffset -to $endOffset -c copy $outFilePath"
        val cmd1: Array<String> = commands.split(" ").toTypedArray()
        val rc = FFmpegKit.execute(cmd1)
        when {
            rc.returnCode.isSuccess -> {
                mFFMepgCallback.onSuccess(cmd1)
            }
            rc.returnCode.isCancel -> {
                mFFMepgCallback.onCancel(cmd1)
            }
            else -> {
                mFFMepgCallback.onFailed(cmd1, rc.returnCode.value, "")
            }
        }
    }

    fun convertToAccFile(
        sourceFilePath: String,
        outFilePath: String
    ) {
        val file = File(outFilePath)
        if (file.exists()) {
            file.delete()
        }
        val commands = "-i $sourceFilePath -c:a aac -b:a 192k $outFilePath"
        val cmd1: Array<String> = commands.split(" ").toTypedArray()
        val rc = FFmpegKit.execute(cmd1)
        when {
            rc.returnCode.isSuccess -> {
                mFFMepgCallback.onSuccess(cmd1)
            }
            rc.returnCode.isCancel -> {
                mFFMepgCallback.onCancel(cmd1)
            }
            else -> {
                mFFMepgCallback.onFailed(cmd1, rc.returnCode.value, "")
            }
        }
    }

    private fun getFileNamesMerged(vararg effects: AudioFile): String {
        val stringBuilder = StringBuilder()
        for (file: AudioFile? in effects) {
            stringBuilder.append(" -i ")
            stringBuilder.append(file!!.filePath)
        }
        return stringBuilder.toString()
    }

    private fun getDelayParamsMerged(vararg effects: AudioFile): String {
        val stringBuilder: java.lang.StringBuilder = StringBuilder()
        var i = 1
        for (file: AudioFile? in effects) {
            stringBuilder.append("[$i]adelay=")
            stringBuilder.append(file?.startOffset)
            stringBuilder.append("|")
            stringBuilder.append(file?.startOffset)
            stringBuilder.append("[s$i];")
            i += 1
        }
        return stringBuilder.toString()
    }

    private fun getMixParamsMerged(vararg effects: AudioFile): String {
        val stringBuilder: java.lang.StringBuilder = java.lang.StringBuilder("[0]")
        var i = 1
        for (file: AudioFile? in effects) {
            stringBuilder.append("[s$i]")
            i += 1
        }
        stringBuilder.append("amix=$i[mixout]")
        return stringBuilder.toString()
    }


    private fun overlayAudios(
        fileName1: String,
        fileName2: String,
        fileName3: String
    ) {
        val cmd: Array<String> =
            arrayOf(
                "-y",
                "-i",
                fileName1.orEmpty(),
                "-i",
                fileName2.orEmpty(),
                "-filter_complex",
                "amerge=inputs=2",
                fileName3.orEmpty()
            )
        val rc = FFmpegKit.execute(cmd)
        when {
            rc.returnCode.isSuccess -> {
                mFFMepgCallback.onSuccess(cmd)
            }
            rc.returnCode.isCancel -> {
                mFFMepgCallback.onCancel(cmd)
            }
            else -> {
                mFFMepgCallback.onFailed(cmd, rc.returnCode.value, "")
            }
        }
    }

}
