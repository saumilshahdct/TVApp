package com.veeps.app.feature.signIn.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.veeps.app.data.common.APIRepository
import com.veeps.app.util.AppConstants
import com.veeps.app.util.DEFAULT
import com.veeps.app.util.Logger

class SignInViewModel : ViewModel() {
	var contentHasLoaded = MutableLiveData(false)
	var errorMessage = MutableLiveData("")
	var errorPositiveLabel = MutableLiveData("")
	var errorNegativeLabel = MutableLiveData("")
	var isErrorPositiveApplicable = MutableLiveData(false)
	var isErrorNegativeApplicable = MutableLiveData(false)

	var authenticationLoginURL = MutableLiveData("")
	var authenticationCode = MutableLiveData("")
	var authenticationQRCode = MutableLiveData("")
	var pollingInterval = MutableLiveData(0)
	var tokenExpiryTime = MutableLiveData(0)
	var deviceCode = MutableLiveData("")

	fun fetchAuthenticationDetails() = APIRepository().authenticationDetails(AppConstants.clientId)
	fun authenticationPolling() =
		APIRepository().authenticationPolling(if (deviceCode.value.isNullOrBlank()) DEFAULT.EMPTY_STRING else deviceCode.value.toString())

	fun fetchUserDetails() = APIRepository().fetchUserDetails()
}