package com.veeps.app.feature.intro.ui

import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.OnBackPressedCallback
import com.veeps.app.R
import com.veeps.app.core.BaseActivity
import com.veeps.app.databinding.ActivityIntroScreenBinding
import com.veeps.app.feature.intro.viewModel.IntroViewModel
import com.veeps.app.util.AppConstants
import com.veeps.app.util.Logger
import kotlin.system.exitProcess


class IntroScreen : BaseActivity<IntroViewModel, ActivityIntroScreenBinding>() {
	override fun isSplashScreenRequired(): Boolean {
		return true
	}

	override fun getBackCallback(): OnBackPressedCallback {
		val backPressedCallback = object : OnBackPressedCallback(true) {
			override fun handleOnBackPressed() {
				Logger.print("Back Pressed on ${this@IntroScreen.localClassName} Finishing Activity")
				finishAffinity()
				exitProcess(0)
			}
		}
		return backPressedCallback
	}

	override fun getViewBinding(): ActivityIntroScreenBinding {
		return ActivityIntroScreenBinding.inflate(layoutInflater)
	}

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
		}
		notifyAppEvents()
		setupSplashScreenConfiguration()
		loadAppContent()
	}

	private fun loadAppContent() {
		viewModel.contentHasLoaded.postValue(true)
	}

	private fun notifyAppEvents() {

	}

	fun onSignIn() {
		showError("SIGN_IN", "Error")
	}

	private fun setupBlurView() {
		binding.errorContainer.setupWith(binding.container).setBlurRadius(12.5f)
	}

	private fun setupSplashScreenConfiguration() {		/* Comment: This keeps splash open till manually moved to next activity
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