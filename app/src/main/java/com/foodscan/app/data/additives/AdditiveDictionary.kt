package com.foodscan.app.data.additives

import java.util.Locale

/** Editorial categories, not a toxicological score or an estimate of a product's dose. */
enum class AdditiveConcern { HIGH, ATTENTION, LOW, UNKNOWN }

data class AdditiveSource(val title: String, val url: String, val language: String = "ru")

data class AdditiveInfo(
    val code: String,
    val name: String,
    val purpose: String,
    val description: String,
    val concern: AdditiveConcern,
    val reason: String,
    val healthEffects: String,
    val sources: List<AdditiveSource>,
    val variants: List<String> = emptyList(),
)

object AdditiveDictionary {
    const val reviewedOn = "15.10.2026"
    val entries: List<AdditiveInfo> = additiveCatalog().sortedWith(
        compareBy<AdditiveInfo> { it.code.drop(1).takeWhile(Char::isDigit).toInt() }.thenBy { it.code },
    )
    private val byCode = entries.associateBy { it.code }
    // Only explicitly reviewed subgroups inherit an entry's description and concern.
    private val byVariant = entries.flatMap { item -> item.variants.map { normalize(it) to item } }.toMap()

    fun normalize(code: String): String = code.trim().substringAfter(':')
        .replace(Regex("[\\s\\u00A0-]+"), "").replace('Е', 'E').replace('е', 'e').uppercase(Locale.ROOT)

    fun find(code: String): AdditiveInfo? {
        val normalized = normalize(code)
        return byCode[normalized] ?: byVariant[normalized]
    }

    fun search(query: String): List<AdditiveInfo> {
        val terms = query.trim().lowercase(Locale.ROOT).replace('ё', 'е').split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (terms.isEmpty()) return entries
        val matchedCode = find(query)
        return entries.filter { item ->
            item == matchedCode || terms.all { term ->
                val name = "${item.name} ${item.purpose}".lowercase(Locale.ROOT).replace('ё', 'е')
                name.contains(term) || item.code.contains(normalize(term), ignoreCase = true)
            }
        }
    }
}
