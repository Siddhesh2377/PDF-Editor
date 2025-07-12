package com.example.pdfeditor.pdfEngine

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.zip.InflaterInputStream

class PdfTokenizer(input: InputStream) {
    private val buffer: ByteArray = input.readBytes()
    private var position: Int = 0

    fun nextToken(): String? {
        skipWhitespace()
        if (position >= buffer.size) return null

        return when (buffer[position].toInt().toChar()) {
            '/' -> readName()
            '(' -> readString()
            '<' -> if (buffer.getOrNull(position + 1)?.toInt()
                    ?.toChar() == '<'
            ) readDictionaryStart() else readHexString()

            '>' -> if (buffer.getOrNull(position + 1)?.toInt()
                    ?.toChar() == '>'
            ) readDictionaryEnd() else ">"

            '[' -> {
                position++; "["
            }

            ']' -> {
                position++; "]"
            }

            else -> readLiteral()
        }
    }

    private fun skipWhitespace() {
        while (position < buffer.size && buffer[position].toInt().toChar().isWhitespace()) {
            position++
        }
    }

    private fun readName(): String {
        val start = position++
        while (position < buffer.size && !buffer[position].toInt().toChar()
                .isWhitespace() && buffer[position].toInt().toChar() !in listOf(
                '/',
                '<',
                '>',
                '(',
                ')'
            )
        ) {
            position++
        }
        return buffer.decodeToString(start, position)
    }

    private fun readString(): String {
        val start = ++position
        while (position < buffer.size && buffer[position].toInt().toChar() != ')') position++
        val str = buffer.decodeToString(start, position)
        position++ // skip ')'
        return "($str)"
    }

    private fun readDictionaryStart(): String {
        position += 2
        return "<<"
    }

    private fun readDictionaryEnd(): String {
        position += 2
        return ">>"
    }

    private fun readHexString(): String {
        val start = ++position
        while (position < buffer.size && buffer[position].toInt().toChar() != '>') position++
        val str = buffer.decodeToString(start, position)
        position++ // skip '>'
        return "<$str>"
    }

    private fun readLiteral(): String? {
        val start = position
        while (position < buffer.size && !buffer[position].toInt().toChar()
                .isWhitespace() && buffer[position].toInt().toChar() !in listOf(
                '/',
                '[',
                ']',
                '<',
                '>',
                '(',
                ')'
            )
        ) {
            position++
        }
        return if (position > start) buffer.decodeToString(start, position) else null
    }

    /**
     * Reads raw stream data after `stream` keyword
     * @param length: The number of bytes to read
     */
    fun readStreamBytes(length: Int): ByteArray {
        // Move to first byte after `stream` (and newline)
        skipLineBreak()
        val streamBytes = buffer.copyOfRange(position, position + length)
        position += length
        return streamBytes
    }

    fun decompressFlateData(raw: ByteArray): String {
        val inflater = InflaterInputStream(ByteArrayInputStream(raw))
        return inflater.bufferedReader().use { it.readText() }
    }

    private fun skipLineBreak() {
        // Handles both \r\n and \n or \r
        if (buffer.getOrNull(position)?.toInt()?.toInt()?.toChar() == '\r') {
            position++
            if (buffer.getOrNull(position)?.toInt()?.toInt()?.toChar() == '\n') position++
        } else if (buffer.getOrNull(position)?.toInt()?.toInt()?.toChar() == '\n') {
            position++
        }
    }
}
