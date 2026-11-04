package mx.petmate.app.domain.usecase

import mx.petmate.app.core.result.ValidationReason
import mx.petmate.app.domain.model.LoginCredentials
import mx.petmate.app.domain.model.RegisterData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateCredentialsUseCaseTest {

    private val validate = ValidateCredentialsUseCase()

    @Test
    fun `login valido no produce errores`() {
        val errors = validate.validateLogin(
            LoginCredentials(email = "demo@petmate.mx", password = "petmate123"),
        )
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `login sin correo reporta campo obligatorio`() {
        val errors = validate.validateLogin(LoginCredentials(email = "", password = "x"))
        assertEquals(ValidationReason.REQUIRED, errors.first().reason)
        assertEquals(ValidateCredentialsUseCase.FIELD_EMAIL, errors.first().field)
    }

    @Test
    fun `login con correo mal formado reporta formato invalido`() {
        val errors = validate.validateLogin(
            LoginCredentials(email = "no-es-correo", password = "petmate123"),
        )
        assertEquals(ValidationReason.INVALID_EMAIL, errors.first().reason)
    }

    @Test
    fun `login sin contrasena reporta campo obligatorio`() {
        val errors = validate.validateLogin(
            LoginCredentials(email = "demo@petmate.mx", password = ""),
        )
        assertEquals(ValidateCredentialsUseCase.FIELD_PASSWORD, errors.first().field)
    }

    @Test
    fun `registro valido no produce errores`() {
        val errors = validate.validateRegister(validRegistration())
        assertTrue(errors.isEmpty())
    }

    @Test
    fun `registro sin nombre reporta campo obligatorio`() {
        val errors = validate.validateRegister(validRegistration().copy(name = "  "))
        assertEquals(ValidateCredentialsUseCase.FIELD_NAME, errors.first().field)
    }

    @Test
    fun `contrasena corta reporta longitud insuficiente`() {
        val errors = validate.validateRegister(
            validRegistration().copy(password = "corta", passwordConfirmation = "corta"),
        )
        assertEquals(ValidationReason.PASSWORD_TOO_SHORT, errors.first().reason)
    }

    @Test
    fun `contrasenas distintas reportan que no coinciden`() {
        val errors = validate.validateRegister(
            validRegistration().copy(passwordConfirmation = "otraCosa123"),
        )
        assertEquals(ValidationReason.PASSWORDS_DO_NOT_MATCH, errors.first().reason)
    }

    @Test
    fun `el correo con espacios alrededor se acepta`() {
        val errors = validate.validateLogin(
            LoginCredentials(email = "  demo@petmate.mx  ", password = "petmate123"),
        )
        assertTrue(errors.isEmpty())
    }

    private fun validRegistration() = RegisterData(
        name = "Adrian",
        email = "adrian@petmate.mx",
        password = "petmate123",
        passwordConfirmation = "petmate123",
        privacyAccepted = true,
    )
}
