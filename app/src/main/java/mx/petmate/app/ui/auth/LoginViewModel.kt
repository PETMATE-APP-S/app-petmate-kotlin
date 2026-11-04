package mx.petmate.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.petmate.app.core.result.AppResult
import mx.petmate.app.core.ui.UiText
import mx.petmate.app.domain.model.LoginCredentials
import mx.petmate.app.domain.usecase.LoginUseCase
import mx.petmate.app.domain.usecase.ValidateCredentialsUseCase
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: UiText? = null,
    val passwordError: UiText? = null,
    val generalError: UiText? = null,
    val isSubmitting: Boolean = false,
    val isSignedIn: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val login: LoginUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, generalError = null) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, generalError = null) }
    }

    fun onSubmit() {
        if (_uiState.value.isSubmitting) return
        _uiState.update {
            it.copy(
                isSubmitting = true,
                emailError = null,
                passwordError = null,
                generalError = null,
            )
        }
        viewModelScope.launch {
            val state = _uiState.value
            val result = login(LoginCredentials(state.email, state.password))
            when (result) {
                is AppResult.Success ->
                    _uiState.update { it.copy(isSubmitting = false, isSignedIn = true) }

                is AppResult.Failure -> _uiState.update { it.applyError(result) }
            }
        }
    }

    private fun LoginUiState.applyError(result: AppResult.Failure): LoginUiState {
        val error = result.error
        val message = error.toUiText()
        val field = (error as? mx.petmate.app.core.result.AppError.Validation)?.field
        return copy(
            isSubmitting = false,
            emailError = message.takeIf { field == ValidateCredentialsUseCase.FIELD_EMAIL },
            passwordError = message.takeIf { field == ValidateCredentialsUseCase.FIELD_PASSWORD },
            generalError = message.takeIf { field == null },
        )
    }
}
