package com.veeps.app.feature.user.model

import com.google.gson.annotations.SerializedName

data class UserData(
	@SerializedName("id") var id: String = "",
	@SerializedName("sub") var subscriptionStatus: String = "",
	@SerializedName("currency") var currency: String = "",
	@SerializedName("time_zone") var timeZone: String = "",
	@SerializedName("beacon_base_url") var beaconBaseURL: String = "",
	@SerializedName("email") var email: String = "",
	@SerializedName("full_name") var fullName: String = "",
	@SerializedName("display_name") var displayName: String = "",
	@SerializedName("avatar_url") var avatarURL: String = "",
	@SerializedName("time_zone_abbr") var timeZoneAbbr: String = "",
)
