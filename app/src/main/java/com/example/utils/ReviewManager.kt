package com.example.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import com.google.android.play.core.review.ReviewManagerFactory

object ReviewManager {
    private fun Context.findActivity(): Activity? {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        return null
    }

    fun askForReview(context: Context) {
        val activity = context.findActivity() ?: return
        try {
            val manager = ReviewManagerFactory.create(context)
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val reviewInfo = task.result
                    val flow = manager.launchReviewFlow(activity, reviewInfo)
                    flow.addOnCompleteListener { _ ->
                        Log.d("ReviewManager", "Review flow completed")
                    }
                } else {
                    Log.e("ReviewManager", "Review task failed")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
