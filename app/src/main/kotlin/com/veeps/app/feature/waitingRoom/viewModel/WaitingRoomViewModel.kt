package com.veeps.app.feature.waitingRoom.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.veeps.app.util.DEFAULT

class WaitingRoomViewModel : ViewModel() {
	var contentHasLoaded = MutableLiveData(false)
	var eventId = MutableLiveData(DEFAULT.EMPTY_STRING)
	var eventTimer = MutableLiveData(DEFAULT.EMPTY_STRING)
	var eventTimerDescription = MutableLiveData(DEFAULT.EMPTY_STRING)
	var eventTitle = MutableLiveData(DEFAULT.EMPTY_STRING)
	var eventLogo = MutableLiveData(DEFAULT.EMPTY_STRING)
}