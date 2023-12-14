package com.veeps.app.feature.contentRail.model

import com.google.gson.annotations.SerializedName

data class UserStatsResponse(
	@SerializedName("data") var userStats: ArrayList<UserStats> = arrayListOf(),
)
