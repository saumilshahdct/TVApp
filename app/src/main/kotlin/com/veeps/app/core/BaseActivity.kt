package com.veeps.app.core

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.core.widget.ContentLoadingProgressBar
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.veeps.app.R
import com.veeps.app.util.Logger
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import java.lang.reflect.ParameterizedType

abstract class BaseActivity<VM : ViewModel, VB : ViewDataBinding> : FragmentActivity() {

	lateinit var viewModel: VM
	lateinit var binding: VB
	lateinit var backPressedCallback: OnBackPressedCallback
	private lateinit var screenLoader: ContentLoadingProgressBar

	override fun attachBaseContext(newBase: Context?) {
		super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!))
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		if (isSplashScreenRequired()) {
			Logger.print("Splash Required -- ${isSplashScreenRequired()}")
			installSplashScreen()
		}
		super.onCreate(savedInstanceState)
		viewModel = ViewModelProvider(this)[getViewModelClass()]
		binding = getViewBinding()
		binding.lifecycleOwner = this        /*loader = Loader.getLoader(this)*/
		setContentView(binding.root)
		backPressedCallback = getBackCallback()
		onBackPressedDispatcher.addCallback(this, backPressedCallback)
		screenLoader = binding.root.findViewById(R.id.loader)
		onRendered(viewModel, binding)

	}

	abstract fun isSplashScreenRequired(): Boolean

	abstract fun getBackCallback(): OnBackPressedCallback

	private fun getViewModelClass(): Class<VM> {
		val type = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
		return type as Class<VM>
	}

	abstract fun getViewBinding(): VB

	open fun callback() {

	}

	open fun onRendered(viewModel: VM, binding: VB) {}

	open fun showError(tag: String, message: String) {}

	open fun <T> fetch(
		dataResource: BaseDataSource.Resource<T>,
		isLoaderEnabled: Boolean = true,
		canUserAccessScreen: Boolean = false,
		block: () -> Unit
	) {
		when (dataResource.callStatus) {
			BaseDataSource.Resource.CallStatus.SUCCESS -> {
				if (isLoaderEnabled) {
					screenLoader.show()
					screenLoader.visibility = View.VISIBLE
				}
				block.invoke()
				Logger.print("API Call is successful for ${dataResource.tag}")
			}

			BaseDataSource.Resource.CallStatus.LOADING -> {
				if (isLoaderEnabled && !screenLoader.isVisible) {
                    screenLoader.show()
					screenLoader.visibility = View.VISIBLE
                }
				Logger.print("API Call is loading for ${dataResource.tag}")
			}

			BaseDataSource.Resource.CallStatus.ERROR -> {
				if (isLoaderEnabled) {
					screenLoader.hide()
					screenLoader.visibility = View.GONE
				}
				dataResource.message?.let { message -> showError(dataResource.tag, message) }
					?: showError(dataResource.tag, getString(R.string.unknown_error))
				Logger.print("Error While Calling API for ${dataResource.tag} - " + dataResource.message)
			}
		}
	}
}
