package ai.secureprivacy.example.additional_features

import ai.secureprivacy.example.databinding.ActivityAdditionalFeaturesBinding
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class AdditionalFeaturesActivity : ComponentActivity() {

    companion object {
        private const val TAG = "AdditionalFeaturesActivity"
    }

    private val binding by lazy {
        ActivityAdditionalFeaturesBinding.inflate(layoutInflater, null, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.customAppBar.ibBack.setOnClickListener { finish() }
        binding.customAppBar.tvTitle.text = "Demo - Additional Features"
        binding.firebaseAnalyticsBtn.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    FirebaseAnalyticsExampleActivity::class.java
                )
            )
        }
        binding.singularBtn.setOnClickListener {
            startActivity(Intent(this, SingularExampleActivity::class.java))
        }
        binding.tenjinBtn.setOnClickListener {
            startActivity(Intent(this, TenjinExampleActivity::class.java))
        }
        binding.airbridgeBtn.setOnClickListener {
            startActivity(Intent(this, AirbridgeExampleActivity::class.java))
        }
    }
}