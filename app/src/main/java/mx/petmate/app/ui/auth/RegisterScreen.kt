package mx.petmate.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.petmate.app.R
import mx.petmate.app.ui.theme.PetMateEdelTheme

@Composable
fun RegisterScreen(
    onSignedIn: () -> Unit,
    onGoToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSignedIn) {
        if (uiState.isSignedIn) onSignedIn()
    }

    RegisterContent(
        uiState = uiState,
        onNameChange = viewModel::onNameChanged,
        onEmailChange = viewModel::onEmailChanged,
        onPasswordChange = viewModel::onPasswordChanged,
        onPasswordConfirmationChange = viewModel::onPasswordConfirmationChanged,
        onPrivacyChange = viewModel::onPrivacyChanged,
        onSubmit = viewModel::onSubmit,
        onGoToLogin = onGoToLogin,
        modifier = modifier,
    )
}

@Composable
private fun RegisterContent(
    uiState: RegisterUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordConfirmationChange: (String) -> Unit,
    onPrivacyChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onGoToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val enabled = !uiState.isSubmitting
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.register_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(Modifier.height(24.dp))

        LabeledTextField(
            value = uiState.name,
            onValueChange = onNameChange,
            label = stringResource(R.string.field_name),
            error = uiState.nameError,
            enabled = enabled,
        )
        Spacer(Modifier.height(12.dp))

        LabeledTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            label = stringResource(R.string.field_email),
            error = uiState.emailError,
            keyboardType = KeyboardType.Email,
            enabled = enabled,
        )
        Spacer(Modifier.height(12.dp))

        PasswordField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = stringResource(R.string.field_password),
            error = uiState.passwordError,
            enabled = enabled,
        )
        Spacer(Modifier.height(12.dp))

        PasswordField(
            value = uiState.passwordConfirmation,
            onValueChange = onPasswordConfirmationChange,
            label = stringResource(R.string.field_password_confirmation),
            error = uiState.passwordConfirmationError,
            imeAction = ImeAction.Done,
            enabled = enabled,
        )
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = uiState.privacyAccepted,
                onCheckedChange = onPrivacyChange,
                enabled = enabled,
            )
            Text(
                text = stringResource(R.string.register_privacy),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        uiState.privacyError?.let {
            Text(
                text = it.asString(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Spacer(Modifier.height(16.dp))

        FormError(uiState.generalError)
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onSubmit,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(stringResource(R.string.register_submit))
            }
        }
        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = onGoToLogin,
            enabled = enabled,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text(stringResource(R.string.register_go_login))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterContentPreview() {
    PetMateEdelTheme {
        RegisterContent(
            uiState = RegisterUiState(name = "Adrián"),
            onNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onPasswordConfirmationChange = {},
            onPrivacyChange = {},
            onSubmit = {},
            onGoToLogin = {},
        )
    }
}
