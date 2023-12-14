package com.veeps.app.util

import android.graphics.Bitmap
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.veeps.app.feature.video.model.StoryBoardImagePosition
import java.nio.ByteBuffer
import java.security.MessageDigest

class GlideThumbnailTransformation(
	scrubPosition: Long, tileWidth: Int, tileHeight: Int, tiles: ArrayList<StoryBoardImagePosition>
) : BitmapTransformation() {

	private var x: Int = 0
	private var y: Int = 0
	var width: Int = 0
	var height: Int = 0

	init {
		width = tileWidth
		height = tileHeight
		for (position in tiles.indices) {
			try {
				if (scrubPosition >= tiles[position].start && scrubPosition <= tiles[position + 1].start) {
					x = tiles[position].x
					y = tiles[position].y
					break
				}
			} catch (e: Exception) {
				if (scrubPosition >= tiles[position].start) {
					x = tiles[position].x
					y = tiles[position].y
					break
				}
			}
		}
	}

	override fun updateDiskCacheKey(messageDigest: MessageDigest) {
		val data = ByteBuffer.allocate(8).putInt(x).putInt(y).array()
		messageDigest.update(data)
	}

	override fun transform(
		pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int
	): Bitmap {
		return Bitmap.createBitmap(toTransform, x, y, width, height)
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other == null || javaClass != other.javaClass) return false
		val that = other as GlideThumbnailTransformation
		return if (x != that.x) false else y == that.y
	}

	override fun hashCode(): Int {
		var result = x
		result = 31 * result + y
		return result
	}

}
