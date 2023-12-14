package com.veeps.app.feature.event.model

import com.google.gson.annotations.SerializedName
import com.veeps.app.util.DEFAULT

data class OrderGeneration(
	@SerializedName("id") var id: String = DEFAULT.EMPTY_STRING,
	@SerializedName("items") var items: ArrayList<OrderItems> = arrayListOf(),
	@SerializedName("item_id") var itemId: String = DEFAULT.EMPTY_STRING,
	@SerializedName("currency") var currency: String = DEFAULT.EMPTY_STRING,
	@SerializedName("expires_at") var expiresAt: String = DEFAULT.EMPTY_STRING,
	@SerializedName("shipping_amount") var shippingAmount: String = DEFAULT.EMPTY_STRING,
	@SerializedName("service_fee_amount") var serviceFeeAmount: String = DEFAULT.EMPTY_STRING,
	@SerializedName("subtotal_amount") var subtotalAmount: String = DEFAULT.EMPTY_STRING,
	@SerializedName("tax_amount") var taxAmount: String = DEFAULT.EMPTY_STRING,
	@SerializedName("total_amount") var totalAmount: String = DEFAULT.EMPTY_STRING,
	@SerializedName("display_service_fee_amount") var displayServiceFeeAmount: String = DEFAULT.EMPTY_STRING,
	@SerializedName("display_shipping_amount") var displayShippingAmount: String = DEFAULT.EMPTY_STRING,
	@SerializedName("display_subtotal_amount") var displaySubtotalAmount: String = DEFAULT.EMPTY_STRING,
	@SerializedName("display_tax_amount") var displayTaxAmount: String = DEFAULT.EMPTY_STRING,
	@SerializedName("display_total_amount") var displayTotalAmount: String = DEFAULT.EMPTY_STRING,
	@SerializedName("primer") var primer: String = DEFAULT.EMPTY_STRING,
)
