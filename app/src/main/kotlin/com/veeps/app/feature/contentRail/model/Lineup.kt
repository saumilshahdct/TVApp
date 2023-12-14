package com.veeps.app.feature.contentRail.model

import com.google.gson.annotations.SerializedName

data class Lineup(
	@SerializedName("id") var id: String? = null,
	@SerializedName("name") var name: String? = null,
	@SerializedName("rank") var rank: String? = null,
	@SerializedName("genre") var genre: String? = null,
	@SerializedName("sub_genre") var subGenre: String? = null,
	@SerializedName("landscape_url") var landscapeUrl: String? = null,
	@SerializedName("portrait_url") var portraitUrl: String? = null,
	@SerializedName("logo_url") var logoUrl: String? = null,
	@SerializedName("stage") var stage: String? = null,
)
