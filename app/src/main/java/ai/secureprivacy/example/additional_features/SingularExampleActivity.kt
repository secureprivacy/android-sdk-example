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
import com.google.firebase.analytics.FirebaseAnalytics
import com.singular.sdk.Singular
import com.singular.sdk.internal.SingularInstance

class SingularExampleActivity : ComponentActivity() {

    companion object {
        private const val TAG = "SingularExampleActivity"
    }

    private val binding by lazy {
        EventConsoleScreenBinding.inflate(layoutInflater, null, false)
    }

    private val consoleMsgBuilder by lazy { StringBuilder() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.customAppBar.ibBack.setOnClickListener { finish() }
        binding.customAppBar.tvTitle.text = "Demo - Singular"

        (application as SPApplication).getSPConsentEngine().observe(this) { result ->
            result?.data?.let { engine ->
                binding.btLogEvent.setOnClickListener { submitEvent(engine) }
            }
        }

        SPLogger.i(TAG,  "getSessionId(): ${Singular.getSessionId()}")
        SPLogger.i(TAG,  "getLimitDataSharing(): ${Singular.getLimitDataSharing()}")
        SPLogger.i(TAG,  "isAllTrackingStopped(): ${Singular.isAllTrackingStopped()}")
    }

    private fun submitEvent(engine: SPConsentEngine) = Utils.submitEvent(
        context = this,
        engine = engine,
        applicationId = AppConfig.APPLICATION_ID,
        supportedPackage = SPSupportedPackage.SINGULAR,
        etEventMessage = binding.etEventMessage,
        tvConsoleLabel = binding.tvConsoleLabel,
        tvConsole = binding.tvConsole,
        consoleMsgBuilder = consoleMsgBuilder,
        svConsole = binding.svConsole,
        submitEvent = { eventMsg ->
            val result = SingularInstance.getInstance().logEvent("EventMessage: $eventMsg")
            SPLogger.i(TAG, msg = "submitEvent($eventMsg)=>result: $result")
            return@submitEvent result
        }
    )
}