package com.veeps.app.feature.search.model

import com.google.gson.annotations.SerializedName
import com.veeps.app.feature.contentRail.model.Entities

data class SearchData(
	@SerializedName("events") var events: ArrayList<Entities> = arrayListOf(),
	@SerializedName("artists") var artists: ArrayList<Entities> = arrayListOf(),
	@SerializedName("matched_genres") var matchedGenres: ArrayList<String> = arrayListOf(),
)
