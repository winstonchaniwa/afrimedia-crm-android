package com.afrimedia.crm.data.remote

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Holds the site URL and WordPress "Application Password" credentials the
 * app authenticates with. Application Passwords are a built-in WordPress
 * feature (Users > Profile > Application Passwords, WP 5.6+) — no custom
 * auth plugin needed on the CRM side. Stored in EncryptedSharedPreferences
 * so the raw password never sits in plain text on the device.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        prefs = EncryptedSharedPreferences.create(
            context,
            "amcrm_session",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    var siteUrl: String
        get() = prefs.getString(KEY_SITE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_SITE, value.trimEnd('/')).apply()

    var username: String
        get() = prefs.getString(KEY_USER, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER, value).apply()

    var appPassword: String
        get() = prefs.getString(KEY_PASS, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PASS, value).apply()

    val isLoggedIn: Boolean
        get() = siteUrl.isNotBlank() && username.isNotBlank() && appPassword.isNotBlank()

    fun saveCredentials(siteUrl: String, username: String, appPassword: String) {
        this.siteUrl = siteUrl
        this.username = username
        this.appPassword = appPassword
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_SITE = "site_url"
        private const val KEY_USER = "username"
        private const val KEY_PASS = "app_password"
    }
}
