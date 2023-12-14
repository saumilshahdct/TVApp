package com.veeps.app.feature.video.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.veeps.app.data.common.APIRepository
import com.veeps.app.feature.video.model.StoryBoardImagePosition
import com.veeps.app.util.AppConstants
import com.veeps.app.util.DEFAULT

class VideoPlayerViewModel  : ViewModel() {
	var contentHasLoaded = MutableLiveData(false)
	var errorMessage = MutableLiveData(DEFAULT.EMPTY_STRING)
	var errorPositiveLabel = MutableLiveData(DEFAULT.EMPTY_STRING)
	var errorNegativeLabel = MutableLiveData(DEFAULT.EMPTY_STRING)
	var isErrorPositiveApplicable = MutableLiveData(false)
	var isErrorNegativeApplicable = MutableLiveData(false)
	var eventId = MutableLiveData(DEFAULT.EMPTY_STRING)
	var playbackURL = MutableLiveData(DEFAULT.EMPTY_STRING)
	var storyBoardURL = MutableLiveData("")
	var tileWidth = MutableLiveData(0)
	var tileHeight = MutableLiveData(0)
	var tiles = MutableLiveData(ArrayList<StoryBoardImagePosition>())

	fun fetchEventStreamDetails(eventId: String) = APIRepository().fetchEventStreamDetails(eventId)
	fun fetchStoryBoard(storyBoardURL: String) = APIRepository().fetchStoryBoard(storyBoardURL)
}