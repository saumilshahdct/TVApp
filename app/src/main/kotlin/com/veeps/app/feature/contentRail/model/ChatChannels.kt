package com.veeps.app.feature.contentRail.model

import com.google.gson.annotations.SerializedName

data class ChatChannels(
	@SerializedName("user") var user: String? = null,
	@SerializedName("artist") var artist: String? = null,
	@SerializedName("signals") var signals: String? = null,
	@SerializedName("main_publish") var mainPublish: String? = null,
	@SerializedName("main_subscribe") var mainSubscribe: String? = null,
)
