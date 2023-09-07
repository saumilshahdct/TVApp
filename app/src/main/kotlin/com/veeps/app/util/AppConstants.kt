package com.veeps.app.util

object AppConstants {
	const val PREFS_FILENAME: String = "veeps"
	const val TAG: String = "VeepsAppTag"
	const val deviceType: String = "fireTv"
	const val deviceName: String = "Amazon Fire TV"

	const val deviceModel: String = "device_model"
	const val deviceUniqueID: String = "device_unique_id"

	var lastKeyPressTime: Long = 0
	const val keyPressLongDelayTime: Long = 300
	const val keyPressShortDelayTime: Long = 100

	const val isUserAuthenticated: String = "is_user_authenticated"
	const val userAuthenticatedWith: String = "user_authenticated_with"
	const val userID: String = "user_id"
	const val userEmailAddress: String = "user_email_address"
	const val userFullName: String = "user_full_name"
	const val userFirstName: String = "user_first_name"
	const val userLastName: String = "user_last_name"
	const val userDateOfBirth: String = "user_date_of_birth"
	const val userAvatar: String = "user_avatar"

	const val authenticatedUserToken: String = "authenticated_user_token"
}