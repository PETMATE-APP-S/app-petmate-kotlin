package mx.petmate.app.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import mx.petmate.app.core.coroutines.DefaultDispatcherProvider
import mx.petmate.app.core.coroutines.DispatcherProvider
import mx.petmate.app.core.log.AndroidLogger
import mx.petmate.app.core.log.Logger
import mx.petmate.app.data.session.SessionStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun provideLogger(): Logger = AndroidLogger()

    @Provides
    @Singleton
    fun provideDispatchers(): DispatcherProvider = DefaultDispatcherProvider()

    @Provides
    @Singleton
    fun provideSessionStore(
        @ApplicationContext context: Context,
        logger: Logger,
    ): SessionStore = SessionStore(context, logger)
}
