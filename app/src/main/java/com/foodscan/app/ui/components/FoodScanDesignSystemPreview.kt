package com.foodscan.app.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.foodscan.app.ui.theme.FoodScanSpacing
import com.foodscan.app.ui.theme.FoodScanTheme

// A component catalog for Android Studio, not an application screen.
@Preview(name = "Phone", widthDp = 360, heightDp = 960, locale = "ru")
@Preview(name = "Narrow • font 200%", widthDp = 320, heightDp = 960, fontScale = 2f, locale = "ru")
@Preview(name = "Tablet", widthDp = 800, heightDp = 1100, locale = "ru")
@Preview(
    name = "Light brand in dark system",
    widthDp = 360,
    heightDp = 960,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    locale = "ru",
)
@Composable
private fun FoodScanDesignSystemPreview() {
    FoodScanTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
                    .padding(FoodScanSpacing.screen),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(FoodScanSpacing.large),
                ) {
                    Text(
                        text = "FoodScan",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Каталог компонентов · этап 2",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FoodScanSectionTitle("Кнопки")
                    FoodScanButton("Основное действие", {}, Modifier.fillMaxWidth())
                    FoodScanButton(
                        "Дополнительное действие с длинной подписью",
                        {},
                        Modifier.fillMaxWidth(),
                        style = FoodScanButtonStyle.Secondary,
                    )
                    FoodScanButton("Текстовое действие", {}, style = FoodScanButtonStyle.Text)
                    FoodScanButton("Недоступное действие", {}, Modifier.fillMaxWidth(), enabled = false)
                    FoodScanSectionTitle("Карточка")
                    FoodScanCard(Modifier.fillMaxWidth()) {
                        Text("Заголовок карточки", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Основной текст свободно переносится на несколько строк и масштабируется вместе с системным шрифтом.",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(
                            "Дополнительная информация",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    FoodScanSectionTitle("Сообщения")
                    FoodScanMessage("Информация", "Описание состояния интерфейса.", Modifier.fillMaxWidth())
                    FoodScanMessage(
                        "Готово", "Действие выполнено.", Modifier.fillMaxWidth(), FoodScanMessageTone.Success,
                    )
                    FoodScanMessage(
                        "Обратите внимание", "Часть сведений отсутствует.", Modifier.fillMaxWidth(), FoodScanMessageTone.Warning,
                    )
                    FoodScanMessage(
                        "Не удалось загрузить", "Попробуйте ещё раз позже.", Modifier.fillMaxWidth(), FoodScanMessageTone.Error,
                    )
                }
            }
        }
    }
}
