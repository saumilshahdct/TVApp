package com.veeps.app.extension

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.LayoutRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.veeps.app.R
import com.veeps.app.util.ImageTags
import com.veeps.app.util.IntValue
import com.veeps.app.util.Logger


fun ViewGroup.inflate(@LayoutRes resourceId: Int) =
	LayoutInflater.from(context).inflate(resourceId, this, false)!!

fun EditText.clear() {
	this.setText("")
}

fun ImageView.loadImage(resource: Any, tag: Any) {
	if (resource.toString().isNotBlank()) {
		when (resource) {
			is Drawable -> {
				this.setImageDrawable(resource)
				Logger.printMessage("Image drawable ($resource) is requested to load.")
			}

			is String -> {
				when (tag) {
					ImageTags.DEFAULT -> {
						Glide.with(this.context).load(resource)
							.diskCacheStrategy(DiskCacheStrategy.ALL).transition(withCrossFade())
							.thumbnail(
								Glide.with(context).asDrawable().load(resource)
									.sizeMultiplier(0.25f)
							).placeholder(android.R.color.transparent)
							.error(android.R.color.transparent).into(this)
					}

					ImageTags.QR -> {
						this.clipToOutline = true
						Glide.with(this.context).load(resource)
							.diskCacheStrategy(DiskCacheStrategy.ALL).transition(withCrossFade())
							.thumbnail(
								Glide.with(context).asDrawable().load(resource)
									.sizeMultiplier(0.25f).transform(
										CenterInside(), RoundedCorners(IntValue.NUMBER_5)
									)
							).transform(
								CenterInside(), RoundedCorners(IntValue.NUMBER_5)
							).placeholder(this.drawable)
							.error(R.drawable.qr_code_background_transparent).into(this)
					}

					ImageTags.HEADER -> {
						this.clipToOutline = true
						Glide.with(this.context).load(resource)
							.diskCacheStrategy(DiskCacheStrategy.ALL).transition(withCrossFade())
							.thumbnail(
								Glide.with(context).asDrawable().load(resource)
									.sizeMultiplier(0.25f).transform(
										CenterInside(), RoundedCorners(IntValue.NUMBER_10)
									)
							).transform(
								CenterInside(), RoundedCorners(IntValue.NUMBER_10)
							).placeholder(this.drawable)
							.error(R.drawable.qr_code_background_transparent).into(this)
					}

					ImageTags.ROUNDED -> {
						this.clipToOutline = true
						Glide.with(this.context).load(resource)
							.diskCacheStrategy(DiskCacheStrategy.ALL).transition(withCrossFade())
							.thumbnail(
								Glide.with(context).asDrawable().load(resource)
									.sizeMultiplier(0.25f).transform(
										CenterInside(), RoundedCorners(IntValue.NUMBER_100)
									)
							).transform(
								CenterInside(), RoundedCorners(IntValue.NUMBER_100)
							).placeholder(this.drawable)
							.error(R.drawable.qr_code_background_transparent).into(this)
					}
				}
				Logger.printMessage("Image url ($resource) is requested to load.")
			}

			is Int -> {
				this.setImageResource(resource)
				Logger.printMessage("Image app resource ($resource) is requested to load.")
			}

			is Bitmap -> {
				this.setImageBitmap(resource)
				Logger.printMessage("Image bitmap ($resource) is requested to load.")
			}

			else -> {
				Logger.printMessage("Unknown image resource is requested to load. Check resource type and add equivalent case.")
			}
		}
	}
}