package com.foodscan.app.data.additives

/** Every link opens a Russian-language page; scopes are recorded in ADDITIVES_METHODOLOGY.md. */
internal object AdditiveSources {
    val regulation = AdditiveSource("ЕЭК · ТР ТС 029/2012 и изменения",
        "https://eec.eaeunion.org/comission/department/deptexreg/tr/bezopPischDobavok.php")
    val amendment = AdditiveSource("ЕЭК · изменения № 84, приложения",
        "https://docs.eaeunion.org/docs/ru-ru/01440845/err_31082023_84")
    val who = AdditiveSource("ВОЗ · пищевые добавки",
        "https://www.who.int/ru/news-room/fact-sheets/detail/food-additives")
    val allergy = AdditiveSource("Пищевая аллергия · клинические рекомендации РФ, 2025",
        "https://diseases.medelement.com/disease/пищевая-аллергия-кр-рф-2025/19095")
    val colors = AdditiveSource("Ростест · предупреждение о красителях, ТР ТС 022/2011",
        "https://rtmsk.ru/faq/otsenka_sootvetstviya_faq/esli_krasitel_kotoryy_okazyvaet_otritsatelnoe_vliyanie_na_detey_ispolzuetsya_v_energeticheskikh_napi/")
    val polyols = AdditiveSource("Роспотребнадзор · маркировка продуктов с сахароспиртами",
        "https://zpp.rospotrebnadzor.ru/info/analysis/511180")
    val aspartame = AdditiveSource("ВОЗ · оценка опасности и риска аспартама, 2023",
        "https://www.who.int/ru/news/item/14-07-2023-aspartame-hazard-and-risk-assessment-results-released")
    val phenylalanine = AdditiveSource("Роспотребнадзор · подсластители и фенилкетонурия",
        "https://41.rospotrebnadzor.ru/content/den-rozhdeniya-zhevatelnoy-rezinki")
    val phosphates = AdditiveSource("Роспотребнадзор · питание при нарушении функции почек",
        "https://25.rospotrebnadzor.ru/press-tsentr/press-relizy/2235/")
    val nitrites = AdditiveSource("Роспотребнадзор · нитриты и нитрозосоединения",
        "https://39.rospotrebnadzor.ru/content/zagryazniteli-pishchevyh-produktov")
    val titanium = AdditiveSource("Роскачество · диоксид титана и ограничения в ЕС",
        "https://rskrf.ru/news/v-es-zapretili-dioksid-titana-v-produktakh/")
    val overview = AdditiveSource("Роскачество · добавки с индексом Е и неразрешённые коды",
        "https://rskrf.ru/tips/eksperty-obyasnyayut/zakodirovannaya-eda-bezvrednye-i-opasnye-dobavki-s-indeksom-e/")
    val red2g = AdditiveSource("Роспотребнадзор · постановление о запрете E128",
        "https://06.rospotrebnadzor.ru/content/postanovlenie-ot-03092007-no68-o-zapreshchenii-ispolzovaniya-pishchevoy-dobavki-e-128")
    val carrageenan = AdditiveSource("Роскачество · каррагинан, 2025",
        "https://rskrf.ru/tips/eksperty-obyasnyayut/karraginan/")
}
