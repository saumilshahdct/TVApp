package com.veeps.app.extension

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.LayoutRes
import com.veeps.app.util.Logger

fun ViewGroup.inflate(@LayoutRes resourceId: Int) =
	LayoutInflater.from(context).inflate(resourceId, this, false)!!

fun EditText.clear() {
	this.setText("")
}

fun ImageView.loadImage(resource: Any) {
	when (resource) {
		is Drawable -> {
			Logger.print("Drawable to load")
		}

		is String -> {
			Logger.print("URL to load")
		}

		else -> {
			Logger.print("Unknown resource to load. Check resource type and add case.")
		}
	}
}