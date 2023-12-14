package com.veeps.app.feature.venue.model

import com.google.gson.annotations.SerializedName

data class VenueData(
	@SerializedName("id") var id: String? = null,
	@SerializedName("name") var name: String? = null,
	@SerializedName("url") var url: String? = null,
	@SerializedName("genre") var genre: String? = null,
	@SerializedName("sub_genre") var subGenre: String? = null,
	@SerializedName("landscape_url") var landscapeUrl: String? = null,
	@SerializedName("portrait_url") var portraitUrl: String? = null,
	@SerializedName("logo_url") var logoUrl: String? = null,
	@SerializedName("bio") var bio: String? = null,
)
