package com.example.hobbyconnect.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.hobbyconnect.R
import com.example.hobbyconnect.data.AppDatabase
import com.example.hobbyconnect.data.FirebaseModel
import com.example.hobbyconnect.models.Post
import com.example.hobbyconnect.ui.adapters.PostAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    private lateinit var profilePicture: ImageView
    private lateinit var nameTextView: TextView
    private lateinit var hobbiesTextView: TextView
    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var editProfileButton: ImageButton
    private lateinit var postsCountTextView: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private val firebaseModel = FirebaseModel()
    private var postAdapter: PostAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize views
        profilePicture = view.findViewById(R.id.profilePicture)
        nameTextView = view.findViewById(R.id.nameTextView)
        hobbiesTextView = view.findViewById(R.id.hobbiesTextView)
        postsRecyclerView = view.findViewById(R.id.postsRecyclerView)
        editProfileButton = view.findViewById(R.id.editProfileButton)
        postsCountTextView = view.findViewById(R.id.postsCountTextView)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh)


        // Setup RecyclerView
        postsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        postAdapter = PostAdapter(
            emptyList(),
            onEdit = { post -> navigateToEditPost(post) }, // Callback for editing
            onDelete = { post -> deletePost(post) }       // Callback for deleting
        )

        postsRecyclerView.adapter = postAdapter

        // Load user data and posts
        loadUserData()
        loadUserPosts()

        // Handle Edit Profile button
        editProfileButton.setOnClickListener {
            navigateToEditProfile(view)
        }

        swipeRefreshLayout.setOnRefreshListener {
            refreshData() // Call a method to reload data
        }

        return view
    }

    private fun loadUserData() {
        val currentUser = firebaseModel.getCurrentUser()
        if (currentUser != null) {
            Log.d("ProfileFragment", "Current User ID: ${currentUser.uid}")
            Log.d("ProfileFragment", "Firebase Username: ${currentUser.displayName}")

            val userId = currentUser.uid
            val db = AppDatabase.getDatabase(requireContext())

            lifecycleScope.launch(Dispatchers.IO) {
                val userEntity = db.userDao().getUserById(userId)
                Log.d("ProfileFragment", "User from DB: $userEntity")

                withContext(Dispatchers.Main) {
                    if (userEntity != null) {
                        nameTextView.text = userEntity.username ?: "No Username"
                        hobbiesTextView.text = userEntity.hobbies ?: "No hobbies yet"

                        // Show avatar if available
                        if (!userEntity.avatarUri.isNullOrEmpty()) {
                            Glide.with(requireContext())
                                .load(userEntity.avatarUri)
                                .placeholder(R.drawable.profile_avatar)
                                .into(profilePicture)
                        } else {
                            profilePicture.setImageResource(R.drawable.profile_avatar)
                        }
                    } else {
                        nameTextView.text = currentUser.displayName ?: "User"
                        Log.e("ProfileFragment", "User data not found in database.")
                    }
                }
            }
        } else {
            Log.e("ProfileFragment", "User not logged in!")
        }
    }




    private fun loadUserPosts() {
        val currentUser = firebaseModel.getCurrentUser()
        if (currentUser != null) {
            val userId = currentUser.uid // Fetch userId
            val db = AppDatabase.getDatabase(requireContext()) // Use `requireContext()` in Fragment

            db.postDao().getPostsByUser(currentUser.displayName ?: "").observe(viewLifecycleOwner) { postEntities ->
                if (!postEntities.isNullOrEmpty()) {
                    val posts = postEntities.map { postEntity ->
                        Post(
                            id = postEntity.id,
                            userId = userId, // Pass userId
                            title = postEntity.title,
                            username = postEntity.username,
                            hobby = postEntity.hobby,
                            description = postEntity.description,
                            imageUrl = postEntity.imageUrl,
                            lastUpdated = postEntity.lastUpdated
                        )
                    }
                    postAdapter?.updatePosts(posts) // Update the adapter
                    postsCountTextView.text = posts.size.toString() // Update the posts count
                } else {
                    postAdapter?.updatePosts(emptyList())
                    postsCountTextView.text = "0"
                    Toast.makeText(requireContext(), "No posts found", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deletePost(post: Post) {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(requireContext())
            db.postDao().deletePostById(post.id)
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Post deleted", Toast.LENGTH_SHORT).show()
                loadUserPosts()
            }
        }
    }

    private fun navigateToEditProfile(view: View?) {
        view?.let {
            Navigation.findNavController(it)
                .navigate(R.id.action_profileFragment_to_editProfileFragment)
        }
    }

    private fun navigateToEditPost(post: Post) {
        val bundle = Bundle().apply {
            putString("postId", post.id)            // Pass post ID
            putString("userId", post.userId)        // Pass user ID
            putString("description", post.description) // Pass description
            putString("hobby", post.hobby)          // Pass hobby
            putString("imageUrl", post.imageUrl)    // Pass image URL
            putString("username", post.username)    // Pass username
        }

        view?.let {
            Navigation.findNavController(it)
                .navigate(R.id.action_profileFragment_to_editPostFragment, bundle)
        }
    }

    private fun refreshData() {
        // Refresh user data and posts
        loadUserData()
        loadUserPosts()

        // Stop the spinner after ensuring data is updated
        lifecycleScope.launch {
            // Add slight delay to simulate or wait for data loading
            delay(1000)
            swipeRefreshLayout.isRefreshing = false
            reloadFragment()
        }
    }
    private fun reloadFragment() {
        parentFragmentManager.beginTransaction().apply {
            detach(this@ProfileFragment) // Detach the current fragment
            attach(this@ProfileFragment) // Attach the same fragment instance to reload
            commit() // Commit the transaction
        }
    }



}
