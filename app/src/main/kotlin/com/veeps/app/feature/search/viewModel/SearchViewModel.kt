package com.veeps.app.feature.search.viewModel

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.veeps.app.data.common.APIRepository
import com.veeps.app.feature.contentRail.model.RailData
import kotlinx.coroutines.Job

class SearchViewModel : ViewModel(), DefaultLifecycleObserver {
	var contentHasLoaded = MutableLiveData(false)
	var isVisible = MutableLiveData(false)
	var upcomingRail = MutableLiveData(ArrayList<RailData>())
	var featuredContentRail = MutableLiveData(ArrayList<RailData>())
	var searchResult = MutableLiveData(ArrayList<RailData>())
	var noResult = MutableLiveData(false)
	var search = MutableLiveData("")

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

	fun fetchUpcomingEvents() = APIRepository().fetchUpcomingEvents()
	fun fetchFeaturedContent() = APIRepository().fetchFeaturedContent()
	fun fetchSearchResult(job: Job) = APIRepository().fetchSearchResult(search = search.value.orEmpty(), job)
}