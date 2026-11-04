package mx.petmate.app.domain.usecase

import mx.petmate.app.core.result.AppError
import mx.petmate.app.core.result.ValidationReason
import mx.petmate.app.domain.model.LoginCredentials
import mx.petmate.app.domain.model.RegisterData
import javax.inject.Inject

/**
 * Reglas de validacion de los formularios de identidad.
 *
 * Viven en el dominio y no en la pantalla: las mismas reglas se aplican al
 * escribir y al enviar, y duplicarlas garantiza que se desincronicen
 * (plan 002, D-05 de la unidad 011 aplica el mismo criterio).
 *
 * La politica definitiva de contrasena la define el backend (spec 002, Q-02).
 * Mientras tanto se exige un minimo conservador que no contradice ninguna
 * politica razonable.
 */
class ValidateCredentialsUseCase @Inject constructor() {

    fun validateLogin(credentials: LoginCredentials): List<AppError.Validation> = buildList {
        addAll(emailErrors(credentials.email))
        if (credentials.password.isBlank()) {
            add(AppError.Validation(FIELD_PASSWORD, ValidationReason.REQUIRED))
        }
    }

    fun validateRegister(data: RegisterData): List<AppError.Validation> = buildList {
        if (data.name.isBlank()) {
            add(AppError.Validation(FIELD_NAME, ValidationReason.REQUIRED))
        }
        addAll(emailErrors(data.email))
        when {
            data.password.isBlank() ->
                add(AppError.Validation(FIELD_PASSWORD, ValidationReason.REQUIRED))

            data.password.length < MIN_PASSWORD_LENGTH ->
                add(AppError.Validation(FIELD_PASSWORD, ValidationReason.PASSWORD_TOO_SHORT))
        }
        if (data.password.isNotBlank() && data.password != data.passwordConfirmation) {
            add(
                AppError.Validation(
                    FIELD_PASSWORD_CONFIRMATION,
                    ValidationReason.PASSWORDS_DO_NOT_MATCH,
                ),
            )
        }
    }

    private fun emailErrors(email: String): List<AppError.Validation> = when {
        email.isBlank() -> listOf(AppError.Validation(FIELD_EMAIL, ValidationReason.REQUIRED))
        !EMAIL_PATTERN.matches(email.trim()) ->
            listOf(AppError.Validation(FIELD_EMAIL, ValidationReason.INVALID_EMAIL))

        else -> emptyList()
    }

    companion object {
        const val FIELD_NAME = "name"
        const val FIELD_EMAIL = "email"
        const val FIELD_PASSWORD = "password"
        const val FIELD_PASSWORD_CONFIRMATION = "passwordConfirmation"

        const val MIN_PASSWORD_LENGTH = 8

        private val EMAIL_PATTERN = Regex("""^[^@\s]+@[^@\s.]+\.[^@\s]+$""")
    }
}
