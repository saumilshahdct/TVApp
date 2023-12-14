package com.veeps.app.feature.venue.model

import com.google.gson.annotations.SerializedName

data class VenueResponse(
	@SerializedName("data") var data: VenueData? = VenueData(),
)
