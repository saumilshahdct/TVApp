package com.veeps.app.util

object APIConstants {

	private const val STAGE: String = "https://api.veeps-team.com/"
	private const val PROD: String = "https://api.veeps.com/"
	private const val AUTH_PROD: String = "https://veeps.com/"
	private const val PLAYER_STAT_STAGE: String = "https://vstats.veeps-team.com"
	private const val PLAYER_STAT_PROD: String = "https://vstats.veeps.com"

	const val BASE_URL: String = PROD
	private const val API_VERSION: String = "v3/"
	private const val AUTH_VERSION: String = AUTH_PROD + "oauth/"

	const val QR_CODE_BASE_URL: String =
		"https://veeps.com/qr-code?i=https://veeps.com/activate?code="

	const val fetchAuthenticationDetails: String = AUTH_VERSION + "authorize_device"
	const val authenticationPolling: String = AUTH_VERSION + "token"
	const val fetchUserDetails: String = API_VERSION + "me"
}