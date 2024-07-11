package com.veeps.app.feature.intro.ui

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import com.veeps.app.R
import com.veeps.app.core.BaseActivity
import com.veeps.app.core.BaseDataSource
import com.veeps.app.databinding.ActivityIntroScreenBinding
import com.veeps.app.extension.goToScreen
import com.veeps.app.feature.browse.ui.BrowseScreen
import com.veeps.app.feature.intro.viewModel.IntroViewModel
import com.veeps.app.feature.signIn.ui.SignInScreen
import com.veeps.app.util.APIConstants
import com.veeps.app.util.AppConstants
import com.veeps.app.util.AppPreferences
import com.veeps.app.util.DEFAULT
import com.veeps.app.util.Logger
import com.veeps.app.util.Screens
import com.veeps.app.util.Screens.APP_UPDATE
import kotlin.system.exitProcess


@UnstableApi
class IntroScreen : BaseActivity<IntroViewModel, ActivityIntroScreenBinding>() {

	private lateinit var player: ExoPlayer

	private fun getBackCallback(): OnBackPressedCallback {
		val backPressedCallback = object : OnBackPressedCallback(true) {
			override fun handleOnBackPressed() {
				Logger.print("Back Pressed on ${this@IntroScreen.localClassName} Finishing Activity")
				if (isTaskRoot) {
					val homeIntent = Intent(Intent.ACTION_MAIN)
					homeIntent.addCategory(Intent.CATEGORY_HOME)
					homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
					startActivity(homeIntent)
				} else {
					finishAffinity()
					exitProcess(0)
				}
//				moveTaskToBack(true)
//				finish()
			}
		}
		return backPressedCallback
	}

	override fun getViewBinding(): ActivityIntroScreenBinding =
		ActivityIntroScreenBinding.inflate(layoutInflater)

	override fun showError(tag: String, message: String, description: String) {
		Logger.print("showError"+tag)

		when (tag) {
			APIConstants.validateAppVersions -> {
				Logger.print("APP_UPDATE")

				viewModel.isErrorVisible.postValue(true)
				viewModel.contentHasLoaded.postValue(true)
				viewModel.errorMessage.postValue(message)
				binding.errorDescription.text = description

				viewModel.errorPositiveLabel.postValue(getString(R.string.app_update))
				viewModel.errorNegativeLabel.postValue(getString(R.string.skip_update))
				viewModel.isErrorPositiveApplicable.postValue(true)
				viewModel.isErrorNegativeApplicable.postValue(true)
			}

			else -> viewModel.contentHasLoaded.postValue(true)
		}
	}

	override fun onRendered(viewModel: IntroViewModel, binding: ActivityIntroScreenBinding) {
		backPressedCallback = getBackCallback()
		onBackPressedDispatcher.addCallback(this, backPressedCallback)
		binding.apply {
			intro = viewModel
			introScreen = this@IntroScreen
			lifecycleOwner = this@IntroScreen
			signIn.requestFocus()
		}
	}

	private fun setupVideoPlayer() {
		releaseVideoPlayer()
		player = ExoPlayer.Builder(this@IntroScreen).build()
		player.setAudioAttributes(
			AudioAttributes.Builder().setUsage(C.USAGE_MEDIA)
				.setContentType(C.AUDIO_CONTENT_TYPE_MOVIE).build(), true
		)
		binding.videoPlayer.player = player
		player.repeatMode = Player.REPEAT_MODE_ALL
		val playbackUrl = RawResourceDataSource.buildRawResourceUri(R.raw.splash_video)
		player.setMediaItem(MediaItem.fromUri(playbackUrl))
		player.prepare()
		player.play()
	}

	override fun onResume() {
		super.onResume()
		validateAppVersion(
			APIConstants.validateAppVersions,
			"firetv",
			"prd",
			"1.4.0"
		)
		setupVideoPlayer()
	}

	override fun onStop() {
		releaseVideoPlayer()
		super.onStop()
	}

	override fun onDestroy() {
		releaseVideoPlayer()
		super.onDestroy()
	}

	private fun releaseVideoPlayer() {
		if (this::player.isInitialized) {
			player.playWhenReady = false
			player.pause()
			player.release()
		}
	}

	fun onSignIn() {
		goToScreen<SignInScreen>(
			false, Pair(AppConstants.TAG, Screens.SIGN_IN)
		)
	}

	private fun validateAppVersion(
		appVersionAPIURL: String,
		platform: String,
		stage: String,
		appVersion: String,
	) {
		viewModel.validateAppVersion(
			appVersionAPIURL,
			platform,
			stage,
			appVersion
		).observe(this@IntroScreen) { appVersionResponse ->
			fetch(
				appVersionResponse,
				isLoaderEnabled = false,
				canUserAccessScreen = false,
				shouldBeInBackground = false
			) {

				when (appVersionResponse.callStatus) {
					BaseDataSource.Resource.CallStatus.SUCCESS -> {
						Logger.print("App_update=SUCCESS")
					}

					BaseDataSource.Resource.CallStatus.ERROR -> {
						Logger.print("App_update=ERROR")

						val errorMessage = appVersionResponse.message
						if (errorMessage != null) {
							showError(APP_UPDATE, errorMessage, "")
						}
					}

					else -> Logger.doNothing()
				}
			}
		}
	}

	fun onErrorPositive(tag: Any?) {
		val amazonUrl = "amzn://apps/android?p=com.veeps.app"
		val intent = Intent(Intent.ACTION_VIEW)
		intent.setData(Uri.parse(amazonUrl))
		startActivity(intent)
	}

	fun onErrorNegative(tag: Any?) {
		binding.errorContainer.visibility = View.GONE
	}
}