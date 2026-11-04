package mx.petmate.app.domain.usecase

import kotlinx.coroutines.flow.Flow
import mx.petmate.app.domain.model.SessionState
import mx.petmate.app.domain.repository.AuthRepository
import javax.inject.Inject

/** Observa el estado de la sesion para decidir que grafo mostrar. */
class ObserveSessionUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    operator fun invoke(): Flow<SessionState> = repository.sessionState
}
