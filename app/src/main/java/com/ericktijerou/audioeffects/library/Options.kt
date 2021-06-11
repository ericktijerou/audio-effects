package com.ericktijerou.audioeffects.library

class Options(builder: Builder) {
    private val Y = "-y"
    private val I = "-i"
    private val AF = "-af"

    val option1: String
    val option2: String
    val option3: String

    val audioAlterOption: String
    val inFilename: String
    val outFilename: String

    fun get(): Array<String> {
        return arrayOf(option1, option2, inFilename, option3, audioAlterOption, outFilename)
    }

    class Builder(
        val audioAlterOption: String,
        val inFilename: String,
        val outFilename: String
    ) {
        var option1: String? = null
        var option2: String? = null
        var option3: String? = null

        fun option1(option1: String?) {
            this.option1 = option1
        }

        fun option2(option2: String?) {
            this.option2 = option2
        }

        fun option3(option3: String?) = apply {
            this.option3 = option3
        }

        fun build(): Options {
            return Options(this)
        }
    }

    init {
        audioAlterOption = builder.audioAlterOption
        inFilename = builder.inFilename
        outFilename = builder.outFilename
        option1 = if (builder.option1 == null) Y else builder.option1!!
        option2 = if (builder.option2 == null) I else builder.option2!!
        option3 = if (builder.option3 == null) AF else builder.option3!!
    }
}
