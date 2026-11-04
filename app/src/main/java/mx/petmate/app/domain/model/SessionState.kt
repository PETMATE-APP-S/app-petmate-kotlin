package mx.petmate.app.domain.model

/**
 * Estado de la sesion.
 *
 * Checking existe para que la pantalla inicial no parpadee entre login y
 * contenido mientras se restaura la sesion guardada (spec 002, C-07).
 */
sealed interface SessionState {
    data object Checking : SessionState
    data object SignedOut : SessionState
    data class SignedIn(val user: User) : SessionState
}
