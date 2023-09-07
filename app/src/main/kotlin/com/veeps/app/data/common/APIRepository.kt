package com.veeps.app.data.common

import com.veeps.app.core.BaseDataSource
import com.veeps.app.data.network.APIUtil
import com.veeps.app.util.APIConstants

class APIRepository : BaseDataSource() {

	fun guestUserToken() = performOperation(APIConstants.fetchGuestUserToken) {
		getResult(APIConstants.fetchGuestUserToken) {
			APIUtil.service.fetchGuestUserToken()
		}
	}

}