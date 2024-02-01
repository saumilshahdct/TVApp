package com.veeps.app.feature.contentRail.model

import com.veeps.app.util.DEFAULT

data class ChatMessageItem(
	var name: String = DEFAULT.EMPTY_STRING,
	var message: String = DEFAULT.EMPTY_STRING,
	var isArtist: Boolean = false,
	var timeToken: String = DEFAULT.EMPTY_STRING,
)
