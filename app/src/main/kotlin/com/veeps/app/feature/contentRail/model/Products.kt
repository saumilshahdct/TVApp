package com.veeps.app.feature.contentRail.model

import com.google.gson.annotations.SerializedName

data class Products(
	@SerializedName("id") var id: String? = null,
	@SerializedName("name") var name: String? = null,
	@SerializedName("description") var description: String? = null,
	@SerializedName("currency") var currency: String? = null,
	@SerializedName("availability") var availability: String? = null,
	@SerializedName("price") var price: String? = null,
	@SerializedName("image_url") var imageUrl: String? = null,
	@SerializedName("product_type") var productType: String? = null,
	@SerializedName("bundle_id") var bundleId: String? = null,
	@SerializedName("display_price") var displayPrice: String? = null,
	@SerializedName("bundle_name") var bundleName: String? = null,
	@SerializedName("product_code") var productCode: String? = null,
)
