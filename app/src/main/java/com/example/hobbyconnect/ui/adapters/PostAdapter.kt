package com.example.hobbyconnect.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hobbyconnect.R
import com.example.hobbyconnect.models.Post

class PostAdapter(
    private var posts: List<Post>,
    private val onEdit: (Post) -> Unit, // Callback for edit action
    private val onDelete: (Post) -> Unit // Callback for delete action
) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    fun updatePosts(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post, onEdit, onDelete)
    }

    override fun getItemCount(): Int = posts.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val postImageView: ImageView = itemView.findViewById(R.id.postImageView)
        private val hobbyTextView: TextView = itemView.findViewById(R.id.hobbyTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
        private val editButton: ImageButton = itemView.findViewById(R.id.editButton)

        fun bind(post: Post, onEdit: (Post) -> Unit, onDelete: (Post) -> Unit) {
            hobbyTextView.text = post.hobby
            descriptionTextView.text = post.description

            Glide.with(itemView.context)
                .load(post.imageUrl) // Ensure this is the correct URL
                .into(postImageView)

            // Handle delete button click
            deleteButton.setOnClickListener {
                onDelete(post)
            }

            // Handle edit button click
            editButton.setOnClickListener {
                onEdit(post) // Call the edit callback with the post
            }
        }
    }
}
