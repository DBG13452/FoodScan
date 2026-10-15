package com.foodscan.app.ui.scanner

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.foodscan.app.R
import com.foodscan.app.domain.model.ProductBarcode
import com.foodscan.app.ui.components.FoodScanButton
import com.foodscan.app.ui.components.FoodScanButtonStyle
import com.foodscan.app.ui.components.FoodScanScreenColumn
import com.foodscan.app.ui.components.FoodScanScreenTitle
import com.foodscan.app.ui.components.FoodScanSectionTitle
import com.foodscan.app.ui.theme.FoodScanSpacing
import com.foodscan.app.ui.theme.FoodScanTheme

@Composable
fun ScannerScreen(onHome: () -> Unit, onBarcode: (String) -> Unit, modifier: Modifier = Modifier, isActive: Boolean = true) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val keyboard = LocalSoftwareKeyboardController.current
    var input by rememberSaveable { mutableStateOf("") }
    var invalid by rememberSaveable { mutableStateOf(false) }
    var askedPermission by rememberSaveable { mutableStateOf(false) }
    var cameraRequested by rememberSaveable { mutableStateOf(false) }
    var openingProduct by remember { mutableStateOf(false) }
    var cameraError by remember { mutableStateOf(false) }
    fun hasPermission() = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    var permissionGranted by remember { mutableStateOf(hasPermission()) }
    var resumed by remember { mutableStateOf(owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        askedPermission = true
        permissionGranted = granted
        cameraRequested = granted
    }
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            resumed = owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
            if (event == Lifecycle.Event.ON_RESUME) permissionGranted = hasPermission()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(isActive) { if (isActive) openingProduct = false }
    fun openProduct(raw: String) {
        val code = ProductBarcode.normalize(raw)
        invalid = code == null
        if (code != null && !openingProduct) {
            openingProduct = true
            cameraRequested = false
            keyboard?.hide()
            onBarcode(ProductBarcode.forLookup(code))
        }
    }
    val permanentlyDenied = askedPermission && !permissionGranted &&
        context.findActivity()?.let { !ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA) } == true
    FoodScanScreenColumn(modifier) {
        FoodScanScreenTitle(stringResource(R.string.scanner_title))
        if (cameraRequested && permissionGranted && isActive && resumed && !cameraError) {
            BarcodeCamera(
                onBarcode = ::openProduct,
                onError = { cameraError = true; cameraRequested = false },
                modifier = Modifier.fillMaxWidth().height(240.dp).clip(MaterialTheme.shapes.large),
            )
            Text(stringResource(R.string.scanner_aim), style = MaterialTheme.typography.bodyMedium)
            FoodScanButton(stringResource(R.string.scanner_stop_camera), { cameraRequested = false },
                style = FoodScanButtonStyle.Text)
        } else {
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer) {
                Column(Modifier.fillMaxWidth().padding(FoodScanSpacing.extraLarge),
                    verticalArrangement = Arrangement.spacedBy(FoodScanSpacing.large)) {
                    Text(stringResource(when {
                        cameraError -> R.string.scanner_camera_error
                        askedPermission && !permissionGranted -> R.string.scanner_permission_denied
                        else -> R.string.scanner_camera_hint
                    }), style = MaterialTheme.typography.bodyLarge)
                    FoodScanButton(stringResource(if (permanentlyDenied) R.string.scanner_settings else R.string.scanner_start_camera), {
                        cameraError = false
                        if (permanentlyDenied) {
                            context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                "package:${context.packageName}".toUri()))
                        } else if (hasPermission()) {
                            permissionGranted = true
                            cameraRequested = true
                        } else permission.launch(Manifest.permission.CAMERA)
                    }, Modifier.fillMaxWidth())
                }
            }
        }
        FoodScanSectionTitle(stringResource(R.string.scanner_manual_title))
        OutlinedTextField(
            value = input,
            onValueChange = { input = it.take(32); invalid = false },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.scanner_barcode_label)) },
            singleLine = true,
            isError = invalid,
            supportingText = { Text(stringResource(if (invalid) R.string.scanner_invalid_barcode else R.string.scanner_barcode_hint)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { openProduct(input) }),
        )
        FoodScanButton(stringResource(R.string.scanner_find), { openProduct(input) }, Modifier.fillMaxWidth(),
            enabled = input.isNotBlank() && !openingProduct)
        FoodScanButton(stringResource(R.string.back_home), onHome, Modifier.fillMaxWidth(), style = FoodScanButtonStyle.Text)
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Preview(name = "Scanner", widthDp = 360, locale = "ru")
@Preview(name = "Scanner • 200%", widthDp = 320, fontScale = 2f, locale = "ru")
@Composable
private fun ScannerPreview() { FoodScanTheme { Surface { ScannerScreen({}, {}) } } }
