package com.example.hobbyconnect.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hobbyconnect.R
import com.example.hobbyconnect.models.Post

class SearchAdapter(private var results: List<Post>) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    // Update the results list and refresh the RecyclerView
    fun updateResults(newResults: List<Post>) {
        results = newResults
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = results[position]
        holder.bindPost(post)
    }

    override fun getItemCount(): Int = results.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val postImage: ImageView = itemView.findViewById(R.id.postImage)
        private val resultTitle: TextView = itemView.findViewById(R.id.resultTitle)
        private val resultUsername: TextView = itemView.findViewById(R.id.resultUsername)

        fun bindPost(post: Post) {
            // Set post image
            val imageUrl = post.imageUrl
            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(imageUrl) // Load image from URL or file path
                    .placeholder(R.color.gray) // Placeholder while loading
                    .into(postImage)
            } else {
                postImage.setImageResource(R.color.gray) // Default placeholder
            }

            // Set post title and hobby
            val title = post.description ?: "Untitled" // Default title if null
            val hobby = post.hobby ?: "Unknown" // Default hobby if null
            resultTitle.text = "$title #$hobby"

            // Set username
            val username = post.username ?: "Anonymous" // Default username if null
            resultUsername.text = "@$username"
        }
    }
}
