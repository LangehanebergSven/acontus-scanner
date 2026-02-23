package com.example.scanner.scanner

import android.content.Context
import android.util.Log
import com.keyence.autoid.sdk.SdkStatus
import com.keyence.autoid.sdk.scan.DecodeResult
import com.keyence.autoid.sdk.scan.ScanManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeyenceBarcodeScanner @Inject constructor(
    @field:ApplicationContext private val context: Context
) : BarcodeScanner, ScanManager.DataListener {

    private val TAG = "KeyenceBarcodeScanner"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _scannedBarcode = MutableStateFlow<String?>(null)
    override val scannedBarcode: StateFlow<String?> = _scannedBarcode

    // manager is nullable — helps avoid crashes if SDK isn't present at runtime
    private var scanManager: ScanManager? = null

    init {
        try {
            // Keyence sample uses: ScanManager.createScanManager(context)
            scanManager = ScanManager.createScanManager(context)

            scanManager?.let { mgr ->
                mgr.addDataListener(this) // register callback
                // try to apply sensible defaults (guarded)
                try {
                    // Example: configure code types or scan params if available.
                    // The exact config classes differ by SDK version; wrap in try/catch.
                    val codeType = com.keyence.autoid.sdk.scan.scanparams.CodeType()
                    val status = mgr.getConfig(codeType)
                    if (status == SdkStatus.SUCCESS) {
                        // tweak what you need, for example:
                        // codeType.qrCode = true
                        // codeType.upcEanJan = true
                        mgr.setConfig(codeType)
                    }
                } catch (e: Throwable) {
                    Log.w(TAG, "Could not configure scan params: ${e.localizedMessage}")
                }

            } ?: run {
                Log.w(TAG, "ScanManager not available at runtime")
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to init ScanManager: ${t.localizedMessage}", t)
            scanManager = null
        }
    }

    override fun startScanning() {
        scope.launch {
            try {
                // sample SDK uses startRead()
                scanManager?.startRead()
            } catch (t: Throwable) {
                Log.e(TAG, "startScanning error: ${t.localizedMessage}", t)
            }
        }
    }

    override fun stopScanning() {
        scope.launch {
            try {
                // sample SDK uses stopRead()
                if (scanManager != null && scanManager!!.isReading) {
                    scanManager?.stopRead()
                } else {
                    // not reading or manager missing — still safe to call stop-like methods guarded
                    Log.d(TAG, "stopScanning called but nothing to stop")
                }
            } catch (t: Throwable) {
                Log.e(TAG, "stopScanning error: ${t.localizedMessage}", t)
            }
        }
    }

    override fun release() {
        scope.launch {
            try {
                // remove listener and release manager
                try {
                    scanManager?.removeDataListener(this@KeyenceBarcodeScanner)
                } catch (ignored: Throwable) { /* some SDK versions may differ */ }

                try {
                    // sample uses releaseScanManager()
                    scanManager?.releaseScanManager()
                } catch (e: NoSuchMethodError) {
                    // some versions might use release() instead
                    try {
                        scanManager?.javaClass?.getMethod("release")?.invoke(scanManager)
                    } catch (inner: Throwable) {
                        Log.w(TAG, "Release method not found on ScanManager: ${inner.localizedMessage}")
                    }
                } catch (t: Throwable) {
                    Log.w(TAG, "Error releasing ScanManager: ${t.localizedMessage}")
                }
            } catch (t: Throwable) {
                Log.e(TAG, "release error: ${t.localizedMessage}", t)
            } finally {
                scanManager = null
            }
        }
    }

    /**
     * Keyence SDK callback:
     * override fun onDataReceived(decodeResult: DecodeResult?)
     *
     * decodeResult.result gives a DecodeResult.Result enum (SUCCESS, WARNING, TIMEOUT, ...)
     * decodeResult.data contains the scanned string when result == SUCCESS.
     */
    override fun onDataReceived(decodeResult: DecodeResult?) {
        scope.launch {
            try {
                if (decodeResult == null) {
                    Log.w(TAG, "onDataReceived: decodeResult is null")
                    return@launch
                }

                when (decodeResult.result) {
                    DecodeResult.Result.SUCCESS -> {
                        val data = decodeResult.data
                        if (!data.isNullOrBlank()) {
                            _scannedBarcode.value = data
                        } else {
                            Log.w(TAG, "onDataReceived: empty data")
                        }
                        // optionally stopRead() / cancel trigger if you want single-shot behavior
                        try { scanManager?.stopRead() } catch (_: Throwable) {}
                    }
                    DecodeResult.Result.ALERT -> {
                        Log.w(TAG, "onDataReceived: ALERT")
                    }
                    DecodeResult.Result.TIMEOUT -> {
                        Log.w(TAG, "onDataReceived: TIMEOUT")
                    }
                    DecodeResult.Result.CANCELED -> {
                        Log.w(TAG, "onDataReceived: CANCELED")
                    }
                    DecodeResult.Result.FAILED -> {
                        Log.w(TAG, "onDataReceived: FAILED")
                    }
                    else -> {
                        Log.w(TAG, "onDataReceived: unknown result ${decodeResult.result}")
                    }
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Error in onDataReceived: ${t.localizedMessage}", t)
            }
        }
    }
}
