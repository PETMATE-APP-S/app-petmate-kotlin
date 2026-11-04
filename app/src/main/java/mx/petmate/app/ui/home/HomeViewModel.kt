package mx.petmate.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.petmate.app.domain.usecase.LogoutUseCase
import javax.inject.Inject

data class HomeUiState(
    val userName: String = "",
    val isLoggingOut: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val logout: LogoutUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun setUserName(name: String) = _uiState.update { it.copy(userName = name) }

    fun onLogoutClicked() {
        if (_uiState.value.isLoggingOut) return
        _uiState.update { it.copy(isLoggingOut = true) }
        viewModelScope.launch {
            logout()
            // La navegacion la decide el grafo al observar el estado de sesion:
            // esta pantalla no navega por su cuenta.
            _uiState.update { it.copy(isLoggingOut = false) }
        }
    }
}
