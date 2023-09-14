package com.veeps.app.data.common

import com.veeps.app.core.BaseDataSource
import com.veeps.app.data.network.APIUtil
import com.veeps.app.util.APIConstants
import com.veeps.app.util.AppConstants
import com.veeps.app.util.Logger

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

}