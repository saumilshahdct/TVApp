package com.veeps.app.extension

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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