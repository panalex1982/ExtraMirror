package com.bue.extramirror

import android.app.Application
import com.bue.extramirror.ads.AdsConsentManager
import com.bue.extramirror.data.SettingsRepository
import com.bue.extramirror.data.settingsDataStore
import kotlinx.coroutines.MainScope

class ExtraMirrorApplication : Application() {

    private val appScope = MainScope()

    val settingsRepository by lazy { SettingsRepository(settingsDataStore) }

    val adsConsentManager by lazy { AdsConsentManager(this, appScope) }
}
