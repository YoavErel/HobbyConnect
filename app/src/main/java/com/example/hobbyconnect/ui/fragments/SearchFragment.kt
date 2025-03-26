package com.example.hobbyconnect.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.hobbyconnect.R
import com.example.hobbyconnect.data.AppDatabase
import com.example.hobbyconnect.ui.adapters.SearchAdapter
import com.example.hobbyconnect.utils.toPost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchFragment : Fragment() {

    private lateinit var searchBar: EditText
    private lateinit var searchResults: RecyclerView
    private lateinit var emptyStateTextView: TextView
    private lateinit var sortSpinner: Spinner
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private var searchAdapter: SearchAdapter? = null
    private var isAscending = true // Default sorting order

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        // Initialize views
        searchBar = view.findViewById(R.id.searchBar)
        searchResults = view.findViewById(R.id.searchResults)
        emptyStateTextView = view.findViewById(R.id.emptyStateTextView)
        sortSpinner = view.findViewById(R.id.sortSpinner)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh)

        // Setup RecyclerView
        searchResults.layoutManager = LinearLayoutManager(requireContext())
        searchAdapter = SearchAdapter(emptyList())
        searchResults.adapter = searchAdapter

        // Setup Search Bar with debouncing
        setupSearchBar()

        // Setup Sort Spinner
        setupSortSpinner()

        swipeRefreshLayout.setOnRefreshListener {
            refreshData() // Call a method to reload data
        }

        return view
    }

    private fun setupSearchBar() {
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                performSearch(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupSortSpinner() {
        // Create an ArrayAdapter for the spinner
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.sort_options,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner.adapter = adapter

        // Handle item selection
        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> isAscending = true // Sort by Newest
                    1 -> isAscending = false // Sort by Oldest
                }
                performSearch(searchBar.text.toString()) // Trigger search again with the updated sort order
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun performSearch(query: String) {
        if (query.isBlank()) {
            searchAdapter?.updateResults(emptyList())
            showEmptyState("Start typing to search...")
            return
        }

        val db = AppDatabase.getDatabase(requireContext())

        db.postDao().searchPosts(query).observe(this) { results ->
            if (results.isNullOrEmpty()) {
                showEmptyState("No results found for \"$query\"")
            } else {
                // Corrected sorting logic
                val sortedResults = if (!isAscending) { // Sort Newest to Oldest
                    results.map { it.toPost() }.sortedBy { it.lastUpdated }
                } else { // Sort Oldest to Newest
                    results.map { it.toPost() }.sortedByDescending { it.lastUpdated }
                }
                hideEmptyState()
                searchAdapter?.updateResults(sortedResults)
            }
        }
    }


    private fun showEmptyState(message: String) {
        emptyStateTextView.visibility = View.VISIBLE
        emptyStateTextView.text = message
        searchResults.visibility = View.GONE
    }

    private fun hideEmptyState() {
        emptyStateTextView.visibility = View.GONE
        searchResults.visibility = View.VISIBLE
    }

    private fun refreshData() {
        // Re-perform the search with the current query in the search bar
        val currentQuery = searchBar.text.toString()
        performSearch(currentQuery)

        // Stop the spinner after ensuring data is updated
        lifecycleScope.launch {
            // Add slight delay to simulate or wait for data loading
            delay(1000) // Optional delay for smoother UX
            swipeRefreshLayout.isRefreshing = false
            reloadFragment()
        }
    }
    private fun reloadFragment() {
        parentFragmentManager.beginTransaction().apply {
            detach(this@SearchFragment) // Detach the current fragment
            attach(this@SearchFragment) // Attach the same fragment instance to reload
            commit() // Commit the transaction
        }
    }

}
