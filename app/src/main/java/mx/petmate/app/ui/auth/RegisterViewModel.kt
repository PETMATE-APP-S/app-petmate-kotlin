package mx.petmate.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.petmate.app.R
import mx.petmate.app.core.result.AppError
import mx.petmate.app.core.result.AppResult
import mx.petmate.app.core.ui.UiText
import mx.petmate.app.domain.model.RegisterData
import mx.petmate.app.domain.usecase.RegisterUseCase
import mx.petmate.app.domain.usecase.ValidateCredentialsUseCase
import javax.inject.Inject

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val passwordConfirmation: String = "",
    val privacyAccepted: Boolean = false,
    val nameError: UiText? = null,
    val emailError: UiText? = null,
    val passwordError: UiText? = null,
    val passwordConfirmationError: UiText? = null,
    val privacyError: UiText? = null,
    val generalError: UiText? = null,
    val isSubmitting: Boolean = false,
    val isSignedIn: Boolean = false,
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val register: RegisterUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChanged(value: String) =
        _uiState.update { it.copy(name = value, nameError = null, generalError = null) }

    fun onEmailChanged(value: String) =
        _uiState.update { it.copy(email = value, emailError = null, generalError = null) }

    fun onPasswordChanged(value: String) =
        _uiState.update { it.copy(password = value, passwordError = null, generalError = null) }

    fun onPasswordConfirmationChanged(value: String) = _uiState.update {
        it.copy(
            passwordConfirmation = value,
            passwordConfirmationError = null,
            generalError = null,
        )
    }

    fun onPrivacyChanged(accepted: Boolean) =
        _uiState.update { it.copy(privacyAccepted = accepted, privacyError = null) }

    fun onSubmit() {
        if (_uiState.value.isSubmitting) return

        // El aviso de privacidad se valida aqui porque es un requisito de la
        // pantalla, no del dominio (spec 002, C-03).
        if (!_uiState.value.privacyAccepted) {
            _uiState.update {
                it.copy(privacyError = UiText.Resource(R.string.error_privacy_required))
            }
            return
        }

        _uiState.update { it.clearErrors().copy(isSubmitting = true) }

        viewModelScope.launch {
            val state = _uiState.value
            val result = register(
                RegisterData(
                    name = state.name,
                    email = state.email,
                    password = state.password,
                    passwordConfirmation = state.passwordConfirmation,
                    privacyAccepted = state.privacyAccepted,
                ),
            )
            when (result) {
                is AppResult.Success ->
                    _uiState.update { it.copy(isSubmitting = false, isSignedIn = true) }

                is AppResult.Failure -> _uiState.update { it.applyError(result.error) }
            }
        }
    }

    private fun RegisterUiState.clearErrors() = copy(
        nameError = null,
        emailError = null,
        passwordError = null,
        passwordConfirmationError = null,
        privacyError = null,
        generalError = null,
    )

    private fun RegisterUiState.applyError(error: AppError): RegisterUiState {
        val message = error.toUiText()
        val field = (error as? AppError.Validation)?.field
        return copy(
            isSubmitting = false,
            nameError = message.takeIf { field == ValidateCredentialsUseCase.FIELD_NAME },
            emailError = message.takeIf { field == ValidateCredentialsUseCase.FIELD_EMAIL },
            passwordError = message.takeIf { field == ValidateCredentialsUseCase.FIELD_PASSWORD },
            passwordConfirmationError = message.takeIf {
                field == ValidateCredentialsUseCase.FIELD_PASSWORD_CONFIRMATION
            },
            generalError = message.takeIf { field == null },
        )
    }
}
