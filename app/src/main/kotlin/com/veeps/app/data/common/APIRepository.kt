package com.veeps.app.data.common

import com.veeps.app.core.BaseDataSource
import com.veeps.app.data.network.APIUtil
import com.veeps.app.util.APIConstants
import com.veeps.app.util.AppConstants

class APIRepository : BaseDataSource() {
	fun authenticationDetails(clientId: String) =
		performOperation(APIConstants.fetchAuthenticationDetails) {
			getResult(APIConstants.fetchAuthenticationDetails) {
				APIUtil.service.fetchAuthenticationDetails(clientId)
			}
		}

	fun authenticationPolling(deviceCode: String) =
		performOperation(APIConstants.authenticationPolling) {
			getResult(APIConstants.authenticationPolling) {
				APIUtil.service.authenticationPolling(
					AppConstants.grantType,
					deviceCode,
					AppConstants.clientId,
					AppConstants.clientSecret
				)
			}
		}

	fun fetchUserDetails() = performOperation(APIConstants.fetchUserDetails) {
		getResult(APIConstants.fetchUserDetails) {
			APIUtil.service.fetchUserDetails()
		}
	}

	fun fetchBrowseRails() = performOperation(APIConstants.fetchBrowseRails) {
		getResult(APIConstants.fetchBrowseRails) {
			APIUtil.service.fetchBrowseRails()
		}
	}

	fun fetchOnDemandRails() = performOperation(APIConstants.fetchOnDemandRails) {
		getResult(APIConstants.fetchOnDemandRails) {
			APIUtil.service.fetchOnDemandRails()
		}
	}

	fun fetchFeaturedContent() = performOperation(APIConstants.fetchFeaturedContent) {
		getResult(APIConstants.fetchFeaturedContent) {
			APIUtil.service.fetchFeaturedContent()
		}
	}

	fun fetchContinueWatchingRail() = performOperation(APIConstants.fetchContinueWatchingRail) {
		getResult(APIConstants.fetchContinueWatchingRail) {
			APIUtil.service.fetchContinueWatchingRail()
		}
	}

	fun fetchUserStats(userStatsAPIURL: String, eventIds: String) =
		performOperation(APIConstants.fetchUserStats) {
			getResult(APIConstants.fetchUserStats) {
				APIUtil.service.fetchUserStats(userStatsAPIURL, eventIds)
			}
		}

	fun fetchUpcomingEvents() = performOperation(APIConstants.fetchUpcomingEvents) {
		getResult(APIConstants.fetchUpcomingEvents) {
			APIUtil.service.fetchUpcomingEvents()
		}
	}

	fun fetchSearchResult(search: String) =
		performOperation(APIConstants.fetchSearchResult) {
			getResult(APIConstants.fetchSearchResult) {
				APIUtil.service.fetchSearchResult(search)
			}
		}

	fun fetchEntityDetails(entity: String, entityId: String) =
		performOperation(APIConstants.fetchEntityDetails) {
			getResult(APIConstants.fetchEntityDetails) {
				APIUtil.service.fetchEntityDetails(entity, entityId)
			}
		}

	fun fetchEntityUpcomingEvents(entityScope: String) =
		performOperation(APIConstants.fetchEntityUpcomingEvents) {
			getResult(APIConstants.fetchEntityUpcomingEvents) {
				APIUtil.service.fetchEntityUpcomingEvents(entityScope)
			}
		}

	fun fetchEntityOnDemandEvents(entityScope: String) =
		performOperation(APIConstants.fetchEntityOnDemandEvents) {
			getResult(APIConstants.fetchEntityOnDemandEvents) {
				APIUtil.service.fetchEntityOnDemandEvents(entityScope)
			}
		}

	fun fetchEntityPastEvents(entityScope: String) =
		performOperation(APIConstants.fetchEntityPastEvents) {
			getResult(APIConstants.fetchEntityPastEvents) {
				APIUtil.service.fetchEntityPastEvents(entityScope)
			}
		}

	fun fetchAllPurchasedEvents() = performOperation(APIConstants.fetchAllPurchasedEvents) {
		getResult(APIConstants.fetchAllPurchasedEvents) {
			APIUtil.service.fetchAllPurchasedEvents()
		}
	}

	fun fetchAllWatchListEvents() = performOperation(APIConstants.watchListEvents) {
		getResult(APIConstants.watchListEvents) {
			APIUtil.service.fetchAllWatchListEvents()
		}
	}

	fun addRemoveWatchListEvent(eventId: HashMap<String, Any>, isRemoveFromWatchList: Boolean) =
		performOperation(APIConstants.watchListEvents) {
			getResult(APIConstants.watchListEvents) {
				if (isRemoveFromWatchList) APIUtil.service.removeWatchListEvent(eventId)
				else APIUtil.service.addWatchListEvent(eventId)
			}
		}

	fun fetchEventStreamDetails(eventId: String) =
		performOperation(APIConstants.fetchEventStreamDetails) {
			getResult(APIConstants.fetchEventStreamDetails) {
				APIUtil.service.fetchEventStreamDetails(eventId)
			}
		}

	fun fetchEventDetails(eventId: String) = performOperation(APIConstants.fetchEventDetails) {
		getResult(APIConstants.fetchEventDetails) {
			APIUtil.service.fetchEventDetails(eventId)
		}
	}

	fun fetchEventProductDetails(eventId: String) =
		performOperation(APIConstants.fetchEventProductDetails) {
			getResult(APIConstants.fetchEventProductDetails) {
				APIUtil.service.fetchEventProductDetails(eventId)
			}
		}

	fun claimFreeTicketForEvent(eventId: String) =
		performOperation(APIConstants.claimFreeTicketForEvent) {
			getResult(APIConstants.claimFreeTicketForEvent) {
				APIUtil.service.claimFreeTicketForEvent(eventId)
			}
		}

	fun clearAllReservations() = performOperation(APIConstants.clearAllReservations) {
		getResult(APIConstants.clearAllReservations) {
			APIUtil.service.clearAllReservations()
		}
	}

	fun setNewReservation(itemId: HashMap<String, Any>) =
		performOperation(APIConstants.setNewReservation) {
			getResult(APIConstants.setNewReservation) {
				APIUtil.service.setNewReservation(itemId)
			}
		}

	fun generateNewOrder() = performOperation(APIConstants.generateNewOrder) {
		getResult(APIConstants.generateNewOrder) {
			APIUtil.service.generateNewOrder()
		}
	}

	fun createOrder(orderDetails: HashMap<String, Any>) =
		performOperation(APIConstants.createOrder) {
			getResult(APIConstants.createOrder) {
				APIUtil.service.createOrder(orderDetails)
			}
		}

	fun fetchStoryBoard(storyBoardURL: String) = performOperation(APIConstants.fetchStoryBoard) {
		getResult(APIConstants.fetchStoryBoard) {
			APIUtil.service.fetchStoryBoard(storyBoardURL)
		}
	}

	fun fetchCompanions(eventDetails: HashMap<String, Any>) =
		performOperation(APIConstants.fetchCompanions) {
			getResult(APIConstants.fetchCompanions) {
				APIUtil.service.fetchCompanions(eventDetails)
			}
		}

	fun addStats(
		addStatsAPIURL: String,
		currentTime: String,
		duration: String,
		playerVersion: String,
		deviceModel: String,
		deviceVendor: String,
		playbackStreamType: String,
		platform: String,
		userType: String
	) = performOperation(APIConstants.addStats) {
		getResult(APIConstants.addStats) {
			APIUtil.service.addStats(
				addStatsAPIURL,
				currentTime,
				duration,
				playerVersion,
				deviceModel,
				deviceVendor,
				playbackStreamType,
				platform,
				userType
			)
		}
	}

}