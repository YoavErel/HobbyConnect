package com.example.hobbyconnect.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hobbyconnect.R
import com.example.hobbyconnect.data.AppDatabase
import com.example.hobbyconnect.data.PostEntity
import com.example.hobbyconnect.models.FavoriteItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoritesAdapter(private var favoritesList: List<PostEntity>) :
    RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite, parent, false)
        return FavoritesViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoritesViewHolder, position: Int) {
        val post = favoritesList[position]
        holder.bind(post)
    }

    override fun getItemCount(): Int = favoritesList.size

    fun updateFavorites(newFavorites: List<PostEntity>) {
        favoritesList = newFavorites
        notifyDataSetChanged()
    }

    class FavoritesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: ImageView = itemView.findViewById(R.id.profileImage)
        private val userName: TextView = itemView.findViewById(R.id.userName)
        private val actionText: TextView = itemView.findViewById(R.id.actionText)
        private val postImage: ImageView = itemView.findViewById(R.id.postImage)

        fun bind(post: PostEntity) {
            userName.text = post.username
            actionText.text = "You liked this post"

            // Load avatar dynamically
            (itemView.context as? FragmentActivity)?.lifecycleScope?.launch(Dispatchers.IO) {
                val db = AppDatabase.getDatabase(itemView.context)
                val userEntity = db.userDao().getUserById(post.userId)
                val avatarUri = userEntity?.avatarUri

                withContext(Dispatchers.Main) {
                    Glide.with(itemView.context)
                        .load(avatarUri ?: R.drawable.profile_avatar) // Default if avatarUri is null
                        .placeholder(R.drawable.profile_avatar)
                        .circleCrop()
                        .into(profileImage)
                }
            }

            // Load post image using Glide
            Glide.with(itemView.context)
                .load(post.imageUrl)
                .into(postImage)
        }
    }
}


