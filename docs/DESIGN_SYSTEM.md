# Дизайн-система FoodScan — этап 2

## Устройство

Код находится в `app/src/main/java/com/foodscan/app/ui`:

| Файл | Назначение |
| --- | --- |
| `theme/Color.kt` | Все 11 исходных цветов, дополнительные контрастные оттенки, семантические акценты |
| `theme/Theme.kt` | Отображение палитры на роли Material 3 |
| `theme/Type.kt` | Все 15 стилей типографики, системный SansSerif, размеры в sp |
| `theme/Shape.kt` | Скругления 8, 12, 16, 24, 28 dp |
| `theme/Spacing.kt` | Отступы 4, 8, 12, 16, 20, 24, 32 dp, минимум высоты кнопки 48 dp |
| `components/FoodScanButton.kt` | Основная, контурная и текстовая кнопки; disabled-состояние |
| `components/FoodScanCard.kt` | Белая карточка с тонкой рамкой, скруглением 24 dp и внутренним отступом 16 dp |
| `components/FoodScanSectionTitle.kt` | Заголовок с семантикой heading для средств доступности |
| `components/FoodScanMessage.kt` | Информация, успех, предупреждение, ошибка; текстовое объяснение состояния |
| `components/FoodScanDesignSystemPreview.kt` | Каталог для Android Studio Preview |
| `components/FoodScanScreenColumn.kt` | Добавлен на этапе 3: прокрутка коротких экранов, общие отступы и ширина до 640 dp |

На этом этапе тема всегда светлая. Динамические цвета Android отключены, чтобы
цвет обоев не менял фирменную палитру. Тёмная тема не реализована. Системные панели
в MainActivity настроены на тёмные значки для светлого фона, включая тёмный режим ОС.

Компоненты не содержат продуктовой логики и не требуют ViewModel. Действия передаются
через `onClick`, строки — через параметры, ширина — через `Modifier` вызывающего экрана.
Будущие экраны передают локализованные строки через `stringResource`.
Демонстрационные надписи каталога используются только в Preview.

## Пример использования

```kotlin
FoodScanTheme {
    FoodScanCard(modifier = Modifier.fillMaxWidth()) {
        FoodScanSectionTitle(text = title)
        Text(text = description, style = MaterialTheme.typography.bodyLarge)
        FoodScanButton(
            text = actionLabel,
            onClick = onAction,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
```

`FoodScanCard` — контейнер без собственного действия. Используйте явную кнопку внутри.
`FoodScanMessage` принимает `FoodScanMessageTone`; заголовок обязан объяснять состояние
словами. Тон сообщения сам по себе не является оценкой продукта или пищевой добавки.

## Контраст и адаптация

Яркие Success, Warning, Danger используются как акценты рамок, а текст сообщений —
`onSurface`. Для Material `error` добавлен более тёмный `#A12626`; исходный Danger
`#E05252` сохранён. Белый текст используется на Brand Primary, тёмный — на Brand Accent.
Text Secondary предназначен для белого и нейтрального фона; на Brand Soft используйте
`onPrimaryContainer` или `onSurface`. `surfaceVariant` имеет нейтральный фон.

Расчёт по относительной яркости sRGB (проверка цветовых пар, не визуальный UI-тест):

| Пара | Контраст |
| --- | --- |
| Белый / Brand Primary | 12,15:1 |
| Text Primary / Surface | 16,66:1 |
| Text Secondary / Background | 4,77:1 |
| Text Primary / Brand Accent | 7,51:1 |
| ErrorText / Surface | 7,45:1 |
| Text Primary / WarningContainer | 15,36:1 |

У текстов нет ограничения числа строк, у карточек и кнопок нет фиксированной высоты.
Кнопки имеют минимальную высоту 48 dp; подпись переносится и увеличивает высоту.
Размеры текста в sp следуют системной настройке. Каталог прокручивается и ограничивает
ширину содержимого на планшете; будущие экраны должны сами задавать адаптивную раскладку.
Disabled-цвета и поведение нажатия предоставляются стандартными кнопками Material 3.

## Как показать преподавателю

1. Открыть `FoodScanDesignSystemPreview.kt` в Android Studio, включить Split или Design.
2. Выполнить Build & Refresh. Доступны Phone (360 dp), Narrow (320 dp, шрифт 200%),
   Tablet (800 dp) и светлая тема при тёмной настройке ОС.
3. В Interactive Preview прокрутить каталог до сообщений и проверить все варианты.
4. Убедиться, что длинная подпись переносится, карточки растут по высоте, текст читаем,
   disabled-кнопка не реагирует на нажатия.

Каталог компонентов не подключён к MainActivity. Начиная с этапа 3 APK открывает
главный экран FoodScan с нижней навигацией. `HomeScreen.kt` содержит отдельные Preview
главной (включая пустой список), а `FoodScanApp.kt` — Preview вместе с нижней панелью.

## Проверки и ограничения на момент завершения этапа 2

Выполнены `:app:assembleDebug` и `:app:lintDebug` в offline-режиме, обе задачи успешны.
Новые зависимости не добавлялись, версии SDK и Gradle не менялись.
Lint: 0 ошибок, 15 прежних предупреждений — 6 GradleDependency,
1 NewerVersionAvailable, 1 RedundantLabel, 7 UnusedResources.

Preview скомпилирован в составе debug-сборки, но визуальный рендер в Android Studio,
запуск на эмуляторе, TalkBack и ручная проверка увеличенного шрифта не выполнялись.
Проверка компиляции не подтверждает отсутствие всех визуальных дефектов.
Unit- и instrumented-тесты на этом этапе не запускались: новой бизнес-логики нет.
