package ai.secureprivacy.example

import ai.secureprivacy.mobileconsent.consent_engine.SPConsentEngine
import ai.secureprivacy.mobileconsent.data.dto.SPAirbridgeConfig
import ai.secureprivacy.mobileconsent.data.dto.SPAuthKey
import ai.secureprivacy.mobileconsent.data.dto.SPDataMessage
import ai.secureprivacy.mobileconsent.data.dto.SPSingularConfig
import ai.secureprivacy.mobileconsent.data.dto.SPTenjinConfig
import ai.secureprivacy.mobileconsent.support.SPLogger
import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import co.ab180.airbridge.Airbridge
import co.ab180.airbridge.AirbridgeLogLevel
import co.ab180.airbridge.AirbridgeOptionBuilder
import com.singular.sdk.Singular
import com.singular.sdk.SingularConfig
import com.tenjin.android.TenjinSDK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SPApplication : Application() {

    private val _spConsentEngine = MutableLiveData<SPDataMessage<SPConsentEngine?>?>()
    private var spConsentEngine: SPConsentEngine? = null

    override fun onCreate() {
        super.onCreate()
        initialiseConsentEngine()
    }

    private fun initialiseConsentEngine() {
        _spConsentEngine.value = null
        spConsentEngine = null
        CoroutineScope(Dispatchers.IO).launch {
            val result = SPConsentEngine.initialise(
                this@SPApplication, SPAuthKey(
                    applicationId = AppConfig.APPLICATION_ID,
                    secondaryApplicationId = AppConfig.SECONDARY_APPLICATION_ID,
                    integrationConfigs = listOf(
                        SPSingularConfig(AppConfig.SINGULAR_API_KEY, AppConfig.SINGULAR_APP_SECRET),
                        SPTenjinConfig(apiKey = AppConfig.TENJIN_KEY),
                        SPAirbridgeConfig(AppConfig.AIRBRIDGE_APP, AppConfig.AIRBRIDGE_TOKEN)
                    )
                )
            )
            spConsentEngine = result.data
            val locale = SPConsentEngine.getLocale(AppConfig.APPLICATION_ID)
            if (locale.code == 200) {
                SPLogger.i(TAG, "Locale : ${locale.data}")
            } else {
                SPLogger.e(TAG, "Locale : $locale", Exception(locale.error))
            }
            _spConsentEngine.postValue(result)
        }
    }

    fun getSPConsentEngine(): LiveData<SPDataMessage<SPConsentEngine?>?> = _spConsentEngine

    fun resetSPSession() {
        _spConsentEngine.value = null
        SPConsentEngine.clearSession()
        initialiseConsentEngine()
    }

    companion object {
        private const val TAG = "SPApplication.kt"
    }
}