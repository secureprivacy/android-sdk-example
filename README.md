# Secure Privacy Android SDK – Example App

## Overview

This repository demonstrates how to integrate the **Secure Privacy Consent Management SDK** into an Android application. It provides a working implementation of the SDK, showcasing essential features such as consent collection, status retrieval, and event handling.

For full SDK documentation, visit:  
[Secure Privacy Android SDK Documentation](https://docs.secureprivacy.ai/guides/mobile/android-sdk/)  
[Secure Privacy Website](https://secureprivacy.ai)

## Getting Started

### Obtain Your Application ID

To use the Secure Privacy SDK, you need an **Application ID**.  
Sign up for a **free trial** at [Secure Privacy](https://secureprivacy.ai) to get your **Application ID**.

### Installation

1. Add the SDK dependency to your `build.gradle`:

   ```gradle
   dependencies {
       implementation("ai.secureprivacy.sdk:mobileConsent:0.2.4-alpha")
   }
   ```

2. Sync your project in Android Studio.

### Running the Example App

1. Clone this repository:

   ```sh
   git clone https://github.com/secureprivacy/android-sdk-example.git
   cd android-sdk-example
   ```

2. Open the project in Android Studio.
3. Build and run the app on an emulator or physical device.

## Features

- Consent Collection: Display primary and secondary consent banners.
- Consent Status Management: Retrieve and check user consent state.
- Package-Specific Consent: Verify if a specific package has consent.
- Event Listening: Track real-time consent changes.
- Session Management: Reset consent session when needed.

## Usage

### Initialize the SDK

Call the following in your main activity or application startup:

```kotlin
val spConsentEngine = SPConsentEngine.initialise(
    activity, SPAuthKey(
        applicationId = "YOUR_APPLICATION_ID"
    )
)
```

If you want to use a **secondary application ID** for displaying an additional consent banner, include it as follows:

```kotlin
val spConsentEngine = SPConsentEngine.initialise(
    activity, SPAuthKey(
        applicationId = "YOUR_APPLICATION_ID",
        secondaryApplicationId = "YOUR_SECONDARY_APPLICATION_ID" // Optional
    )
)
```

This returns an instance of `SPConsentEngine` that allows you to manage user consent.

### Display the Consent Banner

To show the **primary consent banner**:

```kotlin
ConsentBanner.show(activity)
```

To show the **secondary consent banner** (if applicable):

```kotlin
ConsentBanner.showSecondary(activity)
```

### Retrieve Consent Status

```kotlin
val consentStatus = spConsentEngine.getConsentStatus("YOUR_APPLICATION_ID")
```

Possible consent states:

- **Collected** – Consent has been obtained.
- **Pending** – Consent is required.
- **UpdateRequired** – Consent needs to be refreshed.

### Check Package Consent

To verify if a package is enabled:

```kotlin
val packageData = spConsentEngine?.getPackage(packageId, "YOUR_APPLICATION_ID")?.data
if (packageData?.isEnabled == true) {
    // Enable features
}
```

### Listen to Consent Events

Register an event listener in `onStart`:

```kotlin
override fun onStart() {
    super.onStart()
    SPConsentEngine.addListener(
        MOBILE_CONSENT_EVENT_CODE,
        object : SPConsentEventListener {
            override fun onConsentAction(data: SPDataMessage<SPConsentEvent>) {
                // Handle consent updates
            }
        }
    )
}
```

Remove the listener in `onStop`:

```kotlin
override fun onStop() {
    SPConsentEngine.removeListener(MOBILE_CONSENT_EVENT_CODE)
    super.onStop()
}
```

Alternatively, observe events using LiveData:

```kotlin
SPConsentEngine.getConsentEventsData(EVENT_CODE).observe(activity) { eventData ->
    // Handle consent changes
}
```

### Clear the Consent Session

To clear local consent data:

```kotlin
spConsentEngine.clearSession()
```

This is useful for logout scenarios or resetting consent preferences.

### Get Unique Client ID

Retrieve the user’s unique client ID:

```kotlin
val clientId = spConsentEngine.getClientId("YOUR_APPLICATION_ID")
```

This can be used for backend tracking of consent states.

## Customization

The Secure Privacy SDK allows customization of consent banners and the **Preference Center**, where users can manage their consent preferences.

To open the **Preference Center** directly:

```kotlin
SPPreferenceCenter.show(context, "YOUR_APPLICATION_ID")
```

Ensure the **Preference Center Activity** is declared in `AndroidManifest.xml`:

```xml
<activity
    android:name="ai.secureprivacy.mobileconsent.ui.preference_center.SPPreferenceCenter"
    android:theme="@style/Theme.SecurePrivacyMobile"
/>
```

## Support

For detailed documentation and additional guidance, visit:

[Secure Privacy Android SDK Documentation](https://docs.secureprivacy.ai/guides/mobile/android-sdk/)

If you encounter any issues, reach out via our website:

[Secure Privacy](https://secureprivacy.ai)

## License

This project is licensed under the MIT License.
