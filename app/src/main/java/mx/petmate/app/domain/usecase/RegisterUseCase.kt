package mx.petmate.app.domain.usecase

import mx.petmate.app.core.result.AppResult
import mx.petmate.app.domain.model.RegisterData
import mx.petmate.app.domain.model.User
import mx.petmate.app.domain.repository.AuthRepository
import javax.inject.Inject

/** Crea una cuenta despues de validar los datos. */
class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository,
    private val validate: ValidateCredentialsUseCase,
) {
    suspend operator fun invoke(data: RegisterData): AppResult<User> {
        val errors = validate.validateRegister(data)
        if (errors.isNotEmpty()) return AppResult.Failure(errors.first())
        return repository.register(data.copy(email = data.email.trim()))
    }
}
