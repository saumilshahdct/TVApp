package com.veeps.app.feature.event.model

import com.google.gson.annotations.SerializedName
import com.veeps.app.util.DEFAULT

data class Reservation(
	@SerializedName("id") var id: String = DEFAULT.EMPTY_STRING,
	@SerializedName("expires_at") var expiresAt: String = DEFAULT.EMPTY_STRING,
	@SerializedName("item_id") var itemId: String = DEFAULT.EMPTY_STRING,
)
