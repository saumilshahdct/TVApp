package com.veeps.app.feature.contentRail.model

import com.google.gson.annotations.SerializedName

data class Storyboards(
	@SerializedName("json") var json: String? = null,
	@SerializedName("jpg") var jpg: String? = null,
)
