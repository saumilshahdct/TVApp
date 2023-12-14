package com.veeps.app.feature.contentRail.model

import com.google.gson.annotations.SerializedName

data class Presentation(
	@SerializedName("portrait_url") var portraitUrl: String? = null,
	@SerializedName("logo_url") var logoUrl: String? = null,
	@SerializedName("badge_label") var badgeLabel: String? = null,
	@SerializedName("content_badges") var contentBadges: ArrayList<String> = arrayListOf(),
	@SerializedName("byline") var byline: String? = null,
	@SerializedName("poster_url") var posterUrl: String? = null,
	@SerializedName("sponsorship_logo_url") var sponsorshipLogoUrl: String? = null,
	@SerializedName("subtitle") var subtitle: String? = null,
	@SerializedName("badge_bg_color") var badgeBgColor: String? = null,
	@SerializedName("badge_fg_color") var badgeFgColor: String? = null,
	@SerializedName("cta_bg_color") var ctaBgColor: String? = null,
	@SerializedName("cta_bg_color_hover") var ctaBgColorHover: String? = null,
	@SerializedName("cta_fg_color") var ctaFgColor: String? = null,
	@SerializedName("highlight_bg_color") var highlightBgColor: String? = null,
)
