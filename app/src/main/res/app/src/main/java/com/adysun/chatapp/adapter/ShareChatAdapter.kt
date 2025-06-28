package com.adysun.chatapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.adysun.chatapp.R
import com.adysun.chatapp.model.ChatMessage

class ShareChatAdapter(private val messages: ArrayList<ChatMessage>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_SENT_TEXT = 0
        const val TYPE_RECEIVED_TEXT = 1
        const val TYPE_SENT_IMAGE = 2
        const val TYPE_RECEIVED_IMAGE = 3
    }

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return when {
            message.isImage && message.isSent -> TYPE_SENT_IMAGE
            message.isImage && !message.isSent -> TYPE_RECEIVED_IMAGE
            !message.isImage && message.isSent -> TYPE_SENT_TEXT
            else -> TYPE_RECEIVED_TEXT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        return when (viewType) {
            TYPE_SENT_TEXT -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.item_sent_text, parent, false)
                TextMessageViewHolder(view)
            }
            TYPE_RECEIVED_TEXT -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.item_received_text, parent, false)
                TextMessageViewHolder(view)
            }
            TYPE_SENT_IMAGE -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.item_sent_image, parent, false)
                ImageMessageViewHolder(view)
            }
            TYPE_RECEIVED_IMAGE -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.item_received_image, parent, false)
                ImageMessageViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is TextMessageViewHolder) {
            holder.textView.text = message.text
        } else if (holder is ImageMessageViewHolder) {
            holder.imageView.setImageBitmap(message.image)
        }
    }

    override fun getItemCount(): Int = messages.size

    class TextMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.textMessage)
    }

    class ImageMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageMessage)
    }
}
