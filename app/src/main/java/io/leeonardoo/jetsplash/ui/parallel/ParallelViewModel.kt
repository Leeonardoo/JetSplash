package io.leeonardoo.jetsplash.ui.parallel

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.leeonardoo.jetsplash.api.BasicError
import io.leeonardoo.jetsplash.api.NetworkError
import io.leeonardoo.jetsplash.api.NetworkResult
import io.leeonardoo.jetsplash.model.UnsplashPhoto
import io.leeonardoo.jetsplash.repository.UnsplashRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ParallelViewModel(
    private val repository: UnsplashRepository
) : ViewModel() {

    private val _error = MutableStateFlow<NetworkError<BasicError>?>(null)
    val error = _error.asStateFlow()

    val photos = mutableStateListOf<UnsplashPhoto>()

    init {
        getRandomPhotos()
    }

    fun getRandomPhotos() {
        viewModelScope.launch {
            _error.value = null
            photos.clear()

            val requests = (0..20).map {
                async(Dispatchers.IO) {
                    val response = repository.getRandomPhoto()

                    if (response is NetworkResult.Success) {
                        withContext(Dispatchers.Main) {
                            photos.add(response.result)
                        }
                    }

                    response
                }
            }

            val response = requests.awaitAll()

            if (response.any { it is NetworkResult.Error }) {
                val error = response.first { it is NetworkResult.Error } as NetworkResult.Error
                _error.value = error.error
            }
        }
    }

    fun getRandomPhoto() {
        viewModelScope.launch {
            _error.value = null
            photos.clear()

            when (val response = repository.getRandomPhoto()) {
                is NetworkResult.Error ->
                    _error.value = response.error

                is NetworkResult.Success -> TODO()
            }
        }
    }
}