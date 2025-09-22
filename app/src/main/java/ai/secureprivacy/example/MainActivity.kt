package ai.secureprivacy.example

import ai.secureprivacy.example.additional_features.AdditionalFeaturesActivity
import ai.secureprivacy.example.databinding.ActivityMainBinding
import ai.secureprivacy.mobileconsent.consent_engine.SPConsentEngine
import ai.secureprivacy.mobileconsent.data.dto.SPConsentEvent
import ai.secureprivacy.mobileconsent.data.dto.SPDataMessage
import ai.secureprivacy.mobileconsent.listeners.SPConsentEventListener
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat

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
class MainActivity : AppCompatActivity() {

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
    private fun getSelectedApplicationId(): String? =
        when (binding.rgApplicationType.checkedRadioButtonId) {
            binding.rbPrimaryApp.id -> AppConfig.APPLICATION_ID
            else -> AppConfig.SECONDARY_APPLICATION_ID
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
        binding.showPreferenceCenterBtn.setOnClickListener { showPreferenceCenter() }
        binding.checkPackageStatusBtn.setOnClickListener { checkPackageStatus() }
        binding.exploreAdditionalFeaturesBtn.setOnClickListener { exploreAdditionalFeatures() }
        binding.clearSessionBtn.setOnClickListener { clearSession() }


        // Observe SDK consent events
        SPConsentEngine.getConsentEventsData(CONSENT_REQUEST_CODE)
            .observe(this@MainActivity) { event -> fetchConsentStatus() }
    }

    override fun onStart() {
        super.onStart()

        // Register primary consent event listener
        SPConsentEngine.addListener(
            PRIMARY_CONSENT_EVENT_REQUEST_CODE, object : SPConsentEventListener {
                override val applicationId: String = AppConfig.APPLICATION_ID
                override fun onConsentAction(data: SPDataMessage<SPConsentEvent>) {
                    Log.d(TAG, "onConsentAction(${data})")
                }
            })
        if (AppConfig.SECONDARY_APPLICATION_ID != null) {
            // Register secondary consent event listener
            SPConsentEngine.addListener(
                SECONDARY_CONSENT_EVENT_REQUEST_CODE, object : SPConsentEventListener {
                    override val applicationId: String = AppConfig.SECONDARY_APPLICATION_ID
                    override fun onConsentAction(data: SPDataMessage<SPConsentEvent>) {
                        Log.d(TAG, "onConsentAction(${data})")
                    }
                })
        }
    }

    override fun onStop() {
        // Remove consent event listeners
        SPConsentEngine.removeListener(PRIMARY_CONSENT_EVENT_REQUEST_CODE)
        SPConsentEngine.removeListener(SECONDARY_CONSENT_EVENT_REQUEST_CODE)
        super.onStop()
    }

    private fun getSPApplication(): SPApplication {
        return application as SPApplication
    }

    /**
     * Asynchronously initializes the Secure Privacy SDK and updates the UI upon completion.
     */
    private fun initialiseSDK() {
        binding.progressBar.show()
        binding.clearSessionBtn.visibility = View.GONE
        binding.llContent.visibility = View.GONE
        binding.sdkStatusText.text = HtmlCompat.fromHtml(
            "<b>SDK Status</b>: Initialising...", HtmlCompat.FROM_HTML_MODE_COMPACT
        )
        getSPApplication().getSPConsentEngine().observe(this) { result ->
            if (result != null) {
                binding.progressBar.hide()
                binding.clearSessionBtn.visibility = View.VISIBLE
                if (result.code == 200 && result.data != null) {
                    spConsentEngine = result.data
                    binding.sdkStatusText.text = HtmlCompat.fromHtml(
                        "<b>SDK Status</b>: Initialised!", HtmlCompat.FROM_HTML_MODE_COMPACT
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
        val resultPrimary = spConsentEngine?.getClientId(AppConfig.APPLICATION_ID)
        val primaryLocale = SPConsentEngine.getLocale(AppConfig.APPLICATION_ID).data ?: ""
        binding.rbPrimaryApp.text = HtmlCompat.fromHtml(
            "<b>Primary</b>:<br>($primaryLocale) ${resultPrimary?.data ?: resultPrimary?.msg}",
            HtmlCompat.FROM_HTML_MODE_COMPACT
        )
        if (AppConfig.SECONDARY_APPLICATION_ID != null) {
            val resultSecondary = spConsentEngine?.getClientId(AppConfig.SECONDARY_APPLICATION_ID)
            val secondaryLocale = SPConsentEngine.getLocale(AppConfig.APPLICATION_ID).data ?: ""
            binding.rbSecondaryApp.text = HtmlCompat.fromHtml(
                "<b>Secondary</b>:<br>($secondaryLocale) ${resultSecondary?.data ?: resultSecondary?.msg}",
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
        } else {
            binding.rbSecondaryApp.visibility = View.GONE
        }
    }

    /**
     * Fetches and updates the consent status for the selected application.
     */
    private fun fetchConsentStatus() {
        val appId = getSelectedApplicationId() ?: return
        val status = spConsentEngine?.getConsentStatus(appId)
        binding.consentStatusText.text = HtmlCompat.fromHtml(
            "<b>Consent Status</b>: ${
                if (status?.code == 200) status.data else status?.msg
            }", HtmlCompat.FROM_HTML_MODE_COMPACT
        )
    }

    /**
     * Displays the appropriate consent banner based on the selected application type.
     */
    private fun showConsentBanner() {
        spConsentEngine?.let {
            when (binding.rgApplicationType.checkedRadioButtonId) {
                binding.rbPrimaryApp.id -> {
                    val result = it.showConsentBanner(this)
                    if (result.code != 200) {
                        Toast.makeText(this, "$result", Toast.LENGTH_SHORT).show()
                    }
                }

                else -> {
                    val result = it.showSecondaryBanner(this)
                    if (result.code != 200) {
                        Toast.makeText(this, "$result", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /**
     * Displays the appropriate preference center based on the selected application type.
     */
    private fun showPreferenceCenter() {
        spConsentEngine?.let {
            when (binding.rgApplicationType.checkedRadioButtonId) {
                binding.rbPrimaryApp.id -> {
                    val result = it.showPreferenceCenter(this, AppConfig.APPLICATION_ID)
                    if (result.code != 200) {
                        Toast.makeText(this, "$result", Toast.LENGTH_SHORT).show()
                    }
                }

                else -> {
                    if (AppConfig.SECONDARY_APPLICATION_ID != null) {
                        val result =
                            it.showPreferenceCenter(this, AppConfig.SECONDARY_APPLICATION_ID)
                        if (result.code != 200) {
                            Toast.makeText(this, "$result", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
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
        val appId = getSelectedApplicationId() ?: return
        val pkg = spConsentEngine?.getPackage(packageId, appId)?.data

        pkg?.let {
            // pkg is not null here, 'it' refers to pkg
            binding.packageStatusText.text = HtmlCompat.fromHtml(
                "<b>Package status</b>: ${if (it.isEnabled) "Enabled" else "Disabled"}!",
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
        } ?: run {
            // pkg is null here
            binding.packageStatusText.text = HtmlCompat.fromHtml(
                "<b>Package status</b>: Not found!",
                HtmlCompat.FROM_HTML_MODE_COMPACT
            )
        }
    }

    /**
     * Placeholder for exploring additional features.
     * This can be expanded to include more functionalities as needed.
     */
    private fun exploreAdditionalFeatures() {
        startActivity(Intent(this, AdditionalFeaturesActivity::class.java))
    }

    /**
     * Clears the local session data and re-initializes the SDK.
     */
    private fun clearSession() {
        getSPApplication().resetSPSession()
        initialiseSDK()
    }
}