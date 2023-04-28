package io.leeonardoo.jetsplash.di

import io.leeonardoo.jetsplash.repository.IUnsplashRepository
import io.leeonardoo.jetsplash.repository.UnsplashRepository
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoryModule = module {

    factoryOf(::UnsplashRepository) bind IUnsplashRepository::class
}