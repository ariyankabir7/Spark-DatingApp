package com.ariyan.spark.ui.home.match

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ariyan.spark.R
import com.ariyan.spark.databinding.ItemMatchBinding
import com.ariyan.spark.model.MatchItem
import com.ariyan.spark.model.User
import com.ariyan.spark.utils.TimeUtils

class MatchAdapter(
    private val onClick: (User) -> Unit
) : ListAdapter<MatchItem, MatchAdapter.MatchViewHolder>(Diff) {

    object Diff : DiffUtil.ItemCallback<MatchItem>() {
        override fun areItemsTheSame(old: MatchItem, new: MatchItem) = old.user.uid == new.user.uid
        override fun areContentsTheSame(old: MatchItem, new: MatchItem) = old == new
    }

    inner class MatchViewHolder(private val binding: ItemMatchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MatchItem) {
            binding.tvName.text = "${item.user.name}, ${item.user.age}"
            binding.tvLastMsg.text = item.lastMessage.ifEmpty { "Say hi! 👋" }

            // Use the new TimeUtils to display presence (e.g., "Online")
            binding.tvTimestamp.text = item.lastSeen?.let {
                TimeUtils.getPresenceTimestamp(it * 1000)
            } ?: "" // Show nothing if never seen

            Glide.with(binding.root.context)
                .load(item.user.photoUrl)
                .placeholder(R.drawable.ic_person)
                .circleCrop()
                .into(binding.ivProfile)

            binding.root.setOnClickListener { onClick(item.user) }

            // TODO: Implement unread message logic later
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val binding = ItemMatchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MatchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
