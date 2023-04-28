package io.leeonardoo.jetsplash.repository

import io.leeonardoo.jetsplash.api.BasicError
import io.leeonardoo.jetsplash.api.NetworkResult
import io.leeonardoo.jetsplash.model.UnsplashPhoto

interface IUnsplashRepository {

    suspend fun getRandomPhoto(): NetworkResult<UnsplashPhoto, BasicError>
}