package com.adysun.chatapp.model

import android.graphics.Bitmap

data class ChatMessage(
    val text: String? = null,
    val image: Bitmap? = null,
    val isImage: Boolean = false,
    val isSent: Boolean = true
)
