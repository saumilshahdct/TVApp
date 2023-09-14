package com.veeps.app.feature.signIn.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SignInData(
	@SerializedName("interval") var pollingInterval: Int = 0,
	@SerializedName("expires_in") var tokenExpiryTime: Int = 0,
	@SerializedName("device_code") var deviceCode: String = "",
	@SerializedName("user_code") var authenticationCode: String = "",
	@SerializedName("verification_uri") var authenticationLoginURL: String = "",
	@SerializedName("error") var errorCode: String = "",
	@SerializedName("error_description") var errorMessage: String = "",
)