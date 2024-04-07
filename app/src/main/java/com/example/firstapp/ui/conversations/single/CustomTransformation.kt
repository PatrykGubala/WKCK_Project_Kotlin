package com.example.firstapp.ui.conversations.single

import android.graphics.Bitmap
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import java.nio.charset.Charset
import java.security.MessageDigest
import android.content.res.Resources

class CustomTransformation : BitmapTransformation() {

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(ID.toByteArray(Charset.forName("UTF-8")))
    }

    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val maxTargetWidthDP = 300
        val maxTargetHeightDP = 300

        val resources = Resources.getSystem()
        val maxTargetWidthPx = (maxTargetWidthDP * resources.displayMetrics.density).toInt()
        val maxTargetHeightPx = (maxTargetHeightDP * resources.displayMetrics.density).toInt()

        val aspectRatio = toTransform.width.toFloat() / toTransform.height.toFloat()
        val targetAspectRatio = maxTargetWidthPx.toFloat() / maxTargetHeightPx.toFloat()

        val width: Int
        val height: Int
        if (aspectRatio > targetAspectRatio) {
            width = maxTargetWidthPx
            height = (maxTargetWidthPx / aspectRatio).toInt()
        } else {
            width = (maxTargetHeightPx * aspectRatio).toInt()
            height = maxTargetHeightPx
        }

        return TransformationUtils.fitCenter(pool, toTransform, width, height)
    }

    companion object {
        private const val ID = "com.example.firstapp.ui.conversations.single.CustomTransformation"
    }
}
