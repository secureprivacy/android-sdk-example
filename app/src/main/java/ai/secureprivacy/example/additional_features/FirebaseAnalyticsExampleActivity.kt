package ai.secureprivacy.example.additional_features

import ai.secureprivacy.example.AppConfig
import ai.secureprivacy.example.SPApplication
import ai.secureprivacy.example.databinding.EventConsoleScreenBinding
import ai.secureprivacy.example.support.Utils
import ai.secureprivacy.mobileconsent.consent_engine.SPConsentEngine
import ai.secureprivacy.mobileconsent.data.enums.SPSupportedPackage
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseAnalyticsExampleActivity : ComponentActivity() {

    companion object {
        private const val TAG = "FirebaseAnalyticsExampleActivity"
    }

    private val binding by lazy {
        EventConsoleScreenBinding.inflate(layoutInflater, null, false)
    }
    private val consoleMsgBuilder by lazy { StringBuilder() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.customAppBar.ibBack.setOnClickListener { finish() }
        binding.customAppBar.tvTitle.text = "Demo - Firebase Analytics"

        (application as SPApplication).getSPConsentEngine().observe(this) { result ->
            result?.data?.let { engine ->
                binding.btLogEvent.setOnClickListener { submitEvent(engine) }
            }
        }
    }

    private fun submitEvent(engine: SPConsentEngine) = Utils.submitEvent(
        context = this,
        engine = engine,
        applicationId = AppConfig.APPLICATION_ID,
        supportedPackage = SPSupportedPackage.FIREBASE_ANALYTICS,
        etEventMessage = binding.etEventMessage,
        tvConsoleLabel = binding.tvConsoleLabel,
        tvConsole = binding.tvConsole,
        consoleMsgBuilder = consoleMsgBuilder,
        svConsole = binding.svConsole,
        submitEvent = { eventMsg ->
            FirebaseAnalytics.getInstance(this).logEvent(
                "my_demo_event",
                Bundle().apply {
                    putString("type", "custom")
                    putString("msg", eventMsg)
                })
            true
        }
    )
}