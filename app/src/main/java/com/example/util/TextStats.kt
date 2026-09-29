package com.example.util

data class NoteTextStats(
    val characters: Int = 0,
    val charactersNoSpaces: Int = 0,
    val words: Int = 0,
    val sentences: Int = 0,
    val paragraphs: Int = 0
)

object TextStats {
    /**
     * Ultra-fast single-pass stats calculation without regex or string splits.
     * Allocates zero temporary arrays or string objects, making it instantaneous even for megabyte-scale notes.
     */
    fun calculate(text: CharSequence?): NoteTextStats {
        if (text.isNullOrEmpty()) {
            return NoteTextStats()
        }

        val len = text.length
        var characters = len
        var charactersNoSpaces = 0
        var words = 0
        var inWord = false
        var sentences = 0
        var inSentence = false
        var paragraphs = 0
        var consecutiveNewlines = 0
        var hasContentInParagraph = false

        for (i in 0 until len) {
            val c = text[i]
            val isWs = c.isWhitespace()

            if (!isWs) {
                charactersNoSpaces++
                hasContentInParagraph = true
                if (!inWord) {
                    inWord = true
                    words++
                }
                if (!inSentence) {
                    inSentence = true
                    sentences++
                }
            } else {
                inWord = false
            }

            if (c == '.' || c == '!' || c == '?') {
                inSentence = false
            }

            if (c == '\n') {
                consecutiveNewlines++
                if (consecutiveNewlines >= 2 && hasContentInParagraph) {
                    paragraphs++
                    hasContentInParagraph = false
                }
            } else if (!isWs) {
                consecutiveNewlines = 0
            }
        }

        if (hasContentInParagraph) {
            paragraphs++
        }

        return NoteTextStats(
            characters = characters,
            charactersNoSpaces = charactersNoSpaces,
            words = words,
            sentences = sentences.coerceAtLeast(if (words > 0) 1 else 0),
            paragraphs = paragraphs.coerceAtLeast(if (words > 0) 1 else 0)
        )
    }
}

