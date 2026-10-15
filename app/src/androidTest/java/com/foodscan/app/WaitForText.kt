package com.foodscan.app

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithText

fun ComposeContentTestRule.waitForText(text: String) {
    waitUntil(5000) { onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
}
