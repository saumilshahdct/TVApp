package com.veeps.app.feature.contentRail.model

import com.google.gson.annotations.SerializedName

data class UserStats(
	@SerializedName("cursor") var cursor: Double = 0.0,
	@SerializedName("duration") var duration: Double = 0.0,
	@SerializedName("eventId") var eventId: String? = null,
)
