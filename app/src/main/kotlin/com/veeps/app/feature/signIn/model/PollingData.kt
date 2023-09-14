package com.veeps.app.feature.signIn.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class PollingData(
	@SerializedName("scope") var scope: String = "",
	@SerializedName("expires") var expires: String = "",
	@SerializedName("access_token") var accessToken: String = "",
	@SerializedName("refresh_token") var refreshToken: String = "",
	@SerializedName("token_type") var tokenType: String = "",
	@SerializedName("error") var errorCode: String = "",
	@SerializedName("error_description") var errorMessage: String = "",
)