package com.example.hobbyconnect.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hobbyconnect.R
import com.example.hobbyconnect.models.Post

class HomePostAdapter(private var posts: List<Post>,   private val onLikeToggle: (Post) -> Unit) :
    RecyclerView.Adapter<HomePostAdapter.ViewHolder>() {

    // Expose posts through a public getter
    fun getPosts(): List<Post> = posts

    fun updatePosts(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_post, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post, onLikeToggle)
    }

    override fun getItemCount(): Int = posts.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarImage: ImageView = itemView.findViewById(R.id.postProfileImage)
        private val postImage: ImageView = itemView.findViewById(R.id.postImage)
        private val postUsername: TextView = itemView.findViewById(R.id.postUsername)
        private val postDescription: TextView = itemView.findViewById(R.id.postDescription)
        private val likeButton: ImageView = itemView.findViewById(R.id.likeButton)

        fun bind(post: Post, onLikeToggle: (Post) -> Unit) {
            postUsername.text = post.username
            postDescription.text = post.description

            // Handle like button clicks
            likeButton.setOnClickListener {
                Log.d("HomePostAdapter", "Like button clicked for post ID: ${post.id}")
                onLikeToggle(post)
            }

            // Set initial like button state
            val likeDrawable = if (post.likedByCurrentUser) R.drawable.heart2 else R.drawable.like
            likeButton.setImageResource(likeDrawable)


            // Load avatar using Glide
            if (!post.avatarUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(post.avatarUrl)
                    .placeholder(R.drawable.profile_avatar) // Fallback placeholder
                    .circleCrop()
                    .into(avatarImage)
            } else {
                avatarImage.setImageResource(R.drawable.profile_avatar) // Default avatar
            }


            // Load post image using Glide
            if (!post.imageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(post.imageUrl)
                    .into(postImage)
            }
        }
    }
}
