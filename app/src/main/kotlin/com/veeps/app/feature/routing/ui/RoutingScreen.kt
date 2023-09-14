package com.veeps.app.feature.routing.ui

import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.OnBackPressedCallback
import com.veeps.app.R
import com.veeps.app.core.BaseActivity
import com.veeps.app.databinding.ActivityRoutingScreenBinding
import com.veeps.app.extension.openActivity
import com.veeps.app.extension.showToast
import com.veeps.app.feature.routing.viewModel.RoutingViewModel
import com.veeps.app.util.AppConstants
import com.veeps.app.util.AppPreferences
import com.veeps.app.util.Logger
import kotlin.system.exitProcess

class RoutingScreen : BaseActivity<RoutingViewModel, ActivityRoutingScreenBinding>() {

	override fun isSplashScreenRequired(): Boolean {
		return true
	}

	override fun getViewBinding(): ActivityRoutingScreenBinding {
		return ActivityRoutingScreenBinding.inflate(layoutInflater)
	}

	override fun getBackCallback(): OnBackPressedCallback {
		val backPressedCallback = object : OnBackPressedCallback(true) {
			override fun handleOnBackPressed() {
				Logger.print("Back Pressed on RoutingScreen. Finishing Activity")
				finishAffinity()                /* Comment: To Exit the app process as well */
				exitProcess(0)
			}
		}
		return backPressedCallback
	}

	override fun showError(tag: String, message: String) {
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
		binding.negative.isEnabled = false
		binding.positive.tag = tag
		binding.negative.tag = tag
		binding.errorContainer.visibility = View.VISIBLE
		binding.positive.post {
			binding.positive.requestFocus()
		}
	}

	fun onErrorPositive(tag: Any?) {
		backPressedCallback.handleOnBackPressed()
	}

	fun onErrorNegative(tag: Any?) {
		backPressedCallback.handleOnBackPressed()
	}

	override fun onRendered(viewModel: RoutingViewModel, binding: ActivityRoutingScreenBinding) {
		binding.apply {
			routing = viewModel
			routingScreen = this@RoutingScreen
			lifecycleOwner = this@RoutingScreen
			errorContainer.visibility = View.GONE
		}
		notifyAppEvents()
		setupSplashScreenConfiguration()
		loadAppContent()
	}

	private fun notifyAppEvents() {

	}

	private fun loadAppContent() {
		viewModel.contentHasLoaded.observe(this@RoutingScreen) { contentHasLoaded ->
			if (contentHasLoaded) {
				if (intent != null && intent.hasExtra("type") && intent.getStringExtra("type")
						.equals(getString(R.string.session_expired_label))
				) {
					showToast(getString(R.string.session_expired_warning))
				}
				binding.logo.animate().scaleXBy(0.5f).scaleYBy(0.5f).setDuration(1500)
					.withEndAction {
						if (!AppPreferences.get(AppConstants.isUserAuthenticated, false)) {
							AppPreferences.removeAuthenticatedUser()
							openActivity<RoutingScreen>(true, Pair("type", ""))
						} else {
							openActivity<RoutingScreen>(true, Pair("type", ""))
						}
					}.start()
			}
		}
//		fetchGuestUserToken()
	}

	/*private fun fetchGuestUserToken() {
		viewModel.fetchGuestUserToken().observe(this@RoutingScreen) { guestUserTokenCall ->
			fetch(guestUserTokenCall, isLoaderEnabled = false, canUserAccessScreen = false) {
				guestUserTokenCall.response?.let { guestUserTokenResponse ->

					guestUserTokenResponse.data?.let { guestUserToken ->
						AppPreferences.set(AppConstants.authenticatedUserToken, guestUserToken)
					} ?: showError(guestUserTokenCall.tag, getString(R.string.unknown_error))

					viewModel.contentHasLoaded.postValue(true)

				} ?: showError(guestUserTokenCall.tag, getString(R.string.unknown_error))
			}
		}
	}*/

	private fun setupSplashScreenConfiguration() {        /*Comment: This keeps splash open till manually moved to next activity
		splashScreen.setKeepOnScreenCondition { true }*/

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
