package io.leeonardoo.jetsplash.repository

import io.leeonardoo.jetsplash.api.BaseRequestHandler
import io.leeonardoo.jetsplash.api.BasicError
import io.leeonardoo.jetsplash.api.endpoint.UnsplashEndpoint

class UnsplashRepository(
    private val requestHandler: BaseRequestHandler,
    private val endpoint: UnsplashEndpoint
) : IUnsplashRepository {

    override suspend fun getRandomPhoto() =
        requestHandler.handle(BasicError::class.java) {
            endpoint.getRandomPhoto()
        }
}