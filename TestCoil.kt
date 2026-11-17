package com.example

import android.net.Uri

fun getUri(context: android.content.Context, resId: Int): Uri {
    return Uri.parse("android.resource://" + context.packageName + "/" + resId)
}
