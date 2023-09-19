package com.veeps.app.feature.intro.ui

import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.OnBackPressedCallback
import com.veeps.app.R
import com.veeps.app.core.BaseActivity
import com.veeps.app.databinding.ActivityIntroScreenBinding
import com.veeps.app.extension.goToScreen
import com.veeps.app.feature.home.ui.HomeScreen
import com.veeps.app.feature.intro.viewModel.IntroViewModel
import com.veeps.app.feature.signIn.ui.SignInScreen
import com.veeps.app.util.AppConstants
import com.veeps.app.util.AppPreferences
import com.veeps.app.util.IntValue
import com.veeps.app.util.Logger
import com.veeps.app.util.Screens
import kotlin.system.exitProcess


class IntroScreen : BaseActivity<IntroViewModel, ActivityIntroScreenBinding>() {
	override fun isSplashScreenRequired(): Boolean {
		return true
	}

	override fun getBackCallback(): OnBackPressedCallback {
		val backPressedCallback = object : OnBackPressedCallback(true) {
			override fun handleOnBackPressed() {
				Logger.print("Back Pressed on ${this@IntroScreen.localClassName} Finishing Activity")
//				finish()
				finishAffinity()
				exitProcess(0)
			}
		}
		return backPressedCallback
	}

	override fun getViewBinding(): ActivityIntroScreenBinding = ActivityIntroScreenBinding.inflate(layoutInflater)

	override fun showError(tag: String, message: String) {
		setupBlurView()
		viewModel.contentHasLoaded.postValue(true)
		viewModel.errorMessage.postValue(message)
		when (tag) {
			else -> {
				viewModel.errorPositiveLabel.postValue(getString(R.string.ok_label))
				viewModel.errorNegativeLabel.postValue(getString(R.string.cancel_label))
				viewModel.isErrorPositiveApplicable.postValue(true)
				viewModel.isErrorNegativeApplicable.postValue(true)
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

	override fun onRendered(viewModel: IntroViewModel, binding: ActivityIntroScreenBinding) {
		binding.apply {
			intro = viewModel
			introScreen = this@IntroScreen
			lifecycleOwner = this@IntroScreen
			errorContainer.visibility = View.GONE
			signIn.requestFocus()
		}
		notifyAppEvents()
		setupSplashScreenConfiguration()
		loadAppContent()
	}

	private fun loadAppContent() {
		if (AppPreferences.get(AppConstants.isUserAuthenticated, false) && !AppPreferences.get(
				AppConstants.authenticatedUserToken,
				""
			).isNullOrEmpty()
		) {
			goToScreen<HomeScreen>(true, AppConstants.TAG to Screens.BROWSE)
		} else
			Handler(Looper.getMainLooper()).postDelayed({
				viewModel.contentHasLoaded.postValue(true)
			}, IntValue.NUMBER_2000.toLong())
	}

	private fun notifyAppEvents() {

	}

	fun onSignIn() {
		goToScreen<SignInScreen>(
			false, Pair(AppConstants.TAG, Screens.SIGN_IN)
		)
	}

	private fun setupBlurView() {
		binding.errorContainer.setupWith(binding.container).setBlurRadius(12.5f)
	}

	private fun setupSplashScreenConfiguration() {        /* Comment: This keeps splash open till manually moved to next activity
		splashScreen.setKeepOnScreenCondition { true } */

		val content: View = findViewById(android.R.id.content)
		content.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
			override fun onPreDraw(): Boolean {
				return if (viewModel.contentHasLoaded.value!!) {
					content.viewTreeObserver.removeOnPreDrawListener(this)
					true
				} else false
			}
		})
	}

}