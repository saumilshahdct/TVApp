package com.veeps.app.feature.video.model

import androidx.media3.common.TrackGroup
import com.veeps.app.util.DEFAULT

data class Subtitle(val id: String = DEFAULT.EMPTY_STRING, val language: String = DEFAULT.EMPTY_STRING, val label: String = DEFAULT.EMPTY_STRING, val mediaGroup: TrackGroup, val trackPosition: Int) {
	override fun toString(): String {
		return "Subtitle Details -> id: $id, language: $language, label: $label"
	}
}