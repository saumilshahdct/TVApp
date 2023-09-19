package com.veeps.app.feature.user.ui

import android.os.Bundle
import android.view.View
import com.veeps.app.core.BaseFragment
import com.veeps.app.databinding.FragmentProfileScreenBinding
import com.veeps.app.feature.user.viewModel.ProfileViewModel
import com.veeps.app.util.Logger
import com.veeps.app.widget.navigationMenu.NavigationItems

class ProfileScreen : BaseFragment<ProfileViewModel, FragmentProfileScreenBinding>() {
	override fun getViewBinding(): FragmentProfileScreenBinding =
		FragmentProfileScreenBinding.inflate(layoutInflater)

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.apply {
			profile = viewModel
			profileScreen = this@ProfileScreen
			lifecycleOwner = this@ProfileScreen
			lifecycle.addObserver(viewModel)
			loader.visibility = View.GONE
			label.requestFocus()
		}
		notifyAppEvents()
		loadAppContent()
	}

	private fun loadAppContent() {

	}

	private fun notifyAppEvents() {
		viewModel.isVisible.observe(viewLifecycleOwner) { isVisible ->
			if (isVisible) helper.selectNavigationMenu(NavigationItems.PROFILE_MENU)
			Logger.print(
				"Visibility Changed to $isVisible On ${
					this@ProfileScreen.javaClass.name.substringAfterLast(
						"."
					)
				}"
			)
		}
	}
}