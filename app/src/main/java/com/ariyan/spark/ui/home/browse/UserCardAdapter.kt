package com.ariyan.spark.ui.home.browse

import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ariyan.spark.R
import com.ariyan.spark.model.User
import com.bumptech.glide.Glide

class UserCardAdapter(
    private var users: List<User> = emptyList()
) : RecyclerView.Adapter<UserCardAdapter.UserCardViewHolder>() {

    fun submitList(newList: List<User>) {
        users = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserCardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_card, parent, false)
        return UserCardViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserCardViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    inner class UserCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val img: ImageView = itemView.findViewById(R.id.ivPhoto)
        private val nameAge: TextView = itemView.findViewById(R.id.tvNameAge)
        private val interests: TextView = itemView.findViewById(R.id.tvInterest)
        private val ivLike: ImageView = itemView.findViewById(R.id.ivLike)
        private val ivNope: ImageView = itemView.findViewById(R.id.ivNope)

        fun bind(user: User) {
            nameAge.text = "${user.name}, ${user.age}"
            interests.text = user.interests.joinToString(", ")

            Glide.with(itemView)
                .load(user.photoUrl)
                .placeholder(R.drawable.ic_person)
                .into(img)

            ivLike.visibility = View.GONE
            ivNope.visibility = View.GONE
        }

        fun showOverlay(isLike: Boolean) {
            val overlay = if (isLike) ivLike else ivNope
            overlay.visibility = View.VISIBLE
            overlay.alpha = 0.8f

            // hide after 1 second
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                overlay.visibility = View.GONE
            }, 1000)
        }
    }

    fun showSwipeOverlay(position: Int, isLike: Boolean) {
        if (position in users.indices) {
            (bindingRecyclerView?.findViewHolderForAdapterPosition(position) as? UserCardViewHolder)
                ?.showOverlay(isLike)
        }
    }

    // Helper for fragment to pass recycler
    private var bindingRecyclerView: RecyclerView? = null
    fun attachRecyclerView(rv: RecyclerView) {
        bindingRecyclerView = rv
    }
}