# FoodScan

Учебное Android-приложение для анализа пищевых продуктов по штрихкоду.
Package: `com.foodscan.app`. Kotlin, Jetpack Compose, Material 3.

Текущий результат — главный экран и навигация этапа 3 на основе дизайн-системы FoodScan.
Приложение показывает сценарий NORMAL, кнопку сканирования и три демонстрационных продукта.
Нижние вкладки открывают Главную, Сканирование, Избранное и Профиль.
Сканирование, избранное и настройки пока представлены заглушками; API и хранение не подключены.

## Документация

- [Рабочая спецификация](docs/FOODSCAN_SPEC.md) — изложение мастер-промпта пользователя.
- [Статус проекта](PROJECT_STATUS.md) — результат этапа и обнаруженные расхождения.
- [Дизайн-система](docs/DESIGN_SYSTEM.md) — компоненты, решения, примеры и демонстрация.
- [Проверка этапа 3 на эмуляторе](docs/STAGE_3_CHECKLIST.md) — запуск и ожидаемое поведение.
- [Журнал изменений](CHANGELOG.md).
- [Архив аудита](docs/AUDIT_STAGE_0.md) — исторический документ; его старый план не актуален.

## Сборка в Windows

Открыть существующий проект в Android Studio. Проверенная конфигурация: Gradle 9.6.0,
AGP 9.4.1, JVM Gradle 25, compileSdk/targetSdk 37, minSdk 26.
Путь Android SDK задаётся в локальном `local.properties`.

```powershell
.\gradlew.bat :app:assembleDebug :app:lintDebug --console=plain
```

При первой сборке требуется загрузка Navigation Compose и его транзитивных зависимостей.
После загрузки можно добавить `--offline` для сборки из локального кеша.

APK: `app/build/outputs/apk/debug/app-debug.apk`.
Lint: `app/build/reports/lint-results-debug.html`.

Для Preview главной открыть `HomeScreen.kt`; для главной вместе с навигацией — `FoodScanApp.kt`.
Каталог дизайн-системы остаётся в `FoodScanDesignSystemPreview.kt`.
Работа ограничена этапом 3; этап 4 требует подтверждения пользователя.
