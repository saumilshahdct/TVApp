package com.veeps.app.util

object APIConstants {

	private const val STAGE: String =
		"https://rcipovsshb.execute-api.us-west-1.amazonaws.com/stage/"
	private const val PROD: String =
		"https://rcipovsshb.execute-api.us-west-1.amazonaws.com/stage/"

	const val BASE_URL: String = PROD
	private const val API_VERSION: String = "v1/"

	const val fetchContentDetailsFromUplynk: String = "https://content.uplynk.com/" + "{url}"

	const val fetchGuestUserToken: String = API_VERSION + "token/guest"
}