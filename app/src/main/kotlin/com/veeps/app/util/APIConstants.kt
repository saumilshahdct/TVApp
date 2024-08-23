package com.veeps.app.util

import com.veeps.app.BuildConfig.isProduction

object APIConstants {
	private const val STAGE: String = "https://api.veeps-team.com/"
	private const val PROD: String = "https://api.veeps.com/"
	private const val AUTH_PROD: String = "https://veeps.com/"
	private const val AUTH_STAG: String ="https://veeps-team.com/" //Staging
	private const val PLAYER_STAT_STAGE: String = "https://vstats.veeps-team.com"
	private const val PLAYER_STAT_PROD: String = "https://vstats.veeps.com"

	var BASE_URL: String = if (isProduction) PROD else STAGE
	private const val API_VERSION: String = "v3/"
	private const val AUTH_VERSION: String = AUTH_PROD + "oauth/"
	const val QR_CODE_BASE_URL: String =
		"https://veeps.com/qr-code?i="
	const val QR_CODE_ACTIVATE_URL: String =
		"${QR_CODE_BASE_URL}https://veeps.com/activate?code="

	const val fetchAuthenticationDetails: String = AUTH_VERSION + "authorize_device"
	const val authenticationPolling: String = AUTH_VERSION + "token"
	const val fetchUserDetails: String = API_VERSION + "me"
	const val fetchBrowseRails: String = API_VERSION + "features"
	const val fetchOnDemandRails: String = API_VERSION + "features?scope=on-demand"
	const val fetchContinueWatchingRail: String = API_VERSION + "me/tickets?type=on_demand"
	const val fetchUserStats: String = "/getuserstats"
	const val fetchUpcomingEvents: String = API_VERSION + "events?type=upcoming"
	const val fetchSearchResult: String = API_VERSION + "search"
	const val fetchEntityDetails: String = API_VERSION + "entities/{ENTITY}/{ENTITY_ID}"
	const val fetchEntityUpcomingEvents: String =
		API_VERSION + "events/?per_page=10&page=1&type=upcoming"
	const val fetchEntityOnDemandEvents: String =
		API_VERSION + "events/?per_page=10&page=1&type=on_demand"
	const val fetchEntityPastEvents: String = API_VERSION + "events/?per_page=10&page=1&type=past"
	const val fetchAllPurchasedEvents: String = API_VERSION + "me/tickets?type=all"
	const val watchListEvents: String = API_VERSION + "watchlist_entries"
	const val removeWatchListEvents: String = API_VERSION + "watchlist_entries"
	const val fetchEventStreamDetails: String = API_VERSION + "events/{EVENT_ID}/stream"
	const val fetchEventDetails: String = API_VERSION + "events/{EVENT_ID}"
	const val fetchEventProductDetails: String =
		API_VERSION + "events/{EVENT_ID}/products?vendor=fire_tv"
	const val claimFreeTicketForEvent: String = API_VERSION + "events/{EVENT_ID}/redeem"
	const val clearAllReservations: String = API_VERSION + "reservations/all"
	const val setNewReservation: String = API_VERSION + "reservations"
	const val generateNewOrder: String = API_VERSION + "orders/new"
	const val createOrder: String = API_VERSION + "orders"
	const val fetchStoryBoard: String = "fetchStoryBoard"
	const val addStats: String = "/addstat"
	const val fetchCompanions: String = API_VERSION + "me/companion"
	const val fetchFeaturedContent: String = API_VERSION + "features?scope=search"
	const val fetchRecommendedContent: String = API_VERSION + "features"
	const val fetchGenreContent: String = API_VERSION + "features"
	const val subscriptionMapping: String = API_VERSION + "partner_subscriptions/firetv/mapping"
	const val validateAppVersions: String = API_VERSION + "versions?platform=firetv&stage=prod"
}
