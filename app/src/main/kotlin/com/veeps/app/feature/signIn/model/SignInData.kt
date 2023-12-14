package com.veeps.app.feature.signIn.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.veeps.app.util.DEFAULT

@Keep
data class SignInData(
	@SerializedName("interval") var pollingInterval: Int = 0,
	@SerializedName("expires_in") var tokenExpiryTime: Int = 0,
	@SerializedName("device_code") var deviceCode: String = DEFAULT.EMPTY_STRING,
	@SerializedName("user_code") var authenticationCode: String = DEFAULT.EMPTY_STRING,
	@SerializedName("verification_uri") var authenticationLoginURL: String = DEFAULT.EMPTY_STRING,
	@SerializedName("error") var errorCode: String = DEFAULT.EMPTY_STRING,
	@SerializedName("error_description") var errorMessage: String = DEFAULT.EMPTY_STRING,
)