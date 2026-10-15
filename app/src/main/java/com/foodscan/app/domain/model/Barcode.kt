package com.foodscan.app.domain.model

/** GTIN validation. Eight digits can be EAN-8 or compressed UPC-E (number system 0/1). */
object ProductBarcode {
    fun normalize(input: String): String? {
        val code = input.filterNot { it.isWhitespace() || it == '-' }
        if (code.length !in listOf(8, 12, 13) || code.any { it !in '0'..'9' }) return null
        if (code.all { it == '0' }) return null
        return code.takeIf { validChecksum(it) || (it.length == 8 && expandUpce(it)?.let(::validChecksum) == true) }
    }

    fun fromCamera(input: String, isUpce: Boolean): String? =
        if (isUpce) expandUpce(input)?.let(::normalize) else normalize(input)

    fun forLookup(code: String): String =
        if (code.length == 8 && !validChecksum(code)) expandUpce(code) ?: code else code

    internal fun validChecksum(code: String): Boolean {
        val sum = code.dropLast(1).reversed().mapIndexed { i, c -> (c - '0') * if (i % 2 == 0) 3 else 1 }.sum()
        return (10 - sum % 10) % 10 == code.last() - '0'
    }

    internal fun expandUpce(code: String): String? {
        if (code.length != 8 || code.first() !in "01" || code.any { it !in '0'..'9' }) return null
        val d = code.substring(1, 7)
        val body = when (d[5]) {
            '0', '1', '2' -> "${d.take(2)}${d[5]}0000${d.substring(2, 5)}"
            '3' -> "${d.take(3)}00000${d.substring(3, 5)}"
            '4' -> "${d.take(4)}00000${d[4]}"
            else -> "${d.take(5)}0000${d[5]}"
        }
        return "${code.first()}$body${code.last()}"
    }
}
