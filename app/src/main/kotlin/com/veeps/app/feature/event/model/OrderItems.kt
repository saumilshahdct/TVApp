package com.veeps.app.feature.event.model

import com.google.gson.annotations.SerializedName

data class OrderItems(
	@SerializedName("name") var name: String = "",
	@SerializedName("quantity") var quantity: Int = 0,
	@SerializedName("product_id") var productId: String = "",
	@SerializedName("display_price") var displayPrice: String = "",
	@SerializedName("image_url") var imageUrl: String = "",
)
