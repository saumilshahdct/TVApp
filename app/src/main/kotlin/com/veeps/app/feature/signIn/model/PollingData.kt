package com.veeps.app.feature.signIn.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import com.veeps.app.util.DEFAULT

@Keep
data class PollingData(
	@SerializedName("scope") var scope: String = DEFAULT.EMPTY_STRING,
	@SerializedName("expires") var expires: String = DEFAULT.EMPTY_STRING,
	@SerializedName("access_token") var accessToken: String = DEFAULT.EMPTY_STRING,
	@SerializedName("refresh_token") var refreshToken: String = DEFAULT.EMPTY_STRING,
	@SerializedName("token_type") var tokenType: String = DEFAULT.EMPTY_STRING,
	@SerializedName("error") var errorCode: String = DEFAULT.EMPTY_STRING,
	@SerializedName("error_description") var errorMessage: String = DEFAULT.EMPTY_STRING,
)