package com.veeps.app.feature.browse.ui

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewOutlineProvider
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.leanback.widget.BaseGridView
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C.AUDIO_CONTENT_TYPE_MOVIE
import androidx.media3.common.C.USAGE_MEDIA
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.util.EventLogger
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.veeps.app.R
import com.veeps.app.core.BaseFragment
import com.veeps.app.databinding.FragmentBrowseScreenBinding
import com.veeps.app.extension.fadeInNow
import com.veeps.app.extension.fadeOutNow
import com.veeps.app.extension.isGreaterThan
import com.veeps.app.extension.loadImage
import com.veeps.app.feature.browse.viewModel.BrowseViewModel
import com.veeps.app.feature.contentRail.adapter.ContentRailsAdapter
import com.veeps.app.feature.contentRail.model.Entities
import com.veeps.app.feature.contentRail.model.RailData
import com.veeps.app.feature.contentRail.model.UserStats
import com.veeps.app.feature.event.ui.EventScreen
import com.veeps.app.util.APIConstants
import com.veeps.app.util.AppAction
import com.veeps.app.util.AppConstants
import com.veeps.app.util.AppPreferences
import com.veeps.app.util.AppUtil
import com.veeps.app.util.ButtonLabels
import com.veeps.app.util.CardTypes
import com.veeps.app.util.DEFAULT
import com.veeps.app.util.DateTimeCompareDifference
import com.veeps.app.util.EntityTypes
import com.veeps.app.util.ImageTags
import com.veeps.app.util.IntValue
import com.veeps.app.util.Logger
import com.veeps.app.util.Screens
import com.veeps.app.widget.navigationMenu.NavigationItems
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.json.JSONObject


class BrowseScreen : BaseFragment<BrowseViewModel, FragmentBrowseScreenBinding>() {

	private lateinit var carouselData: RailData
	private lateinit var player: ExoPlayer
	private var posterImage: String = DEFAULT.EMPTY_STRING
	private var playbackURL: String = DEFAULT.EMPTY_STRING
	private var requireCarouselRemoval: Boolean = true
	private val action by lazy {
		object : AppAction {
			override fun onAction(entity: Entities) {
				Logger.print(
					"Action performed on ${
						this@BrowseScreen.javaClass.name.substringAfterLast(".")
					}"
				)
				fetchEventDetails(entity)
			}
		}
	}
	private val railAdapter by lazy {
		ContentRailsAdapter(rails = arrayListOf(), helper, Screens.BROWSE, action)
	}

	override fun getViewBinding(): FragmentBrowseScreenBinding =
		FragmentBrowseScreenBinding.inflate(layoutInflater)

	override fun onDestroyView() {
		Logger.print("browse view is destroyed")
		viewModel.railData.postValue(arrayListOf())
		viewModelStore.clear()
		releaseVideoPlayer()
		super.onDestroyView()
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.apply {
			browse = viewModel
			browseScreen = this@BrowseScreen
			lifecycleOwner = viewLifecycleOwner
			lifecycle.addObserver(viewModel)
			loader.visibility = View.VISIBLE
			loader.requestFocus()
			carousel.visibility = View.INVISIBLE
			darkBackground.visibility = View.VISIBLE
			watermark.visibility = View.GONE
		}
		setupBlur()
		loadAppContent()
		notifyAppEvents()
	}

	private fun setupVideoPlayer() {
		releaseVideoPlayer()
		player = context?.let { context -> ExoPlayer.Builder(context).build() }!!
		player.addListener(object : Player.Listener {
			override fun onIsPlayingChanged(isPlaying: Boolean) {
				if (isPlaying) {
					binding.heroImage.fadeOutNow(IntValue.NUMBER_1000)
				} else {
					binding.heroImage.fadeInNow(IntValue.NUMBER_1000)
				}
				super.onIsPlayingChanged(isPlaying)
			}

			override fun onPlaybackStateChanged(playbackState: Int) {
				if (playbackState == Player.STATE_ENDED) {
					binding.heroImage.fadeInNow(IntValue.NUMBER_1000)
					releaseVideoPlayer()
				}
				super.onPlaybackStateChanged(playbackState)
			}

			override fun onPlayerError(error: PlaybackException) {
				releaseVideoPlayer()
				super.onPlayerError(error)
			}
		})
		player.setAudioAttributes(
			AudioAttributes.Builder().setUsage(USAGE_MEDIA).setContentType(AUDIO_CONTENT_TYPE_MOVIE)
				.build(), true
		)
		player.addAnalyticsListener(EventLogger())
		binding.videoPlayer.player = player
		player.repeatMode = Player.REPEAT_MODE_ONE
		if (playbackURL.isNotBlank()) {
			player.setMediaItem(MediaItem.fromUri(playbackURL))
			player.prepare()
			player.play()
			if (homeViewModel.isNavigationMenuVisible.value!! || homeViewModel.isErrorVisible.value!! || binding.darkBackground.visibility == View.VISIBLE) {
				if (this::player.isInitialized && player.isPlaying) {
					player.pause()
				}
			}
		}
	}

	private fun releaseVideoPlayer() {
		if (this::player.isInitialized && player.mediaItemCount.isGreaterThan(0)) {
			player.playWhenReady = false
			player.pause()
			player.release()
		}
	}

	private fun setupBlur() {
		binding.browseLabel.setupWith(binding.container).setBlurRadius(12.5f)
		binding.browseLabel.outlineProvider = ViewOutlineProvider.BACKGROUND
		binding.browseLabel.clipToOutline = true

		binding.onDemandLabel.setupWith(binding.container).setBlurRadius(12.5f)
		binding.onDemandLabel.outlineProvider = ViewOutlineProvider.BACKGROUND
		binding.onDemandLabel.clipToOutline = true

		binding.primary.setupWith(binding.container).setBlurRadius(12.5f)
		binding.primary.outlineProvider = ViewOutlineProvider.BACKGROUND
		binding.primary.clipToOutline = true
		binding.primary.setBlurEnabled(true)

		binding.myShows.setupWith(binding.container).setBlurRadius(12.5f)
		binding.myShows.outlineProvider = ViewOutlineProvider.BACKGROUND
		binding.myShows.clipToOutline = true
		binding.myShows.setBlurEnabled(true)
	}

	private fun loadAppContent() {
		binding.listing.apply {
			setNumColumns(1)
			setHasFixedSize(true)
			windowAlignment = BaseGridView.WINDOW_ALIGN_HIGH_EDGE
			windowAlignmentOffsetPercent = 0f
			isItemAlignmentOffsetWithPadding = true
			itemAlignmentOffsetPercent = 0f
			adapter = railAdapter
		}
		binding.onDemandLabel.setOnFocusChangeListener { _, hasFocus ->
			context?.let { context ->
				binding.onDemandText.setTextColor(
					ContextCompat.getColor(
						context, if (hasFocus) R.color.dark_black else R.color.white
					)
				)
			}
		}
		binding.browseLabel.setOnFocusChangeListener { _, hasFocus ->
			context?.let { context ->
				binding.browseText.setTextColor(
					ContextCompat.getColor(
						context, if (hasFocus) R.color.dark_black else R.color.white
					)
				)
			}
		}
		binding.primary.setOnKeyListener { _, keyCode, keyEvent ->
			if (keyEvent.action == KeyEvent.ACTION_DOWN) {
				if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
//					if (this::player.isInitialized && player.mediaItemCount.isGreaterThan(0) && player.isPlaying) {
//						player.pause()
//					}
//					binding.darkBackground.visibility = View.VISIBLE
//					binding.carousel.visibility = View.GONE
//					binding.browseLabel.visibility = View.GONE
//					binding.onDemandLabel.visibility = View.GONE
//					binding.logo.visibility = View.GONE
//					binding.watermark.visibility = View.VISIBLE
				}
			}
			return@setOnKeyListener false
		}
		binding.myShows.setOnKeyListener { _, keyCode, keyEvent ->
			if (keyEvent.action == KeyEvent.ACTION_DOWN) {
				if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
//					if (this::player.isInitialized && player.mediaItemCount.isGreaterThan(0) && player.isPlaying) {
//						player.pause()
//					}
//					binding.darkBackground.visibility = View.VISIBLE
//					binding.carousel.visibility = View.GONE
//					binding.browseLabel.visibility = View.GONE
//					binding.onDemandLabel.visibility = View.GONE
//					binding.logo.visibility = View.GONE
//					binding.watermark.visibility = View.VISIBLE
				}
			}
			return@setOnKeyListener false
		}
		binding.primary.setOnFocusChangeListener { _, hasFocus ->
			context?.let { context ->
				binding.primaryLabel.setTextColor(
					ContextCompat.getColor(
						context,
						if (hasFocus) R.color.dark_black else if (binding.primaryLabel.isSelected) R.color.white_30 else R.color.white
					)
				)
			}
		}
		binding.myShows.setOnFocusChangeListener { _, hasFocus ->
			context?.let { context ->
				binding.myShowsLabel.compoundDrawables.forEach { drawable ->
					drawable?.setTint(
						ContextCompat.getColor(
							context,
							if (hasFocus.or(binding.myShows.isFocused)
									.or(binding.myShows.hasFocus())
							) R.color.dark_black else R.color.white
						)
					)
				}
				binding.myShowsLabel.setTextColor(
					ContextCompat.getColor(
						context,
						if (hasFocus.or(binding.myShows.isFocused)
								.or(binding.myShows.hasFocus())
						) R.color.dark_black else R.color.white
					)
				)
			}
		}
		onBrowseLabelClicked()
	}

	private fun notifyAppEvents() {
		homeViewModel.isNavigationMenuVisible.observe(viewLifecycleOwner) { isNavigationMenuVisible ->
			if (isNavigationMenuVisible) {
				if (this::player.isInitialized && player.mediaItemCount.isGreaterThan(0) && player.isPlaying) {
					player.pause()
				}
			} else {
				if (this::player.isInitialized && player.mediaItemCount.isGreaterThan(0) && !player.isPlaying && homeViewModel.isErrorVisible.value?.equals(
						false
					) == true && binding.darkBackground.visibility == View.GONE
				) {
					player.play()
				}
			}
		}
		homeViewModel.playerShouldRelease.observe(viewLifecycleOwner) { playerShouldRelease ->
			if (playerShouldRelease) {
				releaseVideoPlayer()
			}
		}
		homeViewModel.playerShouldPause.observe(viewLifecycleOwner) { playerShouldPause ->
			if (playerShouldPause) {
				if (this::player.isInitialized && player.mediaItemCount.isGreaterThan(0) && player.isPlaying) {
					player.pause()
				}
			} else {
				if (this::player.isInitialized && player.mediaItemCount.isGreaterThan(0) && !player.isPlaying && homeViewModel.isErrorVisible.value?.equals(
						false
					) == true && binding.darkBackground.visibility == View.GONE
				) {
					player.play()
				}
			}
		}
		homeViewModel.translateCarouselToTop.observe(viewLifecycleOwner) { shouldTranslate ->
			if (shouldTranslate && viewModel.isVisible.value == true) {
				if (this::player.isInitialized && player.mediaItemCount.isGreaterThan(0) && player.isPlaying) {
					player.pause()
				}
				binding.darkBackground.visibility = View.VISIBLE
				binding.carousel.visibility = View.GONE
				binding.browseLabel.visibility = View.GONE
				binding.onDemandLabel.visibility = View.GONE
				binding.logo.visibility = View.GONE
				binding.watermark.visibility = View.VISIBLE
			}
		}
		homeViewModel.translateCarouselToBottom.observe(viewLifecycleOwner) { shouldTranslate ->
			Logger.printWithTag(
				"saumil", " Here in bottom - $shouldTranslate -- ${viewModel.isVisible.value}"
			)
			if (shouldTranslate && viewModel.isVisible.value == true) {
				if (this::player.isInitialized && player.mediaItemCount.isGreaterThan(0) && !player.isPlaying && homeViewModel.isErrorVisible.value?.equals(
						false
					) == true && homeViewModel.isNavigationMenuVisible.value?.equals(
						false
					) == true
				) {
					player.play()
				}
				binding.watermark.visibility = View.GONE
				binding.darkBackground.visibility = View.GONE
				binding.carousel.visibility = View.VISIBLE
				binding.primary.requestFocus()
				binding.browseLabel.visibility = View.VISIBLE
				binding.onDemandLabel.visibility = View.VISIBLE
				binding.logo.visibility = View.VISIBLE
			}
		}

		viewModel.isVisible.observeForever { isVisible ->
			Logger.printWithTag("MenuFocus",
				"Visibility Changed to $isVisible On ${
					this@BrowseScreen.javaClass.name.substringAfterLast(".")
				}"
			)
			if (isVisible) {
				helper.selectNavigationMenu(NavigationItems.BROWSE_MENU)

				helper.completelyHideNavigationMenu()
				if (this::player.isInitialized && player.mediaItemCount.isGreaterThan(0) && !player.isPlaying && homeViewModel.isErrorVisible.value?.equals(
						false
					) == true && homeViewModel.isNavigationMenuVisible.value?.equals(
						false
					) == true && binding.darkBackground.visibility == View.GONE
				) {
					player.play()
				}
			} else {
				if (this::player.isInitialized && player.mediaItemCount.isGreaterThan(0) && player.isPlaying) {
					player.pause()
				}
			}
		}
		viewModel.railData.observe(viewLifecycleOwner) { rails ->
			if (rails.isNotEmpty()) {
				if (requireCarouselRemoval) {
					var carouselPosition = 0
					val carousel = rails.filterIndexed { index, railData ->
						if (railData.cardType == CardTypes.HERO) {
							carouselPosition = index
						}
						return@filterIndexed railData.cardType == CardTypes.HERO
					}
					if (carousel.isNotEmpty()) {
						carouselData = rails[carouselPosition]
						setCarousel()
						rails.removeAt(carouselPosition)
					}
				} else {
					requireCarouselRemoval = true
				}
				rails.removeIf { rail ->
					rail.cardType.equals("wide")
				}
				railAdapter.setRails(rails)
			} else {
				railAdapter.setRails(arrayListOf())
			}
		}
	}

	private fun setCarousel() {
		val random = 0//Random.nextInt(carouselData.entities.size)
		val entity =
			if (carouselData.entities.isNotEmpty()) carouselData.entities[random] else Entities()
		binding.ctaContainer.visibility = View.INVISIBLE
		binding.primary.alpha = 0.1f
		binding.primaryLabel.text =
			AppUtil.getPrimaryLabelText(entity, Screens.BROWSE, false).also { label ->
				binding.primaryLabel.isSelected = label == ButtonLabels.UNAVAILABLE
			}
		viewModel.eventId = entity.id ?: DEFAULT.EMPTY_STRING
		binding.primary.alpha = 1.0f
		binding.myShows.visibility = if (AppPreferences.get(
				AppConstants.userSubscriptionStatus, "none"
			) != "none"
		) View.VISIBLE else View.GONE
		setupMyShows(isAdded = homeViewModel.watchlistIds.contains(entity.id?.ifBlank { DEFAULT.EMPTY_STRING }
			?: DEFAULT.EMPTY_STRING))
		binding.ctaContainer.visibility =
			if (carouselData.entities.isNotEmpty()) View.VISIBLE else View.INVISIBLE
		val currentDate = DateTime.now()
		val otherDate = DateTime(entity.eventStreamStartsAt?.ifBlank { DateTime.now().toString() }
			?: DateTime.now().toString(), DateTimeZone.UTC).withZone(DateTimeZone.getDefault())
			.toDateTime()
		binding.date.text = otherDate.toString("MMM dd, yyyy${DEFAULT.SEPARATOR}ha")
		if (AppUtil.compare(otherDate, currentDate) == DateTimeCompareDifference.GREATER_THAN) {
			binding.date.visibility =
				if (binding.browseLabel.isSelected) View.VISIBLE else View.GONE
			binding.liveNow.visibility = View.GONE
		} else {
			binding.date.visibility =
				if (binding.browseLabel.isSelected) View.VISIBLE else View.GONE
			binding.liveNow.visibility = View.GONE
		}
		val title = entity.eventName?.ifBlank { DEFAULT.EMPTY_STRING } ?: DEFAULT.EMPTY_STRING
		posterImage =
			entity.presentation.posterUrl?.ifBlank { DEFAULT.EMPTY_STRING } ?: DEFAULT.EMPTY_STRING
		val logoImage =
			entity.presentation.logoUrl?.ifBlank { DEFAULT.EMPTY_STRING } ?: DEFAULT.EMPTY_STRING
		val videoPreviewTreeMap = entity.videoPreviews ?: false
		playbackURL = DEFAULT.EMPTY_STRING
		if (videoPreviewTreeMap is LinkedTreeMap<*, *>) {
			if (videoPreviewTreeMap.isNotEmpty()) {
				val jsonObject = Gson().toJsonTree(videoPreviewTreeMap).asJsonObject
				if (jsonObject != null && !jsonObject.isJsonNull && !jsonObject.isEmpty) {
					val videoPreviewString: String = Gson().toJson(jsonObject)
					val videoPreview = JSONObject(videoPreviewString)
					if (videoPreview.has("high")) {
						playbackURL = videoPreview.getString("high")
					}
				}
			}
		}
		binding.carouselLogo.loadImage(logoImage, ImageTags.LOGO)
		binding.heroImage.loadImage(posterImage, ImageTags.HERO)
		binding.carouselTitle.text = title
		binding.carousel.visibility = View.VISIBLE
		binding.primary.requestFocus()
		binding.darkBackground.visibility = View.GONE
		if (playbackURL.isNotBlank()) {
			setupVideoPlayer()
		} else {
			releaseVideoPlayer()
		}
	}

	private fun fetchEventDetails(entity: Entities) {
		viewModel.fetchEventDetails(entity.eventId ?: entity.id ?: DEFAULT.EMPTY_STRING)
			.observe(viewLifecycleOwner) { eventResponse ->
				fetch(
					eventResponse,
					isLoaderEnabled = true,
					canUserAccessScreen = true,
					shouldBeInBackground = false
				) {
					eventResponse.response?.let { eventStreamData ->
						eventStreamData.data?.let {
							val streamStartsAt = it.eventStreamStartsAt ?: DEFAULT.EMPTY_STRING
							val doorOpensAt = it.eventDoorsAt ?: DEFAULT.EMPTY_STRING
							if (doorOpensAt.isBlank()) {
								if (streamStartsAt.isNotBlank() && AppUtil.compare(
										streamStartsAt
									) == DateTimeCompareDifference.GREATER_THAN
								) {
									val eventId = it.eventId ?: it.id ?: DEFAULT.EMPTY_STRING
									val eventLogo =
										entity.presentation.logoUrl?.ifBlank { DEFAULT.EMPTY_STRING }
											?: DEFAULT.EMPTY_STRING
									val eventTitle =
										entity.eventName?.ifBlank { DEFAULT.EMPTY_STRING }
											?: DEFAULT.EMPTY_STRING
									helper.goToWaitingRoom(
										eventId, eventLogo, eventTitle, doorOpensAt, streamStartsAt
									)
								} else {
									helper.goToVideoPlayer(
										it.eventId ?: it.id ?: DEFAULT.EMPTY_STRING
									)
								}
							} else {
								if (AppUtil.compare(doorOpensAt) == DateTimeCompareDifference.LESS_THAN) {
									helper.goToVideoPlayer(
										it.eventId ?: it.id ?: DEFAULT.EMPTY_STRING
									)
								} else if (streamStartsAt.isNotBlank() && AppUtil.compare(
										streamStartsAt
									) == DateTimeCompareDifference.GREATER_THAN
								) {
									val eventId = it.eventId ?: it.id ?: DEFAULT.EMPTY_STRING
									val eventLogo =
										entity.presentation.logoUrl?.ifBlank { DEFAULT.EMPTY_STRING }
											?: DEFAULT.EMPTY_STRING
									val eventTitle =
										entity.eventName?.ifBlank { DEFAULT.EMPTY_STRING }
											?: DEFAULT.EMPTY_STRING
									helper.goToWaitingRoom(
										eventId, eventLogo, eventTitle, doorOpensAt, streamStartsAt
									)
								} else {
									helper.goToVideoPlayer(
										it.eventId ?: it.id ?: DEFAULT.EMPTY_STRING
									)
								}
							}
						}
					}
				}
			}
	}

	private fun fetchBrowseRails() {
		viewModel.fetchBrowseRails().observe(viewLifecycleOwner) { browseRail ->
			fetch(
				browseRail,
				isLoaderEnabled = true,
				canUserAccessScreen = false,
				shouldBeInBackground = false
			) {
				browseRail.response?.let { railResponse ->

					viewModel.railData.postValue(railResponse.railData)
					fetchContinueWatchingRail()
				} ?: helper.showErrorOnScreen(browseRail.tag, getString(R.string.unknown_error))
			}
		}
	}

	private fun fetchOnDemandRails() {
		viewModel.fetchOnDemandRails().observe(viewLifecycleOwner) { onDemand ->
			fetch(
				onDemand,
				isLoaderEnabled = true,
				canUserAccessScreen = false,
				shouldBeInBackground = false
			) {
				onDemand.response?.let { railResponse ->
					viewModel.railData.postValue(railResponse.railData)
				} ?: helper.showErrorOnScreen(onDemand.tag, getString(R.string.unknown_error))
			}
		}
	}

	private fun fetchContinueWatchingRail() {
		viewModel.fetchContinueWatchingRail().observe(viewLifecycleOwner) { continueWatching ->
			fetch(
				continueWatching,
				isLoaderEnabled = false,
				canUserAccessScreen = true,
				shouldBeInBackground = true,
			) {
				continueWatching.response?.let { railData ->
					if (railData.data.isNotEmpty()) {
						fetchUserStats(railData.data)
					}
				}
			}
		}
	}

	fun addRemoveWatchListEvent(eventId: String) {
		if (eventId.isNotBlank()) {
			viewModel.addRemoveWatchListEvent(
				hashMapOf("event_id" to eventId), binding.myShows.isSelected
			).observe(viewLifecycleOwner) { addWatchList ->
				fetch(
					addWatchList,
					isLoaderEnabled = false,
					canUserAccessScreen = true,
					shouldBeInBackground = true,
				) {
					setupMyShows(!binding.myShows.isSelected)
				}
			}
		}
	}

	private fun setupMyShows(isAdded: Boolean) {
		binding.myShows.isSelected = isAdded
		binding.myShowsLabel.setCompoundDrawablesRelativeWithIntrinsicBounds(
			if (binding.myShows.isSelected) {
				if (binding.myShows.hasFocus()) R.drawable.check_black else R.drawable.check_white
			} else {
				if (binding.myShows.hasFocus()) R.drawable.add_black else R.drawable.add_white
			}, 0, 0, 0
		)
	}

	private fun fetchUserStats(continueWatchingEntities: ArrayList<Entities>) {
		var eventIds = ""
		continueWatchingEntities.forEachIndexed { index, entities ->
			eventIds += "${entities.eventId}${(if (index == continueWatchingEntities.size - 1) "" else ",")}"
		}
		val userStatsAPIURL = AppPreferences.get(
			AppConstants.userBeaconBaseURL, DEFAULT.EMPTY_STRING
		) + APIConstants.fetchUserStats
		AppPreferences.set(AppConstants.generatedJWT, AppUtil.generateJWT(eventIds))
		viewModel.fetchUserStats(userStatsAPIURL, eventIds)
			.observe(viewLifecycleOwner) { userStatsDetails ->
				fetch(
					userStatsDetails,
					isLoaderEnabled = false,
					canUserAccessScreen = true,
					shouldBeInBackground = true,
				) {
					var userStats = arrayListOf<UserStats>()
					userStatsDetails.response?.let { userStatsResponse ->
						userStats = userStatsResponse.userStats
					}
					val continueWatchingRail = RailData(
						name = "Continue Watching",
						entities = continueWatchingEntities,
						cardType = CardTypes.PORTRAIT,
						entitiesType = EntityTypes.EVENT,
						isContinueWatching = true,
						userStats = userStats
					)
					val rails = viewModel.railData.value.orEmpty()

					viewModel.railData.value = ArrayList(rails).apply {
						add(0, continueWatchingRail)
						requireCarouselRemoval = false
					}
				}
			}
	}

	fun onPrimaryClicked() {
		helper.setupPageChange(
			true, EventScreen::class.java, bundleOf(
				AppConstants.TAG to Screens.EVENT,
				"entityId" to viewModel.eventId,
				"entity" to EntityTypes.EVENT
			), Screens.EVENT, true
		)
	}

	fun onBrowseLabelClicked() {
		if (!binding.browseLabel.isSelected) {
			releaseVideoPlayer()
			railAdapter.setRails(arrayListOf())
			binding.carousel.visibility = View.INVISIBLE
			binding.darkBackground.visibility = View.VISIBLE
			binding.loader.visibility = View.VISIBLE
			binding.loader.requestFocus()
			binding.browseLabel.isSelected = true
			binding.onDemandLabel.isSelected = false
			binding.browseLabel.setBlurEnabled(true)
			binding.onDemandLabel.setBlurEnabled(false)
			helper.fetchAllWatchListEvents()
			fetchBrowseRails()
		}
	}

	fun onOnDemandLabelClicked() {
		if (!binding.onDemandLabel.isSelected) {
			releaseVideoPlayer()
			railAdapter.setRails(arrayListOf())
			binding.carousel.visibility = View.INVISIBLE
			binding.darkBackground.visibility = View.VISIBLE
			binding.loader.visibility = View.VISIBLE
			binding.loader.requestFocus()
			binding.onDemandLabel.isSelected = true
			binding.browseLabel.isSelected = false
			binding.onDemandLabel.setBlurEnabled(true)
			binding.browseLabel.setBlurEnabled(false)
			helper.fetchAllWatchListEvents()
			fetchOnDemandRails()
		}
	}
}