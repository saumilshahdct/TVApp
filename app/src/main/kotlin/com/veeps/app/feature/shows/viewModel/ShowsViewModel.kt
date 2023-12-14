package com.veeps.app.feature.shows.viewModel

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.veeps.app.data.common.APIRepository
import com.veeps.app.feature.contentRail.model.RailData

class ShowsViewModel : ViewModel(), DefaultLifecycleObserver {
	var contentHasLoaded = MutableLiveData(false)
	var isVisible = MutableLiveData(false)
	var allRails = MutableLiveData(ArrayList<RailData>())

	override fun onResume(owner: LifecycleOwner) {
		super.onResume(owner)
		isVisible.postValue(true)
	}

	override fun onPause(owner: LifecycleOwner) {
		isVisible.postValue(false)
		super.onPause(owner)
	}

	override fun onStart(owner: LifecycleOwner) {
		super.onStart(owner)
		isVisible.postValue(true)
	}

	override fun onStop(owner: LifecycleOwner) {
		isVisible.postValue(false)
		super.onStop(owner)
	}

	fun fetchEventDetails(eventId: String) = APIRepository().fetchEventDetails(eventId)
	fun fetchAllPurchasedEvents() = APIRepository().fetchAllPurchasedEvents()
	fun fetchAllWatchListEvents() = APIRepository().fetchAllWatchListEvents()
}