package com.veeps.app.util

import android.content.Context
import android.os.Build
import eightbitlab.com.blurview.BlurAlgorithm
import eightbitlab.com.blurview.RenderEffectBlur
import eightbitlab.com.blurview.RenderScriptBlur


object AppUtil {
	fun getBlurAlgorithm(context: Context): BlurAlgorithm {
		val algorithm: BlurAlgorithm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			RenderEffectBlur()
		} else {
			RenderScriptBlur(context)
		}
		return algorithm
	}
}