package com.example.hobbyconnect.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.hobbyconnect.R
import com.example.hobbyconnect.api.WeatherService
import com.example.hobbyconnect.data.AppDatabase
import com.example.hobbyconnect.data.UserLikesEntity
import com.example.hobbyconnect.models.Post
import com.example.hobbyconnect.ui.HomeViewModel
import com.example.hobbyconnect.ui.adapters.HomePostAdapter
import com.example.hobbyconnect.utils.toPost
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnCreate: Button
    private lateinit var emptyStateLayout: View
    private lateinit var homePostAdapter: HomePostAdapter
    private lateinit var viewModel: HomeViewModel
    private lateinit var weatherCity: TextView
    private lateinit var weatherTemperature: TextView
    private lateinit var weatherConditions: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var userHobbies: List<String> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        btnCreate = view.findViewById(R.id.btnCreate)
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh)

        homePostAdapter = HomePostAdapter(emptyList()) { post -> toggleLike(post) }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = homePostAdapter
        weatherCity = view.findViewById(R.id.weatherCity)
        weatherTemperature = view.findViewById(R.id.weatherTemperature)
        weatherConditions = view.findViewById(R.id.weatherConditions)

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        // First, load user's hobbies
        loadUserHobbies()

        // Then observe posts (which will be filtered based on hobbies)
        viewModel.posts.observe(viewLifecycleOwner) { postEntities ->
            Log.d("HomeFragment", "Observed posts: $postEntities")
            if (!postEntities.isNullOrEmpty()) {
                // Convert to Post objects and filter by user hobbies
                val allPosts = postEntities.map { it.toPost() }
                filterAndDisplayPosts(allPosts)
            } else {
                showEmptyState()
            }
        }

        btnCreate.setOnClickListener { navigateToPostCreation() }

        // Load weather
        loadWeather()

        swipeRefreshLayout.setOnRefreshListener {
            refreshData() // Call a method to reload data
        }

        return view
    }

    private fun loadUserHobbies() {
        val currentUserId = getCurrentUserId()
        if (currentUserId.isEmpty()) return

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(requireContext())
                val userEntity = db.userDao().getUserById(currentUserId)

                userEntity?.let {
                    // Parse hobbies string into a list
                    userHobbies = if (!it.hobbies.isNullOrEmpty()) {
                        it.hobbies.split(", ").map { hobby -> hobby.trim() }
                    } else {
                        emptyList()
                    }

                    Log.d("HomeFragment", "User hobbies loaded: $userHobbies")

                    // Reload posts with the new hobby filter
                    viewModel.refreshPosts()
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error loading user hobbies: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Failed to load user preferences", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun filterAndDisplayPosts(allPosts: List<Post>) {
        // If no hobbies are selected, show empty state
        if (userHobbies.isEmpty()) {
            Log.d("HomeFragment", "No user hobbies found, showing empty state")
            showEmptyState()
            return
        }

        // Filter posts by user's hobbies
        val filteredPosts = allPosts.filter { post ->
            userHobbies.contains(post.hobby)
        }

        Log.d("HomeFragment", "Filtered posts: ${filteredPosts.size} out of ${allPosts.size}")

        if (filteredPosts.isEmpty()) {
            showEmptyState()
        } else {
            fetchAvatarsAndDisplayPosts(filteredPosts)
        }
    }

    private fun toggleLike(post: Post) {
        val currentUserId = getCurrentUserId()
        if (currentUserId.isEmpty()) return // Ensure a valid user is logged in

        val updatedPost = post.copy(likedByCurrentUser = !post.likedByCurrentUser)

        // Update the UI immediately
        val updatedPosts = homePostAdapter.getPosts().map {
            if (it.id == post.id) updatedPost else it
        }
        homePostAdapter.updatePosts(updatedPosts)

        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(requireContext())
            val userLikesDao = db.userLikesDao()

            if (updatedPost.likedByCurrentUser) {
                // Add like for the current user
                userLikesDao.insertLike(UserLikesEntity(userId = currentUserId, postId = post.id))
            } else {
                // Remove like for the current user
                userLikesDao.removeLike(currentUserId, post.id)
            }

            withContext(Dispatchers.Main) {
                viewModel.refreshPosts() // Ensure UI updates correctly
            }
        }
    }

    private fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    private fun fetchAvatarsAndDisplayPosts(posts: List<Post>) {
        val currentUserId = getCurrentUserId()
        if (currentUserId.isEmpty()) return // Ensure a valid user is logged in

        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(requireContext())
            val userLikesDao = db.userLikesDao()
            val userDao = db.userDao()

            // Create a list of posts with updated like status and user avatars
            val updatedPosts = posts.map { post ->
                val isLiked = userLikesDao.isPostLiked(currentUserId, post.id) > 0
                val userEntity = userDao.getUserById(post.userId)
                post.copy(likedByCurrentUser = isLiked, avatarUrl = userEntity?.avatarUri)
            }

            withContext(Dispatchers.Main) {
                showPostList(updatedPosts)
            }
        }
    }

    private fun showEmptyState() {
        recyclerView.visibility = View.GONE
        emptyStateLayout.visibility = View.VISIBLE
        btnCreate.visibility = View.VISIBLE
    }

    private fun showPostList(posts: List<Post>) {
        recyclerView.visibility = View.VISIBLE
        emptyStateLayout.visibility = View.GONE
        homePostAdapter.updatePosts(posts)
    }

    private fun navigateToPostCreation() {
        val userId = getCurrentUserId()
        if (userId.isEmpty()) {
            Log.e("HomeFragment", "User ID is empty, preventing crash!")
            return
        }

        findNavController().navigate(R.id.action_homeFragment_to_addPostFragment)
    }

    @SuppressLint("SetTextI18n")
    private fun loadWeather() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val weatherService = WeatherService()
                val weatherResponse = weatherService.fetchWeather()


                weatherCity.text = "Haifa" // Hardcoded city for now
                weatherTemperature.text = "${weatherResponse.current.temperature}°C"
                weatherConditions.text = weatherResponse.current.summary
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error fetching weather: ${e.localizedMessage}", e)
                Toast.makeText(requireContext(), "Failed to load weather", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun refreshData() {
        // Reload user hobbies first
        loadUserHobbies()

        // Refresh posts (will be filtered based on updated hobbies)
        viewModel.refreshPosts()

        // Refresh weather data
        loadWeather()

        // Stop the spinner after a delay or once data is updated
        lifecycleScope.launch {
            delay(1000) // Add slight delay to simulate data load
            swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload user hobbies and refresh posts when returning to this fragment
        loadUserHobbies()
    }
}