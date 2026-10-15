package com.foodscan.app.ui.scanner

import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraState
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.LiveData
import com.foodscan.app.domain.model.ProductBarcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
internal fun BarcodeCamera(onBarcode: (String) -> Unit, onError: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val latestBarcode = rememberUpdatedState(onBarcode)
    val latestError = rememberUpdatedState(onError)
    val view = remember(context) {
        PreviewView(context).apply {
            // TextureView stays inside Compose clipping; SurfaceView can bleed over a new tab.
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    DisposableEffect(view, owner) {
        val disposed = AtomicBoolean(false)
        val delivered = AtomicBoolean(false)
        val executor = Executors.newSingleThreadExecutor()
        val mainExecutor = ContextCompat.getMainExecutor(context)
        val scanner = BarcodeScanning.getClient(BarcodeScannerOptions.Builder().setBarcodeFormats(
            Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8, Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E,
        ).build())
        val preview = Preview.Builder().build().also { it.setSurfaceProvider(view.surfaceProvider) }
        val analysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
        var provider: ProcessCameraProvider? = null
        var cameraState: LiveData<CameraState>? = null
        val cameraObserver = Observer<CameraState> { state ->
            if (!disposed.get() && state.error != null) latestError.value()
        }
        analysis.setAnalyzer(executor) { proxy ->
            val media = proxy.image
            if (disposed.get() || delivered.get() || media == null) {
                proxy.close()
            } else {
                try {
                    scanner.process(InputImage.fromMediaImage(media, proxy.imageInfo.rotationDegrees))
                        .addOnSuccessListener(mainExecutor) { codes ->
                            if (!disposed.get()) {
                                val code = codes.firstNotNullOfOrNull { barcode ->
                                    barcode.rawValue?.let { ProductBarcode.fromCamera(it, barcode.format == Barcode.FORMAT_UPC_E) }
                                }
                                if (code != null && delivered.compareAndSet(false, true)) latestBarcode.value(code)
                            }
                        }
                        .addOnFailureListener(mainExecutor) { if (!disposed.get()) latestError.value() }
                        .addOnCompleteListener { proxy.close() }
                } catch (_: Exception) {
                    proxy.close()
                    mainExecutor.execute { if (!disposed.get()) latestError.value() }
                }
            }
        }
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            if (!disposed.get()) {
                try {
                    provider = future.get()
                    val camera = provider?.bindToLifecycle(owner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
                    cameraState = camera?.cameraInfo?.cameraState
                    cameraState?.observe(owner, cameraObserver)
                } catch (_: Exception) {
                    latestError.value()
                }
            }
        }, mainExecutor)
        onDispose {
            disposed.set(true)
            analysis.clearAnalyzer()
            cameraState?.removeObserver(cameraObserver)
            provider?.unbind(preview, analysis)
            scanner.close()
            executor.shutdown()
        }
    }
    val frameColor = MaterialTheme.colorScheme.inversePrimary
    Box(modifier) {
        AndroidView(factory = { view }, modifier = Modifier.fillMaxSize())
        Canvas(Modifier.fillMaxSize()) {
            drawRect(frameColor, Offset(size.width * .1f, size.height * .3f),
                Size(size.width * .8f, size.height * .4f), style = Stroke(2.dp.toPx()))
        }
    }
}
