package io.leeonardoo.jetsplash.api.endpoint

import io.leeonardoo.jetsplash.model.UnsplashPhoto
import retrofit2.http.GET

interface UnsplashEndpoint {

    @GET("photos/random")
    suspend fun getRandomPhoto(): UnsplashPhoto
}