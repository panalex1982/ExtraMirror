package com.bue.extramirror.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Gathers ad consent through Google's User Messaging Platform before any ad is requested.
 *
 * The legacy AdMob 6.x SDK requested ads without asking. Google now requires a certified
 * consent management platform for users in the EEA, the UK and Switzerland, and UMP also
 * covers the US state privacy regulation messages configured in the AdMob console.
 */
class AdsConsentManager(context: Context, private val scope: CoroutineScope) {

    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context.applicationContext)
    private val mobileAdsStarted = AtomicBoolean(false)

    private val _canShowAds = MutableStateFlow(false)
    /** True once consent allows ad requests and the Mobile Ads SDK is initialized. */
    val canShowAds: StateFlow<Boolean> = _canShowAds.asStateFlow()

    private val _privacyOptionsRequired = MutableStateFlow(false)
    /** Whether a "Privacy choices" entry point must be offered (required by UMP). */
    val privacyOptionsRequired: StateFlow<Boolean> = _privacyOptionsRequired.asStateFlow()

    /** Call on every app start, from the launcher activity. */
    fun gatherConsent(activity: Activity) {
        val params = ConsentRequestParameters.Builder().build()
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    formError?.let(::logFormError)
                    onConsentUpdated(activity)
                }
            },
            { requestError ->
                logFormError(requestError)
                onConsentUpdated(activity)
            },
        )
        // Consent obtained in a previous session can be used straight away.
        if (consentInformation.canRequestAds()) startMobileAds(activity)
    }

    fun showPrivacyOptionsForm(activity: Activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            formError?.let(::logFormError)
            onConsentUpdated(activity)
        }
    }

    private fun onConsentUpdated(context: Context) {
        _privacyOptionsRequired.value = consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
        if (consentInformation.canRequestAds()) startMobileAds(context)
    }

    private fun startMobileAds(context: Context) {
        if (!mobileAdsStarted.compareAndSet(false, true)) return
        val appContext = context.applicationContext
        // Google recommends initializing the SDK off the main thread.
        scope.launch(Dispatchers.IO) {
            MobileAds.initialize(appContext) { _canShowAds.value = true }
        }
    }

    private fun logFormError(error: FormError) {
        Log.w(TAG, "Consent error ${error.errorCode}: ${error.message}")
    }

    private companion object {
        const val TAG = "AdsConsent"
    }
}
