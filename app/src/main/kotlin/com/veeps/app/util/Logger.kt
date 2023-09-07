package com.veeps.app.util

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.io.IOException
import kotlin.math.min

object Logger {

	fun print(message: Any) {
		Log.d(AppConstants.TAG, message.toString())
	}

	fun printError(message: Any) {
		Log.e(AppConstants.TAG, message.toString())
	}

	fun printAPILogs(
		chain: Interceptor.Chain, response: Response, bodyString: String, tookMs: Long, tookS: Long
	) {
		print("Request URL :: ${chain.request().method} ${chain.request().url} \n\n")
		print("Request Time :: $tookS Seconds ( $tookMs ms ) \n\n")
		for (i in 0 until chain.request().headers.size) {
			print(
				"Request Headers :: ${chain.request().headers.name(i)} : ${
					chain.request().headers.value(
						i
					)
				} \n\n"
			)
		}
		try {
			val buffer = Buffer()
			val body = chain.request().body
			body?.writeTo(buffer)
			print("Request Body :: ${if (buffer.size != 0L) buffer.readUtf8() else "null"} \n\n")
		} catch (e: IOException) {
			e.printStackTrace()
			print("EXCEPTION Occurred while printing API Logs -- " + e.message + " \n\n")
		}
		print("Response Code :: ${response.code} \n\n")
		val maxLogSize = 4000
		for (i in 0..bodyString.length / maxLogSize) {
			val start = i * maxLogSize
			var end = (i + 1) * maxLogSize
			end = min(end, bodyString.length)
			print((if (i == 0) "Response :: " else "") + bodyString.substring(start, end))
		}
	}
}