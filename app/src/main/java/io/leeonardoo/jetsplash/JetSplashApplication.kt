package io.leeonardoo.jetsplash

import android.app.Application
import coil.Coil
import coil.ImageLoader
import io.leeonardoo.jetsplash.di.endpointModule
import io.leeonardoo.jetsplash.di.networkModule
import io.leeonardoo.jetsplash.di.repositoryModule
import io.leeonardoo.jetsplash.di.viewModelModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

class JetSplashApplication : Application(), KoinComponent {

    private val coilImageLoader: ImageLoader by inject()

    private val modules = listOf(
        endpointModule,
        networkModule,
        repositoryModule,
        viewModelModule
    )

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        startKoin {
            setupLogger()

            androidContext(this@JetSplashApplication)

            workManagerFactory()

            modules(modules)
        }

        Coil.setImageLoader(coilImageLoader)
    }

    private fun KoinApplication.setupLogger() {
        androidLogger(Level.DEBUG)
    }
}