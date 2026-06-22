package com.example.zencillo_nexgo

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast

import com.nexgo.common.ByteUtils
import com.nexgo.oaf.apiv3.APIProxy
import com.nexgo.oaf.apiv3.DeviceEngine
import com.nexgo.oaf.apiv3.SdkResult

import com.nexgo.oaf.apiv3.device.printer.AlignEnum
import com.nexgo.oaf.apiv3.device.printer.DotMatrixFontEnum
import com.nexgo.oaf.apiv3.device.printer.FontEntity
import com.nexgo.oaf.apiv3.device.printer.Printer

import com.nexgo.oaf.apiv3.device.scanner.OnScannerListener
import com.nexgo.oaf.apiv3.device.scanner.Scanner
import com.nexgo.oaf.apiv3.device.scanner.ScannerCfgEntity

import com.nexgo.oaf.apiv3.device.reader.CardInfoEntity
import com.nexgo.oaf.apiv3.device.reader.CardReader
import com.nexgo.oaf.apiv3.device.reader.CardSlotTypeEnum
import com.nexgo.oaf.apiv3.device.reader.OnCardInfoListener
import com.nexgo.oaf.apiv3.device.reader.RfCardTypeEnum
import com.nexgo.oaf.apiv3.device.reader.TypeAInfoEntity

import io.flutter.plugin.common.MethodChannel
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class NexGoPlugin {

    private val tag = "NEXGO"

    private var deviceEngine: DeviceEngine? = null
    private var printer: Printer? = null
    private var scanner: Scanner? = null
    private var cardReader: CardReader? = null

    private val defaultFont = FontEntity(
        DotMatrixFontEnum.CH_SONG_24X24,
        DotMatrixFontEnum.ASC_SONG_12X24
    )

    fun print(
        text: String,
        context: Context,
        linkQr: String? = null,
        isQr: Boolean
    ): Pair<Boolean, String> {
        return try {
            deviceEngine = APIProxy.getDeviceEngine(context)
            printer = deviceEngine?.getPrinter()
            printer?.initPrinter()

            when (val initResult: Int? = printer?.status) {
                SdkResult.Success -> {
                    printer?.setLetterSpacing(0)
                    printer?.appendPrnStr(text, defaultFont, AlignEnum.CENTER)

                    if (linkQr != null && isQr) {
                        printer?.appendQRcode(linkQr, 500, AlignEnum.CENTER)
                    }

                    printer?.startPrint(true) {}

                    Pair(true, "OK")
                }

                SdkResult.Printer_PaperLack -> {
                    Pair(false, "Out of Paper!")
                }

                else -> {
                    Pair(false, "Printer Init Misc Error: $initResult")
                }
            }

        } catch (e: UnsatisfiedLinkError) {
            Log.e(tag, "UnsatisfiedLinkError printer: ", e)
            Toast.makeText(context, "UnsatisfiedLinkError ===> $e", Toast.LENGTH_LONG).show()
            Pair(false, "UnsatisfiedLinkError ===> $e")

        } catch (e: NoClassDefFoundError) {
            Log.e(tag, "NoClassDefFoundError printer: ", e)
            Toast.makeText(context, "NoClassDefFoundError ===> $e", Toast.LENGTH_LONG).show()
            Pair(false, "NoClassDefFoundError ===> $e")

        } catch (e: Exception) {
            Log.e(tag, "Exception printer: ", e)
            Toast.makeText(context, "Exception ===> $e", Toast.LENGTH_LONG).show()
            Pair(false, "Exception ===> $e")
        }
    }

    fun scan(
        activity: Activity,
        result: MethodChannel.Result
    ) {
        val answered = AtomicBoolean(false)
        val mainHandler = Handler(Looper.getMainLooper())

        fun success(message: String) {
            if (answered.compareAndSet(false, true)) {
                mainHandler.post {
                    result.success(message)
                }
            }
        }

        fun error(message: String) {
            if (answered.compareAndSet(false, true)) {
                mainHandler.post {
                    result.error(
                        "NEXGO_SCAN_ERROR",
                        message,
                        null
                    )
                }
            }
        }

        try {
            deviceEngine = APIProxy.getDeviceEngine(activity)

            /*
             * Esto lo tenías en tu código Java:
             * deviceEngine.getEmvHandler2("app2");
             *
             * Lo dejamos aquí para mantener una inicialización similar
             * al APK Android nativo que sí abría el scanner.
             */
            deviceEngine?.getEmvHandler2("app2")

            scanner = deviceEngine?.getScanner()

            if (scanner == null) {
                error("Scanner es null. No se pudo obtener el lector NexGo.")
                return
            }

            val cfgEntity = ScannerCfgEntity().apply {
                isAutoFocus = true
                isUsedFrontCcd = false
                isBulkMode = false
                interval = 1000

                customBundle = android.os.Bundle().apply {
                    putString("Title", "zencillo.com...")
                    putString("ScanTip", "please scan the Bar or QR code")
                }
            }

            scanner?.initScanner(cfgEntity, object : OnScannerListener {

                override fun onInitResult(retCode: Int) {
                    Log.d(tag, "initScanner retCode: $retCode")

                    if (retCode != SdkResult.Success) {
                        error("No se pudo inicializar scanner NexGo. Código: $retCode")
                        return
                    }

                    val startResult = scanner?.startScan(
                        60,
                        object : OnScannerListener {

                            override fun onInitResult(retCode: Int) {
                                Log.d(tag, "startScan onInitResult: $retCode")
                            }

                            override fun onScannerResult(retCode: Int, data: String?) {
                                Log.d(tag, "onScannerResult retCode: $retCode data: $data")

                                when (retCode) {
                                    SdkResult.Success -> {
                                        if (!data.isNullOrEmpty()) {
                                            success(data)
                                        } else {
                                            error("El scanner leyó vacío")
                                        }
                                    }

                                    SdkResult.TimeOut -> {
                                        error("Tiempo agotado al escanear")
                                    }

                                    SdkResult.Scanner_Customer_Exit -> {
                                        error("Scanner cancelado por el usuario")
                                    }

                                    SdkResult.Scanner_Other_Error -> {
                                        error("Error del scanner NexGo")
                                    }

                                    else -> {
                                        error("Error general scanner NexGo: $retCode")
                                    }
                                }
                            }
                        }
                    )

                    Log.d(tag, "startScan result: $startResult")

                    if (startResult != SdkResult.Success) {
                        error("No se pudo iniciar scanner NexGo. Código: $startResult")
                    }
                }

                override fun onScannerResult(retCode: Int, data: String?) {
                    Log.d(tag, "initScanner onScannerResult no usado: $retCode - $data")
                }
            })

        } catch (e: UnsatisfiedLinkError) {
            Log.e(tag, "UnsatisfiedLinkError scanner NexGo: ", e)
            error("UnsatisfiedLinkError scanner NexGo: ${e.message}")

        } catch (e: NoClassDefFoundError) {
            Log.e(tag, "NoClassDefFoundError scanner NexGo: ", e)
            error("NoClassDefFoundError scanner NexGo: ${e.message}")

        } catch (e: Exception) {
            Log.e(tag, "Exception scanner NexGo: ", e)
            error("Exception scanner NexGo: ${e.message}")
        }
    }

    fun nfc(context: Context): Pair<Boolean, String> {
        val latch = CountDownLatch(1)
        var resultPair: Pair<Boolean, String> = Pair(false, "NFC Timeout o Error")

        try {
            deviceEngine = APIProxy.getDeviceEngine(context)
            deviceEngine?.getEmvHandler2("app2")

            cardReader = deviceEngine?.cardReader

            if (cardReader == null) {
                return Pair(false, "CardReader es null. No se pudo obtener el lector NFC NexGo.")
            }

            val slotTypes = HashSet<CardSlotTypeEnum>()
            slotTypes.add(CardSlotTypeEnum.RF)

            cardReader?.searchCard(slotTypes, 10, object : OnCardInfoListener {

                override fun onCardInfo(retCode: Int, cardInfo: CardInfoEntity?) {
                    if (retCode == SdkResult.Success && cardInfo != null) {
                        if (
                            cardInfo.cardExistslot == CardSlotTypeEnum.RF &&
                            cardInfo.rfCardType == RfCardTypeEnum.ULTRALIGHT
                        ) {
                            val typeAInfo = TypeAInfoEntity()
                            val getInfoResult = cardReader?.getRfCardInfo(typeAInfo)

                            if (getInfoResult == SdkResult.Success) {
                                val uidHex = typeAInfo.uid?.let {
                                    ByteUtils.byteArray2HexString(it)
                                }

                                resultPair = Pair(true, uidHex ?: "UID vacío")
                            } else {
                                resultPair = Pair(false, "Error obteniendo info RF: $getInfoResult")
                            }
                        } else {
                            resultPair = Pair(false, "Tipo de tarjeta no soportado o no ULTRALIGHT")
                        }

                        cardReader?.close(cardInfo.cardExistslot)
                    } else {
                        resultPair = Pair(false, "Fallo al leer tarjeta. Código: $retCode")
                    }

                    latch.countDown()
                }

                override fun onSwipeIncorrect() {
                    resultPair = Pair(false, "Swipe Incorrecto. Intente de nuevo.")
                    latch.countDown()
                }

                override fun onMultipleCards() {
                    resultPair = Pair(false, "Múltiples tarjetas detectadas.")
                    cardReader?.stopSearch()
                    latch.countDown()
                }
            })

            if (!latch.await(10, TimeUnit.SECONDS)) {
                resultPair = Pair(false, "Timeout esperando tarjeta NFC")
            }

        } catch (e: Exception) {
            Log.e(tag, "Exception NFC: ", e)
            resultPair = Pair(false, "Exception NFC ===> $e")
        }

        return resultPair
    }
}