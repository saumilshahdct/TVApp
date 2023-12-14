package com.veeps.app.feature.video.model

import com.google.gson.annotations.SerializedName
import com.veeps.app.feature.contentRail.model.UserStats

data class StoryBoardImages(
	@SerializedName("url") var url: String = "",
	@SerializedName("tile_width") var tileWidth: Int = 0,
	@SerializedName("tile_height") var tileHeight: Int = 0,
	@SerializedName("duration") var duration: Float = 0F,
	@SerializedName("tiles") var tiles: ArrayList<StoryBoardImagePosition> = arrayListOf(),
)
