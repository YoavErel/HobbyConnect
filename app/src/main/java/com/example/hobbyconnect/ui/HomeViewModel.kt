package com.example.hobbyconnect.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.hobbyconnect.data.AppDatabase
import com.example.hobbyconnect.data.PostEntity
import com.example.hobbyconnect.data.FirebaseModel
import com.example.hobbyconnect.data.PostRepository
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository
    val posts: LiveData<List<PostEntity>>

    init {
        val postDao = AppDatabase.getDatabase(application).postDao()
        repository = PostRepository(postDao, FirebaseModel())
        posts = repository.getAllPosts() // Use getAllPosts from the repository
    }

    fun refreshPosts() {
        viewModelScope.launch {
            repository.syncPostsFromFirestore()
        }
    }
}
