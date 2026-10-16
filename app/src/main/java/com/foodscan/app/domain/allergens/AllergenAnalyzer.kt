package com.foodscan.app.domain.allergens

import com.foodscan.app.domain.model.Product
import java.util.Locale

enum class AllergenOrigin { API, INGREDIENTS }
data class AllergenFinding(val name: String, val origins: Set<AllergenOrigin>)
data class AllergenSummary(
    val contains: List<AllergenFinding>,
    val traces: List<AllergenFinding>,
    val conflicts: List<String>,
)

/** Conservative RU/EN text extraction, not an allergen-free certification. Raw source data stays intact. */
object AllergenAnalyzer {
    // Explicit Unicode boundaries behave alike on desktop JDKs and Android's regex engine.
    private const val START = "(?<![\\p{L}\\p{N}_])"
    private const val END = "(?![\\p{L}\\p{N}_])"
    private data class Allergen(val name: String, val pattern: Regex)
    private fun allergen(name: String, pattern: String) = Allergen(name, Regex("$START(?:$pattern)$END", RegexOption.IGNORE_CASE))
    private val dictionary = listOf(
        allergen("Глютен", "глютен\\p{L}*|клейковин\\p{L}*|пшенич\\p{L}*|пшениц\\p{L}*|ржан\\p{L}*|рожь|ячмен\\p{L}*|овес|овсян\\p{L}*|gluten|wheat|rye|barley|oats?"),
        allergen("Молоко", "молок\\p{L}*|молочн\\p{L}*|лактоз\\p{L}*|сыворотк\\p{L}*|казеин\\p{L}*|сыр|сыра|сыром|сырн\\p{L}*|сливки|сливок|сливочн\\p{L}*|milk|lactose|whey|casein|cheese|cream"),
        allergen("Соя", "соя|сои|сою|соей|соев\\p{L}*|soy|soya|soybeans?"),
        allergen("Яйца", "яйц\\p{L}*|яичн\\p{L}*|меланж|egg|eggs|albumen"),
        allergen("Арахис", "арахис\\p{L}*|peanuts?"),
        allergen("Орехи", "орех\\p{L}*|миндал\\p{L}*|фундук\\p{L}*|фисташ\\p{L}*|кешью|пекан\\p{L}*|макадам\\p{L}*|nuts?|almonds?|hazelnuts?|walnuts?|cashews?|pistachios?|pecans?"),
        allergen("Рыба", "рыб\\p{L}*|лосос\\p{L}*|тунец|тунца|тунцом|треск\\p{L}*|анчоус\\p{L}*|fish|salmon|tuna|cod|anchov\\p{L}*"),
        allergen("Ракообразные", "ракообразн\\p{L}*|кревет\\p{L}*|краб\\p{L}*|омар\\p{L}*|лангуст\\p{L}*|crustaceans?|shrimps?|prawns?|crabs?|lobsters?"),
        allergen("Моллюски", "моллюск\\p{L}*|кальмар\\p{L}*|миди\\p{L}*|устриц\\p{L}*|осьминог\\p{L}*|molluscs?|mollusks?|squid|mussels?|oysters?|octopus"),
        allergen("Сельдерей", "сельдер\\p{L}*|celery"),
        allergen("Горчица", "горчиц\\p{L}*|горчичн\\p{L}*|mustard"),
        allergen("Кунжут", "кунжут\\p{L}*|сезам\\p{L}*|sesame(?:-seeds)?"),
        allergen("Люпин", "люпин\\p{L}*|lupins?"),
        allergen("Диоксид серы и сульфиты", "диоксид\\s+серы|сульфит\\p{L}*|sulphites?|sulfites?|sulphur-dioxide-and-sulphites|sulfur\\s+dioxide|e22[0-4678]|е22[0-4678]"),
    )
    private enum class Mode { CONTAINS, TRACE, NEGATION }
    private data class Marker(val start: Int, val end: Int, val mode: Mode, val commaList: Boolean = false)
    private val negation = Regex("$START(?:не\\s+содерж(?:ит|ат|ится|атся)(?:\\s+след(?:ов|ы))?|без(?:\\s+следов)?|does\\s+not\\s+contain|free\\s+from)$END", RegexOption.IGNORE_CASE)
    private val trace = Regex("$START(?:(?:может|могут)\\s+(?:содержать|присутствовать)|след(?:ы|ов|а|ах)|may\\s+contain|traces?\\s+of)$END", RegexOption.IGNORE_CASE)
    private val present = Regex("$START(?:содержит|содержат|содержится|содержатся|состав|ингредиенты|ingredients|contains)$END", RegexOption.IGNORE_CASE)
    private val listConnector = Regex("[\\s,:()–—-]*(?:(?:и|или|and|or|продукт\\p{L}*|переработки)\\s*)*[\\s,:()–—-]*")

    fun analyze(product: Product): AllergenSummary {
        val contains = linkedMapOf<String, MutableSet<AllergenOrigin>>()
        val traces = linkedMapOf<String, MutableSet<AllergenOrigin>>()
        val negated = linkedSetOf<String>()
        fun add(target: MutableMap<String, MutableSet<AllergenOrigin>>, name: String, origin: AllergenOrigin) {
            target.getOrPut(name) { linkedSetOf() }.add(origin)
        }
        fun normalize(text: String) = text.lowercase(Locale.ROOT).replace('ё', 'е')
            .replace(Regex("<[^>]*>"), " ").replace('_', ' ')
        fun fromApi(values: List<String>?, target: MutableMap<String, MutableSet<AllergenOrigin>>) {
            values.orEmpty().filter(String::isNotBlank).forEach { raw ->
                val matches = dictionary.filter { it.pattern.containsMatchIn(normalize(raw)) }
                if (matches.isEmpty()) add(target, raw, AllergenOrigin.API)
                else matches.forEach { add(target, it.name, AllergenOrigin.API) }
            }
        }
        fromApi(product.allergens, contains)
        fromApi(product.traces, traces)
        normalize(product.ingredients.orEmpty()).split(Regex("[.!?;\\n]+")).forEach { sentence ->
            val negatives = negation.findAll(sentence).map {
                Marker(it.range.first, it.range.last + 1, Mode.NEGATION, !it.value.startsWith("без"))
            }.toList()
            val markers = (negatives + listOf(trace to Mode.TRACE, present to Mode.CONTAINS).flatMap { (regex, mode) ->
                regex.findAll(sentence).filter { match -> negatives.none { match.range.first in it.start until it.end } }
                    .map { Marker(it.range.first, it.range.last + 1, mode) }.toList()
            }).sortedBy { it.start }
            data class Hit(val allergen: Allergen, val start: Int, val end: Int)
            val hits = dictionary.flatMap { a -> a.pattern.findAll(sentence).map { Hit(a, it.range.first, it.range.last + 1) }.toList() }
                .sortedBy { it.start }
            var mode = Mode.CONTAINS
            var negativeEnd: Int? = null
            var commaList = false
            var markerIndex = 0
            hits.forEach { hit ->
                while (markerIndex < markers.size && markers[markerIndex].end <= hit.start) {
                    val marker = markers[markerIndex++]
                    if (marker.mode == Mode.NEGATION) {
                        negativeEnd = marker.end; commaList = marker.commaList
                    } else { mode = marker.mode; negativeEnd = null }
                }
                val bridge = negativeEnd?.let { sentence.substring(it, hit.start) }
                val prefix = sentence.substring(0, hit.start)
                val suffix = sentence.substring(hit.end)
                val wordFree = Regex("^[-\\s]*free$END").containsMatchIn(suffix) ||
                    Regex("$START(?:не\\s+содержащ\\p{L}*)\\s*$").containsMatchIn(prefix)
                val isNegated = wordFree || (bridge != null && listConnector.matches(bridge) && (commaList || ',' !in bridge))
                if (isNegated) {
                    // Lactose-free does not mean free from milk proteins.
                    if (!Regex("^(?:лактоз|lactose)").containsMatchIn(sentence.substring(hit.start, hit.end))) negated += hit.allergen.name
                    negativeEnd = if (wordFree) null else hit.end
                } else {
                    negativeEnd = null
                    // Coconut is not a tree nut; compound flavour names are not confirmation of milk/fish/etc.
                    val coconutNut = hit.allergen.name == "Орехи" && Regex("(?:кокосов\\p{L}*|мускатн\\p{L}*)\\s*$").containsMatchIn(prefix)
                    val flavourOnly = Regex("$START(?:со?\\s+вкусом|вкус|аромат|ароматизатор\\p{L}*|flavou?r(?:ed)?(?:\\s+of)?)\\s*[(:«\"]*\\s*$").containsMatchIn(prefix) ||
                        Regex("^\\s+ароматизатор\\p{L}*").containsMatchIn(suffix)
                    val plantCream = hit.allergen.name == "Молоко" && Regex("$START(?:растительн\\p{L}*|кокосов\\p{L}*|овсян\\p{L}*|соев\\p{L}*)\\s*$").containsMatchIn(prefix)
                    val milkAcid = hit.allergen.name == "Молоко" &&
                        (Regex("^\\s+кислот\\p{L}*").containsMatchIn(suffix) || sentence.substring(hit.start, hit.end).startsWith("молочнокисл"))
                    if (!coconutNut && !flavourOnly && !plantCream && !milkAcid) add(if (mode == Mode.TRACE) traces else contains,
                        hit.allergen.name, AllergenOrigin.INGREDIENTS)
                }
            }
        }
        return AllergenSummary(contains.map { AllergenFinding(it.key, it.value.toSet()) },
            traces.filterKeys { it !in contains }.map { AllergenFinding(it.key, it.value.toSet()) },
            negated.filter { it in contains })
    }
}
