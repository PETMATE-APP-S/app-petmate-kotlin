package mx.petmate.app.domain.usecase

import mx.petmate.app.core.result.AppResult
import mx.petmate.app.domain.repository.AuthRepository
import javax.inject.Inject

/** Cierra la sesion del usuario. */
class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(): AppResult<Unit> = repository.logout()
}
