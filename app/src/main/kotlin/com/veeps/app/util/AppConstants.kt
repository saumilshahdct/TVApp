package com.veeps.app.util

object AppConstants {
	const val PREFS_FILENAME: String = "veeps"
	const val TAG: String = "VeepsAppTag"
	const val deviceType: String = "fireTv"
	const val deviceName: String = "Amazon Fire TV"

	const val clientId: String = "40a3903ae06585f0c114464750d605bba618afd01159f4eefef68c255c746a89"
	const val clientSecret: String =
		"0db660fe553f3fe5fba3f0c7270a7a20c31ef49f27167bbde9ce905777a5aaac"
	const val grantType: String = "urn:ietf:params:oauth:grant-type:device_code"
	const val JWTSecretKey_Production: String =
		"27aSEvmPBBRQFrYsQcKJn8rJ0J0mBiiCZGeiZRNqV1Q/dJ7/rrXlbTxHHGJn9cNl"
	const val JWTSecretKey_Staging: String =
		"xSISQv6jnL4lPgibQPrLdZWgtqYynARhSKKLiLK7ZjKrEA/i7x+MoEHTytVcnjP2"

	const val deviceModel: String = "device_model"
	const val deviceUniqueID: String = "device_unique_id"

	var lastKeyPressTime: Long = IntValue.NUMBER_0.toLong()
	const val keyPressLongDelayTime: Long = IntValue.NUMBER_300.toLong()
	const val keyPressShortDelayTime: Long = IntValue.NUMBER_100.toLong()

	const val isUserAuthenticated: String = "is_user_authenticated"
	const val authenticatedUserToken: String = "authenticated_user_token"
	const val userID: String = "user_id"
	const val userSubscriptionStatus: String = "user_subscription_status"
	const val userCurrency: String = "user_currency"
	const val userTimeZone: String = "user_time_zone"
	const val userBeaconBaseURL: String = "user_beacon_base_url"
	const val userEmailAddress: String = "user_email_address"
	const val userFullName: String = "user_full_name"
	const val userDisplayName: String = "user_display_name"
	const val userAvatar: String = "user_avatar"
	const val userTimeZoneAbbr: String = "user_time_zone_abbr"
}

object Screens {
	const val INTRO = "intro"
	const val SIGN_IN = "signIn"
	const val BROWSE = "BROWSE"
	const val EXIT_APP = "EXIT_APP"
}

object ImageTags {
	const val DEFAULT = "DEFAULT"
	const val QR = "QR"
	const val HEADER = "HEADER"
	const val ROUNDED = "ROUNDED"
}

object IntValue {
	const val NUMBER_0 = 0
	const val NUMBER_1 = 1
	const val NUMBER_5 = 5
	const val NUMBER_10 = 10
	const val NUMBER_100 = 100
	const val NUMBER_300 = 300
	const val NUMBER_333 = 333
	const val NUMBER_1000 = 1000
	const val NUMBER_2000 = 2000
	const val NUMBER_5000 = 5000
}

object PollingStatus {
	const val PENDING = "authorization_pending"
	const val SLOW_DOWN = "slow_down"
	const val EXPIRED_TOKEN = "expired_token"
}

object DEFAULT {
	const val EMPTY_STRING = ""
	const val EMPTY_INT = 0
}