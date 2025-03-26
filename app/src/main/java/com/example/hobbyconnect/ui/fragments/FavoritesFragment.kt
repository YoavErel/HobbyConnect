package com.example.hobbyconnect.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.hobbyconnect.R
import com.example.hobbyconnect.data.AppDatabase
import com.example.hobbyconnect.models.Post
import com.example.hobbyconnect.ui.adapters.FavoritesAdapter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoritesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var favoritesAdapter: FavoritesAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorites, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        favoritesAdapter = FavoritesAdapter(emptyList())
        recyclerView.adapter = favoritesAdapter
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)


        //divider between items
        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            (recyclerView.layoutManager as LinearLayoutManager).orientation
        )
        recyclerView.addItemDecoration(dividerItemDecoration)

        loadFavorites()

        swipeRefreshLayout.setOnRefreshListener {
            refreshData() // Call a method to reload data
        }

        return view
    }

    private fun loadFavorites() {
        val currentUserId = getCurrentUserId() // Retrieve the logged-in user's ID
        if (currentUserId.isEmpty()) return // Ensure a valid user ID

        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(requireContext())  // Get database instance
            val userLikesDao = db.userLikesDao()  // Correct DAO access
            val postDao = db.postDao()

            // Get liked post IDs for the user
            val likedPostIds: List<String> = userLikesDao.getLikedPostsByUser(currentUserId)

            // Fetch only the posts that are liked by this user
            val favoritePosts = likedPostIds.mapNotNull { postDao.getPostByIdOnce(it) }

            withContext(Dispatchers.Main) {
                favoritesAdapter.updateFavorites(favoritePosts)
            }
        }
    }


    private fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }




    private fun refreshData() {

        // Stop the spinner after ensuring data is updated
        lifecycleScope.launch {
            // Add slight delay to simulate or wait for data loading
            delay(1000) // Simulate a loading effect
            swipeRefreshLayout.isRefreshing = false
            reloadFragment()
        }
    }



    private fun reloadFragment() {
        parentFragmentManager.beginTransaction().apply {
            detach(this@FavoritesFragment) // Detach the current fragment
            attach(this@FavoritesFragment) // Attach the same fragment instance to reload
            commit() // Commit the transaction
        }
    }

}
