package ai.secureprivacy.example.additional_features

import ai.secureprivacy.example.AppConfig
import ai.secureprivacy.example.SPApplication
import ai.secureprivacy.example.databinding.EventConsoleScreenBinding
import ai.secureprivacy.example.support.Utils
import ai.secureprivacy.mobileconsent.consent_engine.SPConsentEngine
import ai.secureprivacy.mobileconsent.data.enums.SPSupportedPackage
import ai.secureprivacy.mobileconsent.support.SPLogger
import android.os.Bundle
import androidx.activity.ComponentActivity
import co.ab180.airbridge.Airbridge
import co.ab180.airbridge.common.AirbridgeAttribute
import co.ab180.airbridge.common.AirbridgeCategory
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AirbridgeExampleActivity : ComponentActivity() {

    companion object {
        private const val TAG = "AirbridgeExampleActivity"
    }

    private val binding by lazy {
        EventConsoleScreenBinding.inflate(layoutInflater, null, false)
    }

    private val consoleMsgBuilder by lazy { StringBuilder() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.customAppBar.ibBack.setOnClickListener { finish() }
        binding.customAppBar.tvTitle.text = "Demo - Airbridge"

        (application as SPApplication).getSPConsentEngine().observe(this) { result ->
            result?.data?.let(::setupViews)
        }
    }

    private fun setupViews(engine: SPConsentEngine) {
        binding.btLogEvent.setOnClickListener { submitEvent(engine) }

        SPLogger.INFO_LOGS_ENABLED = true
        CoroutineScope(Dispatchers.IO).launch {
            SPLogger.i(
                TAG,
                msg = "Advertising ID: ${AdvertisingIdClient.getAdvertisingIdInfo(this@AirbridgeExampleActivity).id}"
            )
        }
    }

    private fun submitEvent(engine: SPConsentEngine) = Utils.submitEvent(
        context = this,
        engine = engine,
        applicationId = AppConfig.APPLICATION_ID,
        supportedPackage = SPSupportedPackage.AIRBRIDGE,
        etEventMessage = binding.etEventMessage,
        tvConsoleLabel = binding.tvConsoleLabel,
        tvConsole = binding.tvConsole,
        consoleMsgBuilder = consoleMsgBuilder,
        svConsole = binding.svConsole,
        submitEvent = { eventMsg ->
            Airbridge.trackEvent(
                AirbridgeCategory.PRODUCT_VIEWED,
                semanticAttributes = mutableMapOf(Pair(AirbridgeAttribute.ACTION, "custom")),
                customAttributes = mutableMapOf(Pair("msg", eventMsg))
            )
            SPLogger.i(TAG, msg = "submitEvent($eventMsg)")
            return@submitEvent true
        }
    )
}