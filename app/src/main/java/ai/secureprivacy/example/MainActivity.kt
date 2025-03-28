package ai.secureprivacy.example

import ai.secureprivacy.example.databinding.ActivityMainBinding
import ai.secureprivacy.mobileconsent.consent_engine.SPConsentEngine
import ai.secureprivacy.mobileconsent.data.dto.SPAuthKey
import ai.secureprivacy.mobileconsent.data.dto.SPConsentEvent
import ai.secureprivacy.mobileconsent.data.dto.SPDataMessage
import ai.secureprivacy.mobileconsent.listeners.SPConsentEventListener
import ai.secureprivacy.mobileconsent.ui.ConsentBanner
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.ComponentActivity
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * MainActivity for demonstrating the usage of the Secure Privacy Mobile Consent SDK.
 *
 * This activity initializes the SDK, handles consent status checks, displays consent banners,
 * and listens for consent events.
 *
 * ## Features:
 * - Initialize the Secure Privacy SDK.
 * - Show primary and secondary consent banners.
 * - Check the consent status of a given package.
 * - Observe and listen to consent events.
 * - Clear local session data.
 *
 * @author Secure Privacy
 */
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "SecurePrivacy"
        private const val CONSENT_REQUEST_CODE = 1008
        private const val PRIMARY_CONSENT_EVENT_REQUEST_CODE = 1009
        private const val SECONDARY_CONSENT_EVENT_REQUEST_CODE = 1010
    }

    private val binding by lazy {
        ActivityMainBinding.inflate(LayoutInflater.from(this), null, false)
    }

    private var spConsentEngine: SPConsentEngine? = null

    /**
     * Determines the selected application ID based on the chosen radio button.
     *
     * @return The primary or secondary application ID.
     */
    private fun getSelectedApplicationId(): String = when (binding.rbPrimaryApp.id) {
        binding.rbPrimaryApp.id -> Config.APPLICATION_ID
        else -> Config.SECONDARY_APPLICATION_ID
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initialiseSDK()

        // Set up radio button listeners
        binding.rbPrimaryApp.setOnClickListener {
            binding.packageStatusText.text = "Please enter a package name"
            binding.showConsentBtn.text = "Show Consent Banner"
            binding.packageInput.text.clear()
            fetchConsentStatus()
        }

        binding.rbSecondaryApp.setOnClickListener {
            binding.packageStatusText.text = "Please enter a package name"
            binding.showConsentBtn.text = "Show Secondary Banner"
            binding.packageInput.text.clear()
            fetchConsentStatus()
        }

        // Set up UI button actions
        binding.checkConsentStatusBtn.setOnClickListener { fetchConsentStatus() }
        binding.showConsentBtn.setOnClickListener { showConsentBanner() }
        binding.checkPackageStatusBtn.setOnClickListener { checkPackageStatus() }
        binding.clearSessionBtn.setOnClickListener { clearSession() }

        // Observe SDK consent events
        SPConsentEngine.getConsentEventsData(CONSENT_REQUEST_CODE)
            .observe(this@MainActivity) { event -> fetchConsentStatus() }
    }

    override fun onStart() {
        super.onStart()

        // Register primary consent event listener
        SPConsentEngine.addListener(
            PRIMARY_CONSENT_EVENT_REQUEST_CODE,
            object : SPConsentEventListener {
                override val applicationId: String = Config.APPLICATION_ID
                override fun onConsentAction(data: SPDataMessage<SPConsentEvent>) {
                    Log.d(TAG, "onConsentAction(${data})")
                }
            })

        // Register secondary consent event listener
        SPConsentEngine.addListener(
            SECONDARY_CONSENT_EVENT_REQUEST_CODE,
            object : SPConsentEventListener {
                override val applicationId: String = Config.APPLICATION_ID
                override fun onConsentAction(data: SPDataMessage<SPConsentEvent>) {
                    Log.d(TAG, "onConsentAction(${data})")
                }
            })
    }

    override fun onStop() {
        // Remove consent event listeners
        SPConsentEngine.removeListener(PRIMARY_CONSENT_EVENT_REQUEST_CODE)
        SPConsentEngine.removeListener(SECONDARY_CONSENT_EVENT_REQUEST_CODE)
        super.onStop()
    }

    /**
     * Initializes the Secure Privacy SDK.
     *
     * This method runs asynchronously and updates the UI upon completion.
     */
    private fun initialiseSDK() {
        binding.llContent.visibility = View.GONE
        binding.progressBar.show()

        lifecycleScope.launch(Dispatchers.IO) {
            val result = SPConsentEngine.initialise(
                this@MainActivity, SPAuthKey(
                    applicationId = Config.APPLICATION_ID,
                    secondaryApplicationId = Config.SECONDARY_APPLICATION_ID
                )
            )
            withContext(Dispatchers.Main) {
                binding.progressBar.hide()
                if (result.code == 200 && result.data != null) {
                    spConsentEngine = result.data!!
                    binding.sdkStatusText.text = HtmlCompat.fromHtml(
                        "<b>SDK Status</b>: Initialised!",
                        HtmlCompat.FROM_HTML_MODE_COMPACT
                    )
                    binding.llContent.visibility = View.VISIBLE
                    fetchClientId()
                    fetchConsentStatus()
                } else {
                    binding.sdkStatusText.text = HtmlCompat.fromHtml(
                        "<b>SDK Status</b>: ${result.code} ${result.msg}",
                        HtmlCompat.FROM_HTML_MODE_COMPACT
                    )
                }
            }
        }
    }

    /**
     * Fetches and updates the UI with the client ID for the selected application.
     */
    private fun fetchClientId() {
        val result = spConsentEngine?.getClientId(getSelectedApplicationId())
        binding.rbPrimaryApp.text = HtmlCompat.fromHtml(
            "<b>Primary</b>:<br>${result?.data ?: result?.msg}",
            HtmlCompat.FROM_HTML_MODE_COMPACT
        )
        binding.rbSecondaryApp.text = HtmlCompat.fromHtml(
            "<b>Secondary</b>:<br>${result?.data ?: result?.msg}",
            HtmlCompat.FROM_HTML_MODE_COMPACT
        )
    }

    /**
     * Fetches and updates the consent status for the selected application.
     */
    private fun fetchConsentStatus() {
        val status = spConsentEngine?.getConsentStatus(getSelectedApplicationId())
        binding.consentStatusText.text = HtmlCompat.fromHtml(
            "<b>Consent Status</b>: ${
                if (status?.code == 200) status.data else status?.msg
            }",
            HtmlCompat.FROM_HTML_MODE_COMPACT
        )
    }

    /**
     * Displays the appropriate consent banner based on the selected application type.
     */
    private fun showConsentBanner() = when (binding.rbPrimaryApp.id) {
        binding.rbPrimaryApp.id -> ConsentBanner.show(this)
        else -> ConsentBanner.showSecondary(this)
    }

    /**
     * Checks whether a specific package is enabled within the selected application ID.
     * Updates the UI accordingly.
     */
    private fun checkPackageStatus() {
        val packageId = binding.packageInput.text.toString().trim()

        if (packageId.isEmpty()) {
            binding.packageStatusText.text = "Please enter a package name"
            return
        }

        val result = spConsentEngine?.getPackage(packageId, getSelectedApplicationId())
        binding.packageStatusText.text = HtmlCompat.fromHtml(
            "<b>Package status</b>: ${if (result?.data?.isEnabled == true) "Enabled" else "Disabled"}!",
            HtmlCompat.FROM_HTML_MODE_COMPACT
        )
    }

    /**
     * Clears the local session data and re-initializes the SDK.
     */
    private fun clearSession() {
        lifecycleScope.launch(Dispatchers.IO) {
            spConsentEngine?.clearSession()
            withContext(Dispatchers.Main) { initialiseSDK() }
        }
    }
}
