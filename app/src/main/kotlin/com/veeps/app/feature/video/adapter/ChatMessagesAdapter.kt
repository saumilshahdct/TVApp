package com.veeps.app.feature.video.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.veeps.app.databinding.RowChatMessageBinding
import com.veeps.app.feature.contentRail.model.ChatMessageItem


class ChatMessagesAdapter(
	private var messages: ArrayList<ChatMessageItem> = arrayListOf(),
) : RecyclerView.Adapter<ChatMessagesAdapter.ViewHolder>() {
	override fun onCreateViewHolder(
		parent: ViewGroup, viewType: Int,
	): ViewHolder {
		return ViewHolder(
			RowChatMessageBinding.inflate(
				LayoutInflater.from(parent.context), parent, false
			)
		)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		holder.binding.container.isSelected = messages[position].isArtist
		holder.binding.name.isSelected = messages[position].isArtist
		holder.binding.verifiedTick.visibility =
			if (messages[position].isArtist) View.VISIBLE else View.GONE
		holder.binding.name.text = messages[position].name
		holder.binding.message.text = messages[position].message
	}

	override fun getItemId(position: Int): Long {
		return position.toLong()
	}

	override fun getItemCount() = messages.size

	fun addMessage(message: ChatMessageItem) {
		messages.add(message)
		notifyItemInserted(messages.size)
	}

	fun addMessages(messages: ArrayList<ChatMessageItem>) {
		this.messages = messages
		val count = messages.size
		notifyItemRangeChanged(0, count)
	}

	inner class ViewHolder(val binding: RowChatMessageBinding) :
		RecyclerView.ViewHolder(binding.root)
}