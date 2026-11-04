package mx.petmate.app.domain.model

/** Datos para iniciar sesion. */
data class LoginCredentials(
    val email: String,
    val password: String,
)

/** Datos para crear una cuenta. */
data class RegisterData(
    val name: String,
    val email: String,
    val password: String,
    val passwordConfirmation: String,
    val privacyAccepted: Boolean,
)
