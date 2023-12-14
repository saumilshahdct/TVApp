package com.veeps.app.feature.video.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.annotation.OptIn
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.C.TRACK_TYPE_TEXT
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.util.EventLogger
import androidx.media3.ui.TimeBar
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.pubnub.api.PNConfiguration
import com.pubnub.api.PubNub
import com.pubnub.api.UserId
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.enums.PNHeartbeatNotificationOptions
import com.pubnub.api.enums.PNLogVerbosity
import com.pubnub.api.enums.PNReconnectionPolicy
import com.pubnub.api.enums.PNStatusCategory
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.pubsub.PNMessageResult
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult
import com.pubnub.api.models.consumer.pubsub.PNSignalResult
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult
import com.pubnub.api.models.consumer.pubsub.objects.PNObjectEventResult
import com.veeps.app.R
import com.veeps.app.core.BaseActivity
import com.veeps.app.databinding.ActivityVideoPlayerScreenBinding
import com.veeps.app.extension.round
import com.veeps.app.extension.setHorizontalBias
import com.veeps.app.feature.video.model.Subtitle
import com.veeps.app.feature.video.viewModel.VideoPlayerViewModel
import com.veeps.app.util.AppConstants
import com.veeps.app.util.AppPreferences
import com.veeps.app.util.DEFAULT
import com.veeps.app.util.GlideThumbnailTransformation
import com.veeps.app.util.IntValue
import com.veeps.app.util.Logger
import org.joda.time.Period
import org.joda.time.format.PeriodFormatterBuilder


@OptIn(UnstableApi::class)
class VideoPlayerScreen : BaseActivity<VideoPlayerViewModel, ActivityVideoPlayerScreenBinding>() {

	private lateinit var getCurrentTimer: Handler
	private lateinit var timerTask: Runnable
	private lateinit var player: ExoPlayer
	private var timeout = Handler(Looper.getMainLooper())
	private val inactivitySeconds = 10
	private var scrubbedPosition = 0L
	var trickPlayVisible = MutableLiveData<Boolean>()
	private val trickPlayRunnable = Runnable { trickPlayVisible.setValue(false) }
	private lateinit var pubnub: PubNub
	private lateinit var pubNubListener: SubscribeCallback

	private fun getBackCallback(): OnBackPressedCallback {
		val backPressedCallback = object : OnBackPressedCallback(true) {
			override fun handleOnBackPressed() {
				Logger.print(
					"Back Pressed on ${
						this@VideoPlayerScreen.localClassName.substringAfterLast(
							"."
						)
					} Finishing Activity"
				)
				releaseVideoPlayer()
				finish()
			}
		}
		return backPressedCallback
	}

	override fun getViewBinding(): ActivityVideoPlayerScreenBinding =
		ActivityVideoPlayerScreenBinding.inflate(layoutInflater)

	override fun onRendered(
		viewModel: VideoPlayerViewModel, binding: ActivityVideoPlayerScreenBinding
	) {
		backPressedCallback = getBackCallback()
		onBackPressedDispatcher.addCallback(this, backPressedCallback)
		binding.apply {
			video = viewModel
			videoPlayerScreen = this@VideoPlayerScreen
			lifecycleOwner = this@VideoPlayerScreen
			errorContainer.visibility = View.GONE
			loader.visibility = View.GONE
			progress.hideScrubber(500)
			imagePreview.visibility = View.GONE
			seekDuration.visibility = View.GONE
			vodControls.visibility = View.GONE
			liveControls.visibility = View.GONE
			topControls.visibility = View.GONE
			playPause.requestFocus()
		}
		notifyAppEvents()
		loadAppContent()
	}

	private fun loadAppContent() {
		Logger.printWithTag("saumil", "Video Player -- ${intent.hasExtra("eventId")} -- ${viewModel.eventId.value}")
		if (intent != null && intent.hasExtra("eventId")) {
			setupVideoPlayer()
			viewModel.eventId.postValue(intent.getStringExtra("eventId"))
			Logger.printWithTag("saumil", "Video Player -- ${viewModel.eventId.value}")
		}
	}

	private fun notifyAppEvents() {
		getCurrentTimer = Handler(Looper.getMainLooper())

		setupBlur()

		binding.playPause.setOnClickListener { v ->
			if (binding.playPause.isSelected) {
				binding.videoPlayer.player?.pause().also { binding.playPause.isSelected = false }
			} else {
				binding.videoPlayer.player?.play().also { binding.playPause.isSelected = true }
			}
		}

		// Hiding the trickBar to disable the keys when the hiding animation is complete
		binding.controls.animate().setListener(object : AnimatorListenerAdapter() {
			override fun onAnimationEnd(animation: Animator) {
				super.onAnimationEnd(animation)
				if (trickPlayVisible.value != null && !trickPlayVisible.value!!) {
					binding.controls.visibility = View.GONE
				}
			}
		})

		trickPlayVisible.observe(this@VideoPlayerScreen) { visible: Boolean ->
			if (visible) {
				binding.controls.visibility = View.VISIBLE
				binding.playPause.requestFocus()
				binding.controls.animate().translationY(0F).alpha(1F)
			} else {
				binding.controls.animate().translationY(1F).alpha(0F)
			}
		}

		viewModel.eventId.observe(this@VideoPlayerScreen) { eventId ->
			if (!eventId.isNullOrBlank()) {
				fetchEventPlaybackDetails(eventId)
			}
		}

		viewModel.playbackURL.observe(this@VideoPlayerScreen) { playbackURL ->
			if (!playbackURL.isNullOrBlank()) {
				if (this::player.isInitialized) {
					//https://mtoczko.github.io/hls-test-streams/test-vtt/playlist.m3u8
					//https://cdn.bitmovin.com/content/assets/sintel/hls/playlist.m3u8
					//https://cdn.bitmovin.com/content/assets/sintel/sintel.mpd
					//https://mtoczko.github.io/hls-test-streams/test-gap/playlist.m3u8
					//https://mtoczko.github.io/hls-test-streams/test-group/playlist.m3u8
					//https://mtoczko.github.io/hls-test-streams/test-vtt-ts-segments/playlist.m3u8
					//https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8
					val mediaSource = HlsMediaSource.Factory(DefaultHttpDataSource.Factory())
						.setAllowChunklessPreparation(true)
						.createMediaSource(MediaItem.fromUri(Uri.parse(playbackURL)))
					player.setMediaSource(MergingMediaSource(mediaSource))
					player.prepare()
					player.play()
				}
			}
		}
	}

	private fun setupBlur() {
		binding.errorContainer.setupWith(binding.container).setBlurRadius(12.5f)
		binding.liveLabel.setupWith(binding.liveControls).setBlurRadius(12.5f)
	}

	private fun setupVideoPlayer() {
		releaseVideoPlayer()
		val trackSelector = DefaultTrackSelector(this@VideoPlayerScreen)
		player = ExoPlayer.Builder(this@VideoPlayerScreen).setTrackSelector(trackSelector).build()
		player.setSeekParameters(SeekParameters.EXACT)
		player.addListener(object : Player.Listener {
			override fun onTimelineChanged(timeline: Timeline, reason: Int) {
				super.onTimelineChanged(timeline, reason)
				if (player.isCurrentMediaItemLive) {
					initPubNub()
					binding.topControls.visibility = View.VISIBLE
					binding.liveControls.visibility = View.VISIBLE
					binding.vodControls.visibility = View.GONE
				} else {
					binding.topControls.visibility = View.VISIBLE
					binding.vodControls.visibility = View.VISIBLE
					binding.liveControls.visibility = View.GONE
				}
			}

			override fun onCues(cueGroup: CueGroup) {
				super.onCues(cueGroup)
				if (cueGroup.cues.isNotEmpty() && cueGroup.cues.isNotEmpty()) {
					for (i in 0 until cueGroup.cues.size) {
						Logger.printWithTag(
							"saumil", "Track Cue list content - " + cueGroup.cues[i].text
						)
					}
				}
			}

			override fun onIsPlayingChanged(isPlaying: Boolean) {
				Logger.printWithTag("saumil", "Playing Changed -- $isPlaying")
				if (isPlaying) {
					binding.loader.visibility = View.GONE
					binding.playPause.isSelected = true
					binding.videoPlayer.postDelayed(
						this@VideoPlayerScreen::getCurrentPlayerPosition,
						IntValue.NUMBER_1000.toLong()
					)
				} else {
					binding.playPause.isSelected = false
				}
				super.onIsPlayingChanged(isPlaying)
			}

			override fun onPlaybackStateChanged(playbackState: Int) {
				Logger.printWithTag(
					"saumil", "State Changed -- $playbackState -- ${player.isCurrentMediaItemLive}"
				)
				if (playbackState == Player.STATE_BUFFERING) {
					binding.loader.visibility = View.VISIBLE
				}
				if (playbackState == Player.STATE_ENDED) {
					binding.playPause.isSelected = false
				}
				super.onPlaybackStateChanged(playbackState)
			}

			override fun onPlayerError(error: PlaybackException) {
				super.onPlayerError(error)
				binding.playPause.isSelected = false
				Logger.printWithTag("saumil", "Error Out")
			}

			override fun onTracksChanged(tracks: Tracks) {
				super.onTracksChanged(tracks)
				if (tracks.isEmpty) {
					Logger.printWithTag("saumil", "tracks are empty, no subtitle button")
				} else {
					Logger.printWithTag("saumil", "tracks are not empty")
					if (tracks.containsType(TRACK_TYPE_TEXT)) {
						val subtitles: ArrayList<Subtitle> = arrayListOf()
						Logger.printWithTag(
							"saumil", "tracks have subtitles, can show subtitle button"
						)
						tracks.groups.forEach { group ->
							when (group.type) {
								TRACK_TYPE_TEXT -> {
									if (group.isSupported) {
										for (position in 0..<group.mediaTrackGroup.length) {
											val format = group.mediaTrackGroup.getFormat(position)
											val subtitle = Subtitle(
												id = format.id ?: DEFAULT.EMPTY_STRING,
												language = format.language ?: DEFAULT.EMPTY_STRING,
												label = format.label ?: DEFAULT.EMPTY_STRING,
												mediaGroup = group.mediaTrackGroup,
												trackPosition = position
											)
											subtitles.add(subtitle)
											Logger.printWithTag("saumil", subtitle)
										}
									}
								}
							}
						}

						if (subtitles.isEmpty()) {
							Logger.printWithTag(
								"saumil", "subtitles are not supported, no subtitle button"
							)
						} else {
							Logger.printWithTag(
								"saumil",
								"subtitles are supported, show subtitle button and listing"
							)
						}
					} else {
						Logger.printWithTag(
							"saumil", "tracks does not have subtitles, no subtitle button"
						)
					}
				}
			}
		})
		binding.progress.setOnFocusChangeListener { _, hasFocus ->
			if (hasFocus) {
				Logger.printWithTag("saumil2", "has focus")
				scrubbedPosition = player.currentPosition
				setImagePreview(true)
				binding.progress.showScrubber(500)
			} else {
				Logger.printWithTag("saumil2", "lost focus")
				scrubbedPosition = player.currentPosition
				setImagePreview(false)
				getCurrentPlayerPosition()
				player.playWhenReady = true
				binding.progress.hideScrubber(500)
			}
		}
		binding.progress.addListener(object : TimeBar.OnScrubListener {
			override fun onScrubStart(timeBar: TimeBar, position: Long) {
				Logger.printWithTag("saumil2", "scrubber start")
				scrubbedPosition = position
				setImagePreview(true)
				player.pause()
			}

			override fun onScrubMove(timeBar: TimeBar, position: Long) {
				Logger.printWithTag("saumil2", "scrubber move")
				scrubbedPosition = position
				setImagePreview(true)
			}

			override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
				Logger.printWithTag("saumil2", "scrubber stop")
				scrubbedPosition = position
				setImagePreview(true)
			}

		})
		player.setAudioAttributes(
			AudioAttributes.Builder().setUsage(C.USAGE_MEDIA)
				.setContentType(C.AUDIO_CONTENT_TYPE_MOVIE).build(), true
		)
		player.addAnalyticsListener(EventLogger())
		binding.videoPlayer.player = player
		player.repeatMode = Player.REPEAT_MODE_OFF

		// In INACTIVITY_SECONDS seconds of inactivity hide the trickBar
		timeout.postDelayed(trickPlayRunnable, (inactivitySeconds * 1000).toLong())
	}

	private fun initPubNub() {
		val myChannel = "awesomeChannel"
		val myMessage = JsonObject().apply {
			addProperty("msg", "Hello, world")
		}

		pubNubListener = object : SubscribeCallback() {
			override fun status(pubnub: PubNub, pnStatus: PNStatus) {
				if (pnStatus.category == PNStatusCategory.PNUnexpectedDisconnectCategory || pnStatus.category == PNStatusCategory.PNTimeoutCategory) {
					pubnub.reconnect()
				} else if (pnStatus.category === PNStatusCategory.PNConnectedCategory) {
					// Connect event. You can do stuff like publish, and know you'll get it.
					// Or just use the connected event to confirm you are subscribed for
					// UI / internal notifications, etc.
					pubnub.publish(channel = myChannel, message = myMessage)
						.async { result, status ->
							// the result is always of a nullable type
							// it's null if there were errors (status.error)
							// otherwise it's usable

							// handle publish result
							if (status.error) {
								// handle error
								status.exception?.printStackTrace()
							} else {
								println("Message timetoken: ${result!!.timetoken}")
							}
						}
				}

				println("Status category: ${pnStatus.category}")
				// PNConnectedCategory, PNReconnectedCategory, PNDisconnectedCategory

				println("Status operation: ${pnStatus.operation}")
				// PNSubscribeOperation, PNHeartbeatOperation

				println("Status error: ${pnStatus.error}")
				// true or false

			}

			override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {
				println("Presence event: ${pnPresenceEventResult.event}")
				println("Presence channel: ${pnPresenceEventResult.channel}")
				println("Presence uuid: ${pnPresenceEventResult.uuid}")
				println("Presence timetoken: ${pnPresenceEventResult.timetoken}")
				println("Presence occupancy: ${pnPresenceEventResult.occupancy}")
			}

			override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
				if (pnMessageResult.channel == myChannel) {
					println("Received message ${pnMessageResult.message.asJsonObject}")
					val receivedMessageObject: JsonElement = pnMessageResult.message
					println("Received message: $receivedMessageObject")
					// Extract desired parts of the payload, using Gson.
					// Extract desired parts of the payload, using Gson.
					val msg: String = pnMessageResult.message.asJsonObject.get("msg").asString
					println("The content of the message is: $msg")

					// log the following items with your favorite logger:
					// - message.getMessage()
					// - message.getSubscription()
					// - message.getTimetoken()

				}
				println("Message payload: ${pnMessageResult.message}")
				println("Message channel: ${pnMessageResult.channel}")
				println("Message publisher: ${pnMessageResult.publisher}")
				println("Message timetoken: ${pnMessageResult.timetoken}")
			}

			override fun signal(pubnub: PubNub, pnSignalResult: PNSignalResult) {
				println("Signal payload: ${pnSignalResult.message}")
				println("Signal channel: ${pnSignalResult.channel}")
				println("Signal publisher: ${pnSignalResult.publisher}")
				println("Signal timetoken: ${pnSignalResult.timetoken}")
			}

			override fun messageAction(
				pubnub: PubNub, pnMessageActionResult: PNMessageActionResult
			) {
				with(pnMessageActionResult.messageAction) {
					println("Message action type: $type")
					println("Message action value: $value")
					println("Message action uuid: $uuid")
					println("Message action actionTimetoken: $actionTimetoken")
					println("Message action messageTimetoken: $messageTimetoken")
				}

				println("Message action subscription: ${pnMessageActionResult.subscription}")
				println("Message action channel: ${pnMessageActionResult.channel}")
				println("Message action timetoken: ${pnMessageActionResult.timetoken}")
			}

			override fun objects(pubnub: PubNub, objectEvent: PNObjectEventResult) {
				println("Object event channel: ${objectEvent.channel}")
				println("Object event publisher: ${objectEvent.publisher}")
				println("Object event subscription: ${objectEvent.subscription}")
				println("Object event timetoken: ${objectEvent.timetoken}")
				println("Object event userMetadata: ${objectEvent.userMetadata}")

				with(objectEvent.extractedMessage) {
					println("Object event event: $event")
					println("Object event source: $source")
					println("Object event type: $type")
					println("Object event version: $version")
				}
			}
		}
		val config = PNConfiguration(
			UserId(
				AppPreferences.get(
					AppConstants.userID, getString(R.string.app_platform)
				) ?: getString(R.string.app_platform)
			)
		).apply {
			subscribeKey = "sub-c-84ee6f14-961d-11ea-a94f-52daec260573"
			publishKey = "pub-c-eb43a482-29d3-4ffd-8169-adf1cbde3e2d"
			secure = true
			logVerbosity = PNLogVerbosity.BODY
			heartbeatNotificationOptions = PNHeartbeatNotificationOptions.ALL
		}
		pubnub = PubNub(config)
		pubnub.addListener(pubNubListener)
		pubnub.configuration.reconnectionPolicy = PNReconnectionPolicy.LINEAR
		pubnub.configuration.maximumReconnectionRetries = 10
		pubnub.subscribe(channels = listOf(myChannel))
	}

	fun setSubtitles(needDisabled: Boolean, subtitle: Subtitle) {
		if (needDisabled) {
			player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
				.setTrackTypeDisabled(TRACK_TYPE_TEXT, /* disabled= */ true).build()
		} else {
			player.trackSelectionParameters =
				player.trackSelectionParameters.buildUpon().setTrackTypeDisabled(
					TRACK_TYPE_TEXT, /* disabled= */
					false
				).setOverrideForType(
					TrackSelectionOverride(
						subtitle.mediaGroup, /* trackIndex= */
						subtitle.trackPosition
					)
				).build()
		}
	}

	private fun setImagePreview(isVisible: Boolean) {
		if (!viewModel.tiles.value.isNullOrEmpty() && isVisible && binding.progress.hasFocus()) {
			val positionInPercentage =
				scrubbedPosition / (player.duration / IntValue.NUMBER_1000).toDouble().round(2)
					.toFloat()
			binding.vodControls.setHorizontalBias(R.id.image_preview, positionInPercentage)
			Glide.with(binding.imagePreview.context).load(viewModel.storyBoardURL.value ?: "")
				.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).transform(
					GlideThumbnailTransformation(
						scrubbedPosition,
						viewModel.tileWidth.value ?: 0,
						viewModel.tileHeight.value ?: 0,
						viewModel.tiles.value ?: arrayListOf(),
					)
				).into(binding.imagePreview)
			val seekDuration =
				PeriodFormatterBuilder().printZeroAlways().minimumPrintedDigits(2).appendHours()
					.appendSeparator(":").printZeroAlways().minimumPrintedDigits(2).appendMinutes()
					.appendSeparator(":").printZeroAlways().minimumPrintedDigits(2).appendSeconds()
					.toFormatter().print(
						Period.seconds(scrubbedPosition.toInt()).normalizedStandard()
					)
			binding.seekDuration.text = seekDuration ?: "00:00:00"
			binding.seekDuration.visibility = View.VISIBLE
			binding.imagePreview.visibility = View.VISIBLE
			Logger.printWithTag("saumil2", "should be visible")
		} else {
			binding.seekDuration.visibility = View.GONE
			binding.imagePreview.visibility = View.GONE
			Logger.printWithTag("saumil2", "should be gone")
		}
	}

	private fun releaseVideoPlayer() {
		if (this::player.isInitialized) {
			player.playWhenReady = false
			player.pause()
			player.release()
		}
	}

	private fun getCurrentPlayerPosition() {
		val bufferedPosition = player.bufferedPosition / IntValue.NUMBER_1000
		val currentPosition = player.currentPosition / IntValue.NUMBER_1000
		val totalDuration = player.duration / IntValue.NUMBER_1000
		scrubbedPosition = player.currentPosition
		binding.progress.setBufferedPosition(bufferedPosition)
		binding.progress.setPosition(currentPosition)
		binding.progress.setDuration(totalDuration)
		val currentDuration =
			PeriodFormatterBuilder().printZeroAlways().minimumPrintedDigits(2).appendHours()
				.appendSeparator(":").printZeroAlways().minimumPrintedDigits(2).appendMinutes()
				.appendSeparator(":").printZeroAlways().minimumPrintedDigits(2).appendSeconds()
				.toFormatter().print(
					Period.seconds((totalDuration - currentPosition).toInt()).normalizedStandard()
				)
		binding.currentDuration.text = currentDuration ?: "00:00:00"
		Logger.printWithTag(
			"saumil",
			"current pos: $currentPosition -- total duration: $totalDuration -- difference: ${totalDuration - currentPosition} -- Formatted String: ${currentDuration ?: "00:00:00"}"
		)
		if (player.isPlaying) {
			binding.videoPlayer.postDelayed(
				{ getCurrentPlayerPosition() }, IntValue.NUMBER_1000.toLong()
			)
		}
	}

	private fun fetchEventPlaybackDetails(eventId: String) {
		viewModel.fetchEventStreamDetails(eventId)
			.observe(this@VideoPlayerScreen) { eventStreamResponse ->
				fetch(
					eventStreamResponse,
					isLoaderEnabled = true,
					canUserAccessScreen = false,
					shouldBeInBackground = false
				) {
					eventStreamResponse.response?.let { eventStreamData ->
						eventStreamData.data?.let { eventDetails ->
							fetchStoryBoard(eventDetails.storyboards.json ?: "")
							binding.title.text =
								if (eventDetails.lineup.isNotEmpty()) "${eventDetails.lineup[0].name} - ${eventDetails.eventName}" else "${eventDetails.eventName}"
							viewModel.playbackURL.postValue(eventDetails.playback.streamUrl?.ifBlank { DEFAULT.EMPTY_STRING }
								?: DEFAULT.EMPTY_STRING)
						}
					}
				}
			}
	}

	private fun fetchStoryBoard(storyBoardURL: String) {
		viewModel.fetchStoryBoard(storyBoardURL)
			.observe(this@VideoPlayerScreen) { storyBoardResponse ->
				fetch(
					storyBoardResponse,
					isLoaderEnabled = true,
					canUserAccessScreen = true,
					shouldBeInBackground = true
				) {
					storyBoardResponse.response?.let { storyBoardImages ->
						if (storyBoardImages.tiles.isNotEmpty()) {
							viewModel.storyBoardURL.postValue(storyBoardImages.url)
							viewModel.tileWidth.postValue(storyBoardImages.tileWidth)
							viewModel.tileHeight.postValue(storyBoardImages.tileHeight)
							viewModel.tiles.postValue(storyBoardImages.tiles)
						}
					}
				}
			}
	}

	override fun showError(tag: String, message: String, description: String) {
		viewModel.contentHasLoaded.postValue(true)
		viewModel.errorMessage.postValue(message)
		when (tag) {
			else -> {
				viewModel.errorPositiveLabel.postValue(getString(R.string.ok_label))
				viewModel.errorNegativeLabel.postValue(getString(R.string.cancel_label))
				viewModel.isErrorPositiveApplicable.postValue(true)
				viewModel.isErrorNegativeApplicable.postValue(false)
			}
		}
		binding.positive.tag = tag
		binding.negative.tag = tag

		binding.positive.nextFocusUpId = binding.positive.id
		binding.positive.nextFocusDownId = binding.positive.id
		binding.positive.nextFocusLeftId = binding.positive.id
		binding.positive.nextFocusRightId =
			if (binding.negative.isEnabled) binding.negative.id else binding.positive.id
		binding.positive.nextFocusForwardId =
			if (binding.negative.isEnabled) binding.negative.id else binding.positive.id

		binding.negative.nextFocusUpId = binding.negative.id
		binding.negative.nextFocusDownId = binding.negative.id
		binding.negative.nextFocusLeftId =
			if (binding.positive.isEnabled) binding.positive.id else binding.negative.id
		binding.negative.nextFocusRightId = binding.negative.id
		binding.negative.nextFocusForwardId =
			if (binding.positive.isEnabled) binding.positive.id else binding.negative.id

		binding.errorContainer.visibility = View.VISIBLE
		binding.errorContainer.apply {
			alpha = 0f
			visibility = View.VISIBLE
			animate().alpha(1f)
				.setDuration(resources.getInteger(android.R.integer.config_shortAnimTime).toLong())
				.setListener(null)
		}
		binding.positive.postDelayed({
			binding.positive.requestFocus()
		}, AppConstants.keyPressShortDelayTime)
	}

	fun onErrorPositive(tag: Any?) {
		backPressedCallback.handleOnBackPressed()
	}

	fun onErrorNegative(tag: Any?) {
		backPressedCallback.handleOnBackPressed()
	}

	override fun onDestroy() {
		if (this::pubnub.isInitialized && this::pubNubListener.isInitialized) {
			pubnub.removeListener(listener = pubNubListener)
		}
		releaseVideoPlayer()
		super.onDestroy()
	}

	override fun onPause() {
		super.onPause()
		if (this::player.isInitialized && player.isPlaying) {
			player.pause()
		}
		if (this::getCurrentTimer.isInitialized && this::timerTask.isInitialized) {
			getCurrentTimer.removeCallbacks(timerTask)
			getCurrentTimer.removeCallbacksAndMessages(timerTask)
		}
	}

	override fun onResume() {
		super.onResume()
		if (this::player.isInitialized && !player.isPlaying) {
			player.play()
		}
		if (this::getCurrentTimer.isInitialized && this::timerTask.isInitialized) getCurrentTimer.post(
			timerTask
		)
	}

	@SuppressLint("RestrictedApi")
	override fun dispatchKeyEvent(event: KeyEvent): Boolean {
		// This method is called on key down and key up, so avoid being called twice
		if (event.action == KeyEvent.ACTION_DOWN) {
			if (handleUserInput(event.keyCode)) {
				return true
			}
		}
		// Make sure to return super.dispatchKeyEvent(event) so that any key not handled yet will work as expected
		return super.dispatchKeyEvent(event)
	}

	// region Private
	private fun handleUserInput(keycode: Int): Boolean {
		return when (keycode) {
			KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
				binding.playPause.performClick()
				true
			}

			KeyEvent.KEYCODE_MEDIA_PLAY -> {
				binding.videoPlayer.player?.play()
				true
			}

			KeyEvent.KEYCODE_MEDIA_PAUSE -> {
				binding.videoPlayer.player?.pause()
				true
			}

			KeyEvent.KEYCODE_MEDIA_STOP -> {
				binding.videoPlayer.player?.stop()
				true
			}

			KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER, KeyEvent.KEYCODE_SPACE -> {
				// When a key is hit, cancel the timeout of hiding the trick bar and set it again
				if (binding.progress.hasFocus()) {
					Logger.printWithTag("saumil2", "scrubber has focus and key")
					setImagePreview(false)
					player.seekTo(scrubbedPosition * 1000)
					player.playWhenReady = true
					binding.playPause.requestFocus()
					true
				} else {
					timeout.removeCallbacks(trickPlayRunnable)
					timeout.postDelayed(trickPlayRunnable, (inactivitySeconds * 1000).toLong())
					if (trickPlayVisible.value != null && !trickPlayVisible.value!!) {
						trickPlayVisible.value = true
						true
					} else return false
				}
			}

			else -> {
				timeout.removeCallbacks(trickPlayRunnable)
				timeout.postDelayed(trickPlayRunnable, (inactivitySeconds * 1000).toLong())
				if (trickPlayVisible.value != null && !trickPlayVisible.value!!) {
					trickPlayVisible.value = true
					true
				} else return false
			}
		}
	}

}