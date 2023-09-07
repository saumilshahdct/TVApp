package com.veeps.app.feature.intro.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class IntroViewModel : ViewModel() {
	var contentHasLoaded = MutableLiveData(false)
	var errorMessage = MutableLiveData("")
	var errorPositiveLabel = MutableLiveData("")
	var errorNegativeLabel = MutableLiveData("")
	var isErrorPositiveApplicable = MutableLiveData(false)
	var isErrorNegativeApplicable = MutableLiveData(false)
}