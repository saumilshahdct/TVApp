package com.veeps.app.feature.search.ui

import android.os.Bundle
import android.view.View
import com.veeps.app.core.BaseFragment
import com.veeps.app.databinding.FragmentSearchScreenBinding
import com.veeps.app.feature.home.ui.HomeScreen
import com.veeps.app.feature.search.viewModel.SearchViewModel
import com.veeps.app.util.Logger
import com.veeps.app.util.Screens
import com.veeps.app.widget.navigationMenu.NavigationItems

class SearchScreen : BaseFragment<SearchViewModel, FragmentSearchScreenBinding>() {
	override fun getViewBinding(): FragmentSearchScreenBinding =
		FragmentSearchScreenBinding.inflate(layoutInflater)

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.apply {
			search = viewModel
			searchScreen = this@SearchScreen
			lifecycleOwner = this@SearchScreen
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
			if (isVisible) helper.selectNavigationMenu(NavigationItems.SEARCH_MENU)
			Logger.print(
				"Visibility Changed to $isVisible On ${
					this@SearchScreen.javaClass.name.substringAfterLast(
						"."
					)
				}"
			)
		}
	}
}