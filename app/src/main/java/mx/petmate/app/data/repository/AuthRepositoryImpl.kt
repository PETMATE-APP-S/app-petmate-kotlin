package mx.petmate.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import mx.petmate.app.core.coroutines.DispatcherProvider
import mx.petmate.app.core.log.Logger
import mx.petmate.app.core.result.AppResult
import mx.petmate.app.data.remote.AuthApiService
import mx.petmate.app.data.remote.dto.LoginRequestDto
import mx.petmate.app.data.remote.dto.RegisterRequestDto
import mx.petmate.app.data.remote.toAppError
import mx.petmate.app.data.remote.toDomain
import mx.petmate.app.data.session.SessionStore
import mx.petmate.app.domain.model.LoginCredentials
import mx.petmate.app.domain.model.RegisterData
import mx.petmate.app.domain.model.SessionState
import mx.petmate.app.domain.model.User
import mx.petmate.app.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementacion contra el backend real.
 *
 * TODAVIA NO SE USA: el backend no esta desplegado y su contrato esta
 * pendiente (spec 002, Q-01). Queda escrita para que activarla sea cambiar
 * USE_FAKE_BACKEND en app/build.gradle.kts.
 *
 * Al conectarla hay que revisar que las rutas y los nombres de campo de
 * AuthApiService y de los DTO coincidan con el contrato real.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApiService,
    private val sessionStore: SessionStore,
    private val dispatchers: DispatcherProvider,
    private val logger: Logger,
) : AuthRepository {

    private val state = MutableStateFlow<SessionState>(SessionState.Checking)

    override val sessionState: Flow<SessionState> = state.asStateFlow()

    override suspend fun restoreSession() = withContext(dispatchers.io) {
        val saved = sessionStore.readUser()
        state.value = if (saved != null) SessionState.SignedIn(saved) else SessionState.SignedOut
    }

    override suspend fun register(data: RegisterData): AppResult<User> =
        withContext(dispatchers.io) {
            runCatching {
                api.register(
                    RegisterRequestDto(
                        name = data.name,
                        email = data.email,
                        password = data.password,
                    ),
                )
            }.fold(
                onSuccess = { response ->
                    val user = response.user.toDomain()
                    sessionStore.save(user, response.accessToken, response.refreshToken)
                    state.value = SessionState.SignedIn(user)
                    AppResult.Success(user)
                },
                onFailure = { AppResult.Failure(it.toAppError()) },
            )
        }

    override suspend fun login(credentials: LoginCredentials): AppResult<User> =
        withContext(dispatchers.io) {
            runCatching {
                api.login(
                    LoginRequestDto(
                        email = credentials.email,
                        password = credentials.password,
                    ),
                )
            }.fold(
                onSuccess = { response ->
                    val user = response.user.toDomain()
                    sessionStore.save(user, response.accessToken, response.refreshToken)
                    state.value = SessionState.SignedIn(user)
                    AppResult.Success(user)
                },
                onFailure = { AppResult.Failure(it.toAppError()) },
            )
        }

    override suspend fun logout(): AppResult<Unit> = withContext(dispatchers.io) {
        // Lo local se borra SIEMPRE, aunque el servidor falle: el usuario pidio
        // salir y debe salir (spec 002, C-11).
        val remote = runCatching { api.logout() }
        sessionStore.clear()
        state.value = SessionState.SignedOut
        remote.exceptionOrNull()?.let {
            logger.error(TAG, "No se pudo invalidar la sesion en el servidor", it)
        }
        AppResult.Success(Unit)
    }

    private companion object {
        const val TAG = "AuthRepository"
    }
}
