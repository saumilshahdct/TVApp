package com.veeps.app.feature.user.viewModel

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel(), DefaultLifecycleObserver {
	var contentHasLoaded = MutableLiveData(false)
	var isVisible = MutableLiveData(false)

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
}