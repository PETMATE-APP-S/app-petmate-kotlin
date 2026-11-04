package mx.petmate.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mx.petmate.app.BuildConfig
import mx.petmate.app.core.coroutines.DispatcherProvider
import mx.petmate.app.core.log.Logger
import mx.petmate.app.data.remote.AuthApiService
import mx.petmate.app.data.repository.AuthRepositoryImpl
import mx.petmate.app.data.repository.FakeAuthRepository
import mx.petmate.app.data.session.SessionStore
import mx.petmate.app.domain.repository.AuthRepository
import javax.inject.Singleton

/**
 * Decide que implementacion de AuthRepository usa la aplicacion.
 *
 * PARA CONECTAR EL BACKEND REAL: pon USE_FAKE_BACKEND en false dentro de
 * app/build.gradle.kts. No hay que tocar nada mas: ni el dominio, ni los
 * ViewModels, ni las pantallas.
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        api: AuthApiService,
        sessionStore: SessionStore,
        dispatchers: DispatcherProvider,
        logger: Logger,
    ): AuthRepository = if (BuildConfig.USE_FAKE_BACKEND) {
        FakeAuthRepository(sessionStore, dispatchers)
    } else {
        AuthRepositoryImpl(api, sessionStore, dispatchers, logger)
    }
}
