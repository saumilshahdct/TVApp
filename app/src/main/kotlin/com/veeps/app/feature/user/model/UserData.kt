package com.veeps.app.feature.user.model

import com.google.gson.annotations.SerializedName
import com.veeps.app.util.DEFAULT

data class UserData(
	@SerializedName("id") var id: String = DEFAULT.EMPTY_STRING,
	@SerializedName("sub") var subscriptionStatus: String = DEFAULT.EMPTY_STRING,
	@SerializedName("currency") var currency: String = DEFAULT.EMPTY_STRING,
	@SerializedName("time_zone") var timeZone: String = DEFAULT.EMPTY_STRING,
	@SerializedName("beacon_base_url") var beaconBaseURL: String = DEFAULT.EMPTY_STRING,
	@SerializedName("email") var email: String = DEFAULT.EMPTY_STRING,
	@SerializedName("full_name") var fullName: String = DEFAULT.EMPTY_STRING,
	@SerializedName("display_name") var displayName: String = DEFAULT.EMPTY_STRING,
	@SerializedName("avatar_url") var avatarURL: String = DEFAULT.EMPTY_STRING,
	@SerializedName("time_zone_abbr") var timeZoneAbbr: String = DEFAULT.EMPTY_STRING,
)
