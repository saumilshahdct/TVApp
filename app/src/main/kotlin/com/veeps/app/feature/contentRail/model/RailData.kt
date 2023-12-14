package com.veeps.app.feature.contentRail.model

import com.google.gson.annotations.SerializedName

data class RailData(
	@SerializedName("id") var id: String? = null,
	@SerializedName("name") var name: String? = null,
	@SerializedName("description") var description: String? = null,
	@SerializedName("entities") var entities: ArrayList<Entities> = arrayListOf(),
	@SerializedName("data") var data: ArrayList<Entities> = arrayListOf(),
	@SerializedName("card_type") var cardType: String? = null,
	@SerializedName("entities_type") var entitiesType: String? = null,
	var isContinueWatching: Boolean = false,
	var userStats: ArrayList<UserStats> = arrayListOf(),
	var isWatchList: Boolean = false,
	var isExpired: Boolean = false,
)
