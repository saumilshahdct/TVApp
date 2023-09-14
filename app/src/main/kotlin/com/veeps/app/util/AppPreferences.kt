package com.veeps.app.util

import android.content.Context
import android.content.SharedPreferences
import com.veeps.app.application.Veeps

object AppPreferences {
	private lateinit var sharedPreference: SharedPreferences

	private fun open() {
		if (!this::sharedPreference.isInitialized) sharedPreference =
			Veeps.appContext.getSharedPreferences(AppConstants.PREFS_FILENAME, Context.MODE_PRIVATE)
	}

	fun set(key: String?, value: String?) {
		open()
		sharedPreference.edit().putString(key, value).apply()
	}

	fun set(key: String?, value: Int) {
		open()
		sharedPreference.edit().putInt(key, value).apply()
	}

	fun set(key: String?, value: Boolean) {
		open()
		sharedPreference.edit().putBoolean(key, value).apply()
	}

	fun get(key: String?, value: String?): String? {
		open()
		return sharedPreference.getString(key, value)
	}

	fun get(key: String?, value: Int): Int {
		open()
		return sharedPreference.getInt(key, value)
	}

	fun get(key: String?, value: Boolean): Boolean {
		open()
		return sharedPreference.getBoolean(key, value)
	}

	fun remove(key: String?) {
		open()
		sharedPreference.edit().remove(key).apply()
	}

	fun removeAuthenticatedUser() {
		open()
		remove(AppConstants.isUserAuthenticated)
		remove(AppConstants.userID)
		remove(AppConstants.userEmailAddress)
		remove(AppConstants.userFullName)
		remove(AppConstants.userDisplayName)
		remove(AppConstants.userAvatar)
		remove(AppConstants.userSubscriptionStatus)
		remove(AppConstants.userCurrency)
		remove(AppConstants.userTimeZone)
		remove(AppConstants.userTimeZoneAbbr)
		remove(AppConstants.userBeaconBaseURL)
		remove(AppConstants.authenticatedUserToken)
		Logger.print("Authenticated User Details Are Removed.")
	}
}