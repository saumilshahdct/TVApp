package com.veeps.app.feature.search.viewModel

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SearchViewModel : ViewModel(), DefaultLifecycleObserver {
	var contentHasLoaded = MutableLiveData(false)
	var isVisible = MutableLiveData(false)

	override fun onResume(owner: LifecycleOwner) {
		isVisible.value = true
		super.onResume(owner)
	}

	override fun onPause(owner: LifecycleOwner) {
		isVisible.value = false
	}
}