package com.veeps.app.feature.artist.model

import com.google.gson.annotations.SerializedName

data class ArtistResponse(
	@SerializedName("data") var data: ArtistData? = ArtistData(),
)
