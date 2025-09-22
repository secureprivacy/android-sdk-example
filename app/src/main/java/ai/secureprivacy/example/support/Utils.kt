package ai.secureprivacy.example.support

import ai.secureprivacy.mobileconsent.consent_engine.SPConsentEngine
import ai.secureprivacy.mobileconsent.data.enums.SPSupportedPackage
import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.HtmlCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utils {
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun currentDateTime(): String = sdf.format(Date())

    fun submitEvent(
        context: Context,
        engine: SPConsentEngine,
        applicationId: String,
        supportedPackage: SPSupportedPackage,
        etEventMessage: EditText,
        tvConsoleLabel: TextView,
        tvConsole: TextView,
        consoleMsgBuilder: StringBuilder,
        svConsole: ScrollView,
        submitEvent: (event: String) -> Boolean,
    ) {
        val eventMsg = etEventMessage.text.toString().trim()
        if (eventMsg.isEmpty()) {
            Toast.makeText(context, "Input event message first!", Toast.LENGTH_SHORT).show()
            return
        }

        val result =
            engine.getPackage(applicationId = applicationId, packageId = supportedPackage.packageId)

        val pkg = result.data
        if (pkg == null) {
            Toast.makeText(context, "$supportedPackage - Consent not found", Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (!pkg.isEnabled) {
            Toast.makeText(context, "$supportedPackage package is disabled", Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (!submitEvent(eventMsg)) {
            return
        }

        tvConsoleLabel.visibility = View.VISIBLE
        consoleMsgBuilder.append("<br>")
        consoleMsgBuilder.append(currentDateTime())
        consoleMsgBuilder.append(" <b>$eventMsg</b>")
        tvConsole.text =
            HtmlCompat.fromHtml(consoleMsgBuilder.toString(), HtmlCompat.FROM_HTML_MODE_COMPACT)

        etEventMessage.text.clear()
        svConsole.post { svConsole.fullScroll(View.FOCUS_DOWN) }
        Toast.makeText(context, "Submitted Successfully!", Toast.LENGTH_SHORT).show()
    }
}

