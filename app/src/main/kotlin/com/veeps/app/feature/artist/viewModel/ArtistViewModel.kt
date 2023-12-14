package com.veeps.app.feature.artist.viewModel

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.veeps.app.data.common.APIRepository
import com.veeps.app.feature.contentRail.model.RailData

class ArtistViewModel : ViewModel(), DefaultLifecycleObserver {
	var contentHasLoaded = MutableLiveData(false)
	var isVisible = MutableLiveData(false)
	var allEventsRail = MutableLiveData(ArrayList<RailData>())

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

	fun fetchEntityDetails(entity: String, entityId: String) =
		APIRepository().fetchEntityDetails(entity, entityId)

	fun fetchEntityUpcomingEvents(entityScope: String) =
		APIRepository().fetchEntityUpcomingEvents(entityScope)

	fun fetchEntityOnDemandEvents(entityScope: String) =
		APIRepository().fetchEntityOnDemandEvents(entityScope)

	fun fetchEntityPastEvents(entityScope: String) =
		APIRepository().fetchEntityPastEvents(entityScope)
}