package com.veeps.app.extension

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.veeps.app.R
import com.veeps.app.application.Veeps

val Context.isFreshInstall
	get() = with(packageManager.getPackageInfo(packageName, 0)) {
		firstInstallTime == lastUpdateTime
	}

val Context.isFireTV
	get() = packageManager.hasSystemFeature("amazon.hardware.fire_tv")

fun isAppConnected(): Boolean {
	var isOnline = false
	try {
		val manager =
			Veeps.appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
		val capabilities =
			manager.getNetworkCapabilities(manager.activeNetwork) // need ACCESS_NETWORK_STATE permission
		isOnline =
			capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
	} catch (e: java.lang.Exception) {
		e.printStackTrace()
	}
	return isOnline
}

fun goToPage(
	manager: FragmentManager,
	shouldReplace: Boolean,
	fragment: Class<out Fragment>,
	arguments: Bundle,
	tag: String,
	shouldAddToBackStack: Boolean
) {
	manager.commit {
		setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
		if (shouldReplace) {
			replace(
				R.id.fragment_container, fragment, arguments, tag
			)
		} else {
			add(
				R.id.fragment_container, fragment, arguments, tag
			)
		}
		if (shouldAddToBackStack) addToBackStack(tag)
	}
}

fun Int?.isGreaterThan(other: Int?) = this != null && other != null && this > other

fun Int?.isGreaterThanOrEqualTo(other: Int?) = this != null && other != null && this >= other

fun Int?.isLessThan(other: Int?) = this != null && other != null && this < other

fun Int?.isLessThanOrEqualTo(other: Int?) = this != null && other != null && this <= other

fun Int?.convertToMilli() = if (this != null && this.isGreaterThan(0)) this * 1000 else 0