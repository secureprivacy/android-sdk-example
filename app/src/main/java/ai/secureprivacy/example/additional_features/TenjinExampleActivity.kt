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
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.tenjin.android.TenjinSDK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TenjinExampleActivity : ComponentActivity() {

    companion object {
        private const val TAG = "TenjinExampleActivity"
    }

    private val binding by lazy {
        EventConsoleScreenBinding.inflate(layoutInflater, null, false)
    }

    private val consoleMsgBuilder by lazy { StringBuilder() }

    private val tenjin by lazy {
        TenjinSDK.getInstance(this, AppConfig.TENJIN_KEY)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.customAppBar.ibBack.setOnClickListener { finish() }
        binding.customAppBar.tvTitle.text = "Demo - Tenjin"

        (application as SPApplication).getSPConsentEngine().observe(this) { result ->
            result?.data?.let { engine ->
                binding.btLogEvent.setOnClickListener { submitEvent(engine) }
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            SPLogger.i(
                TAG,
                msg = "Advertising ID: ${AdvertisingIdClient.getAdvertisingIdInfo(this@TenjinExampleActivity).id}"
            )
        }
    }

    private fun submitEvent(engine: SPConsentEngine) = Utils.submitEvent(
        context = this,
        engine = engine,
        applicationId = AppConfig.APPLICATION_ID,
        supportedPackage = SPSupportedPackage.TENJIN,
        etEventMessage = binding.etEventMessage,
        tvConsoleLabel = binding.tvConsoleLabel,
        tvConsole = binding.tvConsole,
        consoleMsgBuilder = consoleMsgBuilder,
        svConsole = binding.svConsole,
        submitEvent = { eventMsg ->
            tenjin.eventWithName("EventMessage: $eventMsg")
            SPLogger.i(TAG, msg = "submitEvent($eventMsg)")
            return@submitEvent true
        }
    )
}