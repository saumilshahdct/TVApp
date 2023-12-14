package com.veeps.app.feature.search.model

import com.google.gson.annotations.SerializedName

data class SearchResponse(
	@SerializedName("data") var data: SearchData? = SearchData(),
)
