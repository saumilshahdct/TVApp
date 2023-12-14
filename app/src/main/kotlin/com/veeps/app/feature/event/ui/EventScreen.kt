package com.veeps.app.feature.event.ui

import android.os.Bundle
import android.text.Html
import android.view.KeyEvent
import android.view.View
import android.view.ViewOutlineProvider
import androidx.core.content.ContextCompat
import androidx.leanback.widget.BaseGridView
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.util.EventLogger
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.SimpleItemAnimator
import com.amazon.device.iap.PurchasingService
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.veeps.app.R
import com.veeps.app.core.BaseFragment
import com.veeps.app.databinding.FragmentEventDetailsScreenBinding
import com.veeps.app.extension.isFireTV
import com.veeps.app.extension.loadImage
import com.veeps.app.feature.contentRail.adapter.ContentRailsAdapter
import com.veeps.app.feature.contentRail.model.Entities
import com.veeps.app.feature.contentRail.model.Products
import com.veeps.app.feature.contentRail.model.RailData
import com.veeps.app.feature.event.viewModel.EventViewModel
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
import com.veeps.app.util.Logger
import com.veeps.app.util.Screens
import com.veeps.app.widget.navigationMenu.NavigationItems
import io.noties.markwon.Markwon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.internal.http.HTTP_GONE
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.json.JSONObject

class EventScreen : BaseFragment<EventViewModel, FragmentEventDetailsScreenBinding>() {

	private lateinit var player: ExoPlayer
	private var entity = ""
	private var entityId = ""
	private var entityScope = ""
	private var posterImage = ""
	private var product: Products = Products()
	private var eventDetails: Entities = Entities()
	private val action by lazy {
		object : AppAction {
			override fun onAction() {
				Logger.print(
					"Action performed on ${
						this@EventScreen.javaClass.name.substringAfterLast(".")
					}"
				)
			}
		}
	}

	override fun getViewBinding(): FragmentEventDetailsScreenBinding =
		FragmentEventDetailsScreenBinding.inflate(layoutInflater)

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.apply {
			event = viewModel
			eventScreen = this@EventScreen
			lifecycleOwner = viewLifecycleOwner
			lifecycle.addObserver(viewModel)
			loader.visibility = View.GONE
			paymentLoader.visibility = View.GONE
			ctaContainer.visibility = View.INVISIBLE
			primary.requestFocus()
		}
		notifyAppEvents()
		loadAppContent()
	}

	private fun setupVideoPlayer() {
		releaseVideoPlayer()
		player = context?.let { context -> ExoPlayer.Builder(context).build() }!!
		player.addListener(object : Player.Listener {
			override fun onIsPlayingChanged(isPlaying: Boolean) {
				if (isPlaying) {
					binding.heroImage.visibility = View.GONE
				} else {
					binding.heroImage.visibility = View.VISIBLE
				}
				super.onIsPlayingChanged(isPlaying)
			}

			override fun onPlaybackStateChanged(playbackState: Int) {
				if (playbackState == Player.STATE_ENDED) {
					binding.heroImage.visibility = View.VISIBLE
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
			AudioAttributes.Builder().setUsage(C.USAGE_MEDIA)
				.setContentType(C.AUDIO_CONTENT_TYPE_MOVIE).build(), true
		)
		player.addAnalyticsListener(EventLogger())
		binding.videoPlayer.player = player
		player.repeatMode = Player.REPEAT_MODE_ONE
		viewModel.trailer.value?.let {
			if (it.isNotBlank()) {
				player.setMediaItem(MediaItem.fromUri(it))
				player.prepare()
				player.play()
				if (homeViewModel.isNavigationMenuVisible.value!!) {
					if (this::player.isInitialized && player.isPlaying) {
						player.pause()
					}
				}
			}
		}
	}

	private fun releaseVideoPlayer() {
		if (this::player.isInitialized) {
			player.playWhenReady = false
			player.pause()
			player.release()
		}
	}

	private fun setupBlur() {
		binding.primary.setupWith(binding.layoutContainer).setBlurRadius(12.5f)
		binding.primary.outlineProvider = ViewOutlineProvider.BACKGROUND
		binding.primary.clipToOutline = true
		binding.primary.setBlurEnabled(true)

		binding.myShows.setupWith(binding.layoutContainer).setBlurRadius(12.5f)
		binding.myShows.outlineProvider = ViewOutlineProvider.BACKGROUND
		binding.myShows.clipToOutline = true
		binding.myShows.setBlurEnabled(true)
	}

	private fun loadAppContent() {
		helper.completelyHideNavigationMenu()
		helper.fetchAllWatchListEvents()
		binding.primary.postDelayed({
			binding.primary.requestFocus()
		}, AppConstants.keyPressShortDelayTime)
		if (arguments != null) {
			entity = requireArguments().getString("entity").toString()
			entityId = requireArguments().getString("entityId").toString()
			viewModel.eventId = entityId
			entityScope = "$entity.$entityId"
			entity = entity.plus("s")
		}
		binding.primaryLabel.isSelected = false
		setupBlur()
		fetchEventStreamDetails()
	}

	private fun notifyAppEvents() {
		binding.primary.setOnFocusChangeListener { _, hasFocus ->
			context?.let { context ->
				if (binding.primary.tag == ButtonLabels.PLAY) {
					binding.primaryLabel.compoundDrawables.forEach { drawable ->
						drawable?.setTint(
							ContextCompat.getColor(
								context, if (hasFocus) R.color.dark_black else R.color.white
							)
						)
					}
				}
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
							context, if (hasFocus) R.color.dark_black else R.color.white
						)
					)
				}
				binding.myShowsLabel.setTextColor(
					ContextCompat.getColor(
						context, if (hasFocus) R.color.dark_black else R.color.white
					)
				)
			}
		}

		binding.primary.setOnKeyListener { _, keyCode, keyEvent ->
			if (keyEvent.action == KeyEvent.ACTION_DOWN) {
				if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
					if (viewModel.rail.value?.none { it.entities.isNotEmpty() } == true) {
						if (!binding.description.text.isNullOrBlank()) {
							binding.heroImage.loadImage(
								R.drawable.background_dark_black, ImageTags.HERO
							)
							releaseVideoPlayer()
							binding.description.requestFocus()
							binding.heroImage.visibility = View.GONE
							binding.logo.visibility = View.GONE
							binding.carousel.visibility = View.GONE
						}
					}
				}
			}
			return@setOnKeyListener false
		}

		binding.myShows.setOnKeyListener { _, keyCode, keyEvent ->
			if (keyEvent.action == KeyEvent.ACTION_DOWN) {
				if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
					if (viewModel.rail.value?.none { it.entities.isNotEmpty() } == true) {
						if (!binding.description.text.isNullOrBlank()) {
							binding.heroImage.loadImage(
								R.drawable.background_dark_black, ImageTags.HERO
							)
							releaseVideoPlayer()
							binding.description.requestFocus()
							binding.heroImage.visibility = View.GONE
							binding.logo.visibility = View.GONE
							binding.carousel.visibility = View.GONE
						}
					}
				}
			}
			return@setOnKeyListener false
		}

		binding.description.setOnKeyListener { _, keyCode, keyEvent ->
			if (keyEvent.action == KeyEvent.ACTION_DOWN) {
				if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
					binding.scrollView.pageScroll(View.FOCUS_DOWN)
				}
				if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
					if (binding.scrollView.scrollY == 0) {
						if (viewModel.rail.value?.none { it.entities.isNotEmpty() } == false) {
							binding.listing.visibility = View.VISIBLE
							binding.listing.requestFocus()
						} else {
							setupVideoPlayer()
							binding.heroImage.loadImage(posterImage, ImageTags.HERO)
							binding.heroImage.visibility = View.VISIBLE
							binding.logo.visibility = View.VISIBLE
							binding.carousel.visibility = View.VISIBLE
							binding.primary.postDelayed({
								binding.primary.requestFocus()
							}, AppConstants.keyPressShortDelayTime)
						}
					} else {
						binding.scrollView.pageScroll(View.FOCUS_UP)
					}
				}
			}
			return@setOnKeyListener false
		}

		viewModel.rail.observe(viewLifecycleOwner) { rails ->
			if (rails.none { it.entities.isNotEmpty() }) {
				binding.listing.visibility = View.GONE
			} else {
				binding.listing.apply {
					itemAnimator = null
					setNumColumns(1)
					setHasFixedSize(true)
					windowAlignment = BaseGridView.WINDOW_ALIGN_HIGH_EDGE
					windowAlignmentOffsetPercent = 0f
					isItemAlignmentOffsetWithPadding = true
					itemAlignmentOffsetPercent = 0f
					adapter = ContentRailsAdapter(rails = rails, helper, Screens.EVENT, action)
					onFlingListener = PagerSnapHelper()
				}
				binding.listing.visibility = View.VISIBLE
			}
		}

		homeViewModel.focusItem.observe(viewLifecycleOwner) { hasFocus ->
			if (hasFocus && !binding.description.text.isNullOrBlank()) {
				binding.description.requestFocus()
				if (viewModel.rail.value?.none { it.entities.isNotEmpty() } == false) {
					binding.listing.visibility = View.GONE
				} else {
					binding.heroImage.loadImage(R.drawable.background_dark_black, ImageTags.HERO)
					releaseVideoPlayer()
					binding.heroImage.visibility = View.GONE
					binding.logo.visibility = View.GONE
					binding.carousel.visibility = View.GONE
				}
			}
		}
		homeViewModel.isNavigationMenuVisible.observe(viewLifecycleOwner) { isNavigationMenuVisible ->
			if (isNavigationMenuVisible) {
				if (this::player.isInitialized && player.isPlaying) {
					player.pause()
				}
			} else {
				if (this::player.isInitialized && !player.isPlaying) {
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
				if (this::player.isInitialized && player.isPlaying) {
					player.pause()
				}
			} else {
				if (this::player.isInitialized && !player.isPlaying) {
					player.play()
				}
			}
		}
		homeViewModel.translateCarouselToTop.observe(viewLifecycleOwner) { shouldTranslate ->
			if (shouldTranslate && viewModel.isVisible.value == true) {
				binding.heroImage.loadImage(R.drawable.background_dark_black, ImageTags.HERO)
				releaseVideoPlayer()
				binding.heroImage.visibility = View.GONE
				binding.logo.visibility = View.GONE
				binding.carousel.visibility = View.GONE
			}
		}
		homeViewModel.translateCarouselToBottom.observe(viewLifecycleOwner) { shouldTranslate ->
			if (shouldTranslate && viewModel.isVisible.value == true) {
				setupVideoPlayer()
				binding.heroImage.loadImage(posterImage, ImageTags.HERO)
				binding.heroImage.visibility = View.VISIBLE
				binding.logo.visibility = View.VISIBLE
				binding.carousel.visibility = View.VISIBLE
				binding.primary.postDelayed({
					binding.primary.requestFocus()
				}, AppConstants.keyPressShortDelayTime)
			}
		}

		homeViewModel.purchaseAction.observe(viewLifecycleOwner) {
			if (it.isNullOrBlank()) {
				binding.paymentLoader.visibility = View.GONE
			} else {
				when (it) {
					"PURCHASED" -> {
						createOrder()
					}

					"FAILED" -> {
						helper.showErrorOnScreen(
							APIConstants.generateNewOrder, "Payment Failed. Please Try Again."
						)
						binding.paymentLoader.visibility = View.GONE
					}
				}
			}
		}

		viewModel.trailer.observe(viewLifecycleOwner) { playbackUrl ->
			if (playbackUrl.isNullOrBlank()) {
				releaseVideoPlayer()
			} else {
				setupVideoPlayer()
			}
		}

		viewModel.isVisible.observeForever  { isVisible ->
			Logger.print(
				"Visibility Changed to $isVisible On ${
					this@EventScreen.javaClass.name.substringAfterLast(".")
				}"
			)
			if (isVisible) {
				helper.selectNavigationMenu(NavigationItems.NO_MENU)
				helper.completelyHideNavigationMenu()
				if (this::player.isInitialized && !player.isPlaying && isVisible()) {
					player.play()
				}
			} else {
				if (this::player.isInitialized && player.isPlaying) {
					player.pause()
				}
			}
		}
	}

	private fun fetchEventProductDetails() {
		viewModel.fetchEventProductDetails().observe(viewLifecycleOwner) { eventProductResponse ->
			fetch(
				eventProductResponse,
				isLoaderEnabled = true,
				canUserAccessScreen = false,
				shouldBeInBackground = false
			) {
				eventProductResponse.response?.let { productsResponse ->
					if (productsResponse.products.isNotEmpty()) {
						productsResponse.products.forEach { product ->
							this.product = product
							binding.primaryLabel.text =
								ButtonLabels.BUY_TICKET.plus(product.displayPrice)
							if (context?.isFireTV == true) {
								CoroutineScope(Dispatchers.Main).launch {
									PurchasingService.getProductData(hashSetOf(product.productCode))
								}
							}
						}
					} else {
						binding.primaryLabel.text =
							ButtonLabels.UNAVAILABLE.also { binding.primaryLabel.isSelected }
					}
				} ?: run {
					binding.primaryLabel.text =
						ButtonLabels.UNAVAILABLE.also { binding.primaryLabel.isSelected }
				}
			}
		}
	}

	private fun fetchEventStreamDetails() {
		viewModel.fetchEventStreamDetails().observe(viewLifecycleOwner) { eventStreamResponse ->
			fetch(
				eventStreamResponse,
				isLoaderEnabled = true,
				canUserAccessScreen = false,
				shouldBeInBackground = false
			) {
				eventStreamResponse.response?.let { eventStreamData ->
					eventStreamData.data?.let { eventDetails ->
						viewModel.isEventPurchased = true
						setEventDetails(eventDetails)
					} ?: fetchEventDetails()
				} ?: fetchEventDetails()
			}
		}
	}

	private fun fetchEventDetails() {
		viewModel.fetchEventDetails().observe(viewLifecycleOwner) { eventResponse ->
			fetch(
				eventResponse,
				isLoaderEnabled = true,
				canUserAccessScreen = false,
				shouldBeInBackground = false
			) {
				eventResponse.response?.let { eventStreamData ->
					eventStreamData.data?.let { eventDetails ->
						setEventDetails(eventDetails)
					} ?: helper.goBack()
				} ?: helper.goBack()
			}
		}
	}

	private fun setEventDetails(eventDetails: Entities) {
		this.eventDetails = eventDetails
		binding.primary.alpha = 0.1f
		var primaryLabelText: String
		when (AppUtil.getPrimaryLabelText(eventDetails, Screens.EVENT, viewModel.isEventPurchased)
			.also {
				primaryLabelText = it
				binding.primary.tag = primaryLabelText
			}) {
			ButtonLabels.PLAY -> {
				binding.primaryLabel.compoundDrawablePadding =
					resources.getDimensionPixelSize(R.dimen.dp4)
				binding.primaryLabel.setCompoundDrawablesRelativeWithIntrinsicBounds(
					if (binding.primary.hasFocus()) R.drawable.play_black else R.drawable.play_white,
					0,
					0,
					0
				)
			}

			ButtonLabels.JOIN_LIVE -> {
				binding.primaryLabel.compoundDrawablePadding =
					resources.getDimensionPixelSize(R.dimen.dp8)
				binding.primaryLabel.setCompoundDrawablesRelativeWithIntrinsicBounds(
					R.drawable.rounded_card_background_red, 0, 0, 0
				)
			}

			ButtonLabels.BUY_TICKET -> {
				primaryLabelText = DEFAULT.EMPTY_STRING
				fetchEventProductDetails()
			}
		}
		binding.primaryLabel.text = primaryLabelText.also { label ->
			binding.primaryLabel.isSelected = label == ButtonLabels.UNAVAILABLE
		}
		binding.primary.alpha = 1.0f
		binding.myShows.visibility = if (AppPreferences.get(AppConstants.userSubscriptionStatus, "none") != "none") View.VISIBLE else View.GONE
		setupMyShows(isAdded = homeViewModel.watchlistIds.contains(eventDetails.id))

		val currentDate = DateTime.now()
		val streamStartAtDate =
			DateTime(eventDetails.eventStreamStartsAt?.ifBlank { DateTime.now().toString() }
				?: DateTime.now().toString(), DateTimeZone.UTC).withZone(
				DateTimeZone.getDefault()
			).toDateTime()
		binding.date.text = streamStartAtDate.toString("MMM dd, yyyy${DEFAULT.SEPARATOR}ha")
		if (AppUtil.compare(
				streamStartAtDate, currentDate
			) == DateTimeCompareDifference.GREATER_THAN
		) {
			binding.date.visibility = View.VISIBLE
			binding.liveNow.visibility = View.GONE
		} else {
			binding.date.visibility = View.VISIBLE
			binding.liveNow.visibility = View.GONE
		}

		binding.venue.visibility = View.GONE
		val title = eventDetails.eventName ?: DEFAULT.EMPTY_STRING
		binding.title.text = title
		val description =
			eventDetails.eventDescription?.ifBlank { DEFAULT.EMPTY_STRING } ?: DEFAULT.EMPTY_STRING
		context?.let {
			Markwon.create(it).setMarkdown(binding.description, description)
		} ?: run {
			binding.description.text = Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY)
				.ifBlank { DEFAULT.EMPTY_STRING }
		}

		posterImage = eventDetails.presentation.posterUrl?.ifBlank { DEFAULT.EMPTY_STRING }
			?: DEFAULT.EMPTY_STRING
		val logoImage = eventDetails.presentation.logoUrl?.ifBlank { DEFAULT.EMPTY_STRING }
			?: DEFAULT.EMPTY_STRING
		binding.contentBadge.text = AppUtil.getContentBadge(eventDetails.presentation.contentBadges)

		val status = eventDetails.status?.lowercase() ?: DEFAULT.EMPTY_STRING
		if (eventDetails.eventReWatchDuration.isNullOrBlank()) {
			binding.reWatch.text = "Rewatch is not available for this event."
		} else {
			val reWatchDuration = eventDetails.eventReWatchDuration!!.ifBlank { "0" }.toInt()
			if (reWatchDuration == 0) {
				binding.reWatch.text = "This event is live only. There will be no rewatch period."
			} else {
				val reWatchDurationString = AppUtil.calculateReWatchTime(reWatchDuration)
				when (status) {
					"on_demand" -> {
						binding.reWatch.text = if (eventDetails.watchUntil.isNullOrBlank()) {
							"Rewatch available for $reWatchDurationString after purchase."
						} else {
							"Rewatch until ${
								DateTime(eventDetails.watchUntil, DateTimeZone.UTC).withZone(
									DateTimeZone.getDefault()
								).toDateTime().toString("EEEE, MMM d, yyyy – h:mm a")
							}"
						}
					}

					"ended" -> {
						binding.reWatch.text = DEFAULT.EMPTY_STRING
					}

					else -> {
						binding.reWatch.text =
							"Rewatch available for $reWatchDuration after the start of the event."
					}
				}
			}
		}

		val artistEntities: ArrayList<Entities> = arrayListOf()
		eventDetails.lineup.forEach { lineup ->
			artistEntities.add(Entities(id = lineup.id?.ifBlank { DEFAULT.EMPTY_STRING }
				?: DEFAULT.EMPTY_STRING,
				name = lineup.name?.ifBlank { DEFAULT.EMPTY_STRING } ?: DEFAULT.EMPTY_STRING,
				landscapeUrl = lineup.landscapeUrl?.ifBlank { DEFAULT.EMPTY_STRING }
					?: DEFAULT.EMPTY_STRING,
				portraitUrl = lineup.portraitUrl?.ifBlank { DEFAULT.EMPTY_STRING }
					?: DEFAULT.EMPTY_STRING,
				logoUrl = lineup.logoUrl?.ifBlank { DEFAULT.EMPTY_STRING } ?: DEFAULT.EMPTY_STRING))
		}
		val artistRail = RailData(
			name = getString(
				R.string.featuring_artist
			),
			entities = artistEntities,
			cardType = CardTypes.CIRCLE,
			entitiesType = EntityTypes.ARTIST
		)
		val venueEntities: ArrayList<Entities> = arrayListOf()
		venueEntities.add(Entities(id = eventDetails.venueId,
			name = eventDetails.venueName,
			landscapeUrl = eventDetails.venueLandscapeUrl,
			portraitUrl = eventDetails.venuePortraitUrl?.ifBlank { DEFAULT.EMPTY_STRING }
				?: DEFAULT.EMPTY_STRING,
			logoUrl = eventDetails.venueLogoUrl?.ifBlank { DEFAULT.EMPTY_STRING }
				?: DEFAULT.EMPTY_STRING))
		val venueRail = RailData(
			name = getString(
				R.string.featuring_venue
			),
			entities = venueEntities,
			cardType = CardTypes.CIRCLE,
			entitiesType = EntityTypes.VENUE
		)
		val rails = ArrayList<RailData>()
		rails.add(artistRail)
		rails.add(venueRail)
		viewModel.rail.postValue(rails)

		val videoPreviewTreeMap = eventDetails.videoPreviews ?: false
		var trailer = DEFAULT.EMPTY_STRING
		if (videoPreviewTreeMap is LinkedTreeMap<*, *>) {
			if (videoPreviewTreeMap.isNotEmpty()) {
				val jsonObject = Gson().toJsonTree(videoPreviewTreeMap).asJsonObject
				if (jsonObject != null && !jsonObject.isJsonNull && !jsonObject.isEmpty) {
					val videoPreviewString: String = Gson().toJson(jsonObject)
					val videoPreview = JSONObject(videoPreviewString)
					if (videoPreview.has("high")) {
						trailer = videoPreview.getString("high")
					}
				}
			}
		}
		binding.carouselLogo.loadImage(logoImage, ImageTags.LOGO)
		binding.heroImage.loadImage(posterImage, ImageTags.HERO)
		viewModel.trailer.postValue(trailer)
		binding.primary.postDelayed({
			binding.ctaContainer.visibility = View.VISIBLE
			binding.primary.requestFocus()
		}, AppConstants.keyPressShortDelayTime)
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

	private fun claimFreeTicketForEvent() {
		viewModel.claimFreeTicketForEvent().observe(viewLifecycleOwner) { claimResponse ->
			fetch(
				claimResponse,
				isLoaderEnabled = true,
				canUserAccessScreen = true,
				shouldBeInBackground = true,
			) {
				claimResponse.response?.let { claimData ->
					fetchEventStreamDetails()
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

	private fun clearAllReservations() {
		viewModel.clearAllReservations().observe(viewLifecycleOwner) { clearAllReservations ->
			fetch(
				clearAllReservations,
				isLoaderEnabled = true,
				canUserAccessScreen = true,
				shouldBeInBackground = true
			) {
				setNewReservation()
			}
		}
	}

	private fun setNewReservation() {
		viewModel.setNewReservation(hashMapOf("item_id" to (product.id ?: "")))
			.observe(viewLifecycleOwner) { setNewReservation ->
				fetch(
					setNewReservation,
					isLoaderEnabled = true,
					canUserAccessScreen = true,
					shouldBeInBackground = true
				) {
					setNewReservation.response?.let {
						it.data?.let { reservation ->
							homeViewModel.reservedId = reservation.id
							generateNewOrder()
						}
					}
				}
			}
	}

	private fun generateNewOrder() {
		viewModel.generateNewOrder().observe(viewLifecycleOwner) { generateNewOrder ->
			fetch(
				generateNewOrder,
				isLoaderEnabled = true,
				canUserAccessScreen = true,
				shouldBeInBackground = true
			) {
				generateNewOrder.response?.let {
					it.data?.let { order ->
						homeViewModel.orderId = order.id
						if (context?.isFireTV == true) {
							PurchasingService.purchase(product.productCode)
						} else {
							binding.paymentLoader.visibility = View.GONE
						}
					} ?: run { binding.paymentLoader.visibility = View.GONE }
				} ?: run { binding.paymentLoader.visibility = View.GONE }
			}
		}
	}

	private fun createOrder() {
		viewModel.createOrder(
			hashMapOf(
				"order_id" to homeViewModel.orderId,
				"payment_id" to homeViewModel.receiptId,
				"vendor" to "fire_tv",
			)
		).observe(viewLifecycleOwner) { createOrder ->
			fetch(
				createOrder,
				isLoaderEnabled = true,
				canUserAccessScreen = true,
				shouldBeInBackground = true
			) {
				createOrder.response?.let {
					it.data?.let {
						fetchEventStreamDetails()
						binding.paymentLoader.visibility = View.GONE
					} ?: run { binding.paymentLoader.visibility = View.GONE }
				} ?: run { binding.paymentLoader.visibility = View.GONE }
			}
		}
	}

	fun onPrimaryClicked(tag: Any?) {
		when (tag) {
			ButtonLabels.BUY_TICKET -> {
				binding.paymentLoader.visibility = View.VISIBLE
				clearAllReservations()
			}

			ButtonLabels.PLAY -> {
				helper.goToVideoPlayer(viewModel.eventId)
			}

			ButtonLabels.JOIN_LIVE -> {
				helper.goToVideoPlayer(viewModel.eventId)
			}

			ButtonLabels.JOIN -> {
				val eventLogo = eventDetails.presentation.logoUrl?.ifBlank { DEFAULT.EMPTY_STRING }
					?: DEFAULT.EMPTY_STRING
				val eventId = eventDetails.eventId ?: eventDetails.id ?: DEFAULT.EMPTY_STRING
				val eventTitle =
					eventDetails.eventName?.ifBlank { DEFAULT.EMPTY_STRING } ?: DEFAULT.EMPTY_STRING
				val currentDate = DateTime.now()
				val streamStartsAt = eventDetails.eventStreamStartsAt ?: ""
				val doorOpensAt = eventDetails.eventDoorsAt ?: ""
				helper.goToWaitingRoom(eventId, eventLogo, eventTitle, doorOpensAt, streamStartsAt)
			}

			ButtonLabels.CLAIM_FREE_TICKET -> {
				claimFreeTicketForEvent()
			}
		}
	}
}