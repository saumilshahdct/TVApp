package com.veeps.app.feature.contentRail.model

import com.google.gson.annotations.SerializedName

data class ProductsResponse(
	@SerializedName("data") var products: ArrayList<Products> = arrayListOf(),
)
