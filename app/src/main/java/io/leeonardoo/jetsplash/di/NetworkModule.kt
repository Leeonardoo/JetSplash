package io.leeonardoo.jetsplash.di

import android.os.Build
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.decode.VideoFrameDecoder
import coil.util.DebugLogger
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.squareup.moshi.Moshi
import io.leeonardoo.jetsplash.api.BaseRequestHandler
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

val networkModule = module {

    singleOf(::BaseRequestHandler)

    single {
        OkHttpClient.Builder().apply {
            retryOnConnectionFailure(true)

            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            addInterceptor(ChuckerInterceptor.Builder(androidContext()).build())
            addInterceptor(loggingInterceptor)
        }.build()
    }

    single {
        val okHttpClient: OkHttpClient = get()
        val moshi = Moshi.Builder().build()

        Retrofit.Builder().apply {
            baseUrl("https://unsplash.com/napi/")
            addConverterFactory(MoshiConverterFactory.create(moshi))
            client(okHttpClient)
        }.build()
    }

    single {
        ImageLoader.Builder(androidContext()).components {
            if (Build.VERSION.SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
            add(SvgDecoder.Factory())
            add(VideoFrameDecoder.Factory())
        }.apply {
            respectCacheHeaders(false)
            logger(DebugLogger())
        }.build()
    }
}