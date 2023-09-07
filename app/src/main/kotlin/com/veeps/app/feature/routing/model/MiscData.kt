package com.veeps.app.feature.routing.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class MiscData(
	@SerializedName("variableKey") var variableKey: String = "",
	@SerializedName("variableValue") var variableValue: String = ""
)