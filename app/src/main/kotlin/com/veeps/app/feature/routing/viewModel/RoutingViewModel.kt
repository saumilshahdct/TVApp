package com.veeps.app.feature.routing.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.veeps.app.data.common.APIRepository

class RoutingViewModel : ViewModel() {
	var contentHasLoaded = MutableLiveData(false)
	var errorMessage = MutableLiveData("this is errr")
	var errorPositiveLabel = MutableLiveData("")
	var errorNegativeLabel = MutableLiveData("")
	var isErrorPositiveApplicable = MutableLiveData(false)
	var isErrorNegativeApplicable = MutableLiveData(false)

	fun fetchGuestUserToken() = APIRepository().guestUserToken()
}