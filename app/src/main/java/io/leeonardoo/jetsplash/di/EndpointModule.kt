package io.leeonardoo.jetsplash.di

import io.leeonardoo.jetsplash.api.endpoint.UnsplashEndpoint
import org.koin.dsl.module
import retrofit2.Retrofit

val endpointModule = module {

    single {
        val retrofit: Retrofit = get()
        retrofit.create(UnsplashEndpoint::class.java)
    }
}