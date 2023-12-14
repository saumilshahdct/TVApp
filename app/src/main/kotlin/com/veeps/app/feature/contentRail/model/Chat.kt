package com.veeps.app.feature.contentRail.model

import com.google.gson.annotations.SerializedName

data class Chat(
	@SerializedName("enabled") var enabled: Boolean = false,
	@SerializedName("starts_at") var startsAt: String? = null,
	@SerializedName("channels") var chatChannels: ChatChannels = ChatChannels(),
)
