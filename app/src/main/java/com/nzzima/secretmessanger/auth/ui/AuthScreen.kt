package com.nzzima.secretmessanger.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nzzima.secretmessanger.ui.theme.Accent
import com.nzzima.secretmessanger.ui.theme.Ink
import com.nzzima.secretmessanger.ui.theme.InkDim
import com.nzzima.secretmessanger.ui.theme.Raised
import com.nzzima.secretmessanger.utils.constants.Constants
import org.koin.androidx.compose.koinViewModel

/** Экран авторизации и регистрации. Оформление повторяет `AuthorizationView` и `RegistrationView` на iOS. */
@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val uiState by viewModel.observeAuthScreenState().collectAsStateWithLifecycle()

    AuthScreen(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onLoginChange = viewModel::onLoginChange,
        onPasswordChange = viewModel::onPasswordChange,
        onPasswordRepeatChange = viewModel::onPasswordRepeatChange,
        onModeToggle = viewModel::onModeToggle,
        onSubmit = viewModel::onSubmit,
        modifier = modifier,
    )
}

/**
 * Разметка экрана. Не зависит от [AuthViewModel] и пригодна для превью.
 *
 * Блок полей и кнопки центрируется в пространстве под заголовком равными отступами: на обеих
 * формах это ставит поле почты туда же, куда его ставит Auto Layout на iOS.
 */
@Composable
private fun AuthScreen(
    uiState: AuthUiState,
    onEmailChange: (String) -> Unit,
    onLoginChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordRepeatChange: (String) -> Unit,
    onModeToggle: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        // Высота окна без учёта клавиатуры. Раскладка от клавиатуры не зависит: она только
        // уменьшает видимую часть и включает прокрутку.
        val windowHeight = maxHeight

        // Поле почты стоит там же, где его ставит Auto Layout на iOS: центр экрана со
        // смещением -30 на входе и -100 на регистрации.
        val emailCenter = windowHeight / 2 + when (uiState.mode) {
            AuthUiState.Mode.SignIn -> SIGN_IN_EMAIL_OFFSET
            AuthUiState.Mode.Register -> REGISTER_EMAIL_OFFSET
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(TITLE_TOP))

            Text(
                text = uiState.title,
                color = Ink,
                style = TextStyle(
                    fontFamily = FontFamily.Serif,
                    fontSize = 26.sp,
                    letterSpacing = 1.5.sp,
                ),
            )

            Spacer(Modifier.height(emailCenter - FIELD_HEIGHT / 2 - TITLE_TOP - TITLE_HEIGHT))

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = SIDE_PADDING),
                verticalArrangement = Arrangement.spacedBy(FIELD_GAP),
            ) {
                Field(
                    value = uiState.email,
                    onValueChange = onEmailChange,
                    placeholder = Constants.EMAIL_PLACEHOLDER,
                    keyboardType = KeyboardType.Email,
                )

                if (uiState.mode == AuthUiState.Mode.Register) {
                    Field(
                        value = uiState.login,
                        onValueChange = onLoginChange,
                        placeholder = Constants.LOGIN_PLACEHOLDER,
                        keyboardType = KeyboardType.Ascii,
                    )
                }

                Field(
                    value = uiState.password,
                    onValueChange = onPasswordChange,
                    placeholder = Constants.PASSWORD_PLACEHOLDER,
                    isPassword = true,
                )

                if (uiState.mode == AuthUiState.Mode.Register) {
                    Field(
                        value = uiState.passwordRepeat,
                        onValueChange = onPasswordRepeatChange,
                        placeholder = Constants.PASSWORD_REPEAT_PLACEHOLDER,
                        isPassword = true,
                    )
                }
            }

            Spacer(
                Modifier.height(
                    when (uiState.mode) {
                        AuthUiState.Mode.SignIn -> SIGN_IN_BUTTON_TOP
                        AuthUiState.Mode.Register -> REGISTER_BUTTON_TOP
                    },
                ),
            )

            Button(
                onClick = onSubmit,
                modifier = Modifier.height(BUTTON_HEIGHT).width(uiState.buttonWidth()),
                shape = RoundedCornerShape(BUTTON_CORNER),
                contentPadding = PaddingValues(horizontal = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black,
                ),
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.height(20.dp).width(20.dp), color = Color.Black)
                } else {
                    Text(uiState.submitTitle, fontSize = 16.sp, maxLines = 1)
                }
            }

            Spacer(Modifier.height(LINK_TOP))

            TextButton(onClick = onModeToggle) {
                Text(uiState.switchTitle, color = Accent, fontSize = 15.sp, maxLines = 1)
            }

            uiState.error?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = SIDE_PADDING),
                )
            }

            Spacer(Modifier.height(BOTTOM_PADDING))
        }
    }
}

/** Поле ввода: высота 50, скругление 15, фон [Raised] — как `TextField` на iOS. */
@Composable
private fun Field(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(color = Ink, fontSize = 16.sp),
        cursorBrush = SolidColor(Accent),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            capitalization = KeyboardCapitalization.None,
            autoCorrectEnabled = false,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(FIELD_HEIGHT)
            .background(Raised, RoundedCornerShape(FIELD_CORNER)),
        decorationBox = { inner ->
            Box(
                modifier = Modifier.fillMaxSize().padding(horizontal = FIELD_INNER_PADDING),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isEmpty()) {
                    Text(placeholder, color = InkDim, fontSize = 16.sp)
                }
                inner()
            }
        },
    )
}

private fun AuthUiState.buttonWidth() = when (mode) {
    AuthUiState.Mode.SignIn -> 150.dp
    AuthUiState.Mode.Register -> 220.dp
}

private val TITLE_TOP = 100.dp
private val TITLE_HEIGHT = 34.dp
private val SIGN_IN_EMAIL_OFFSET = (-30).dp
private val REGISTER_EMAIL_OFFSET = (-100).dp
private val SIDE_PADDING = 30.dp
private val FIELD_HEIGHT = 50.dp
private val FIELD_GAP = 20.dp
private val FIELD_CORNER = 15.dp
private val FIELD_INNER_PADDING = 10.dp
private val SIGN_IN_BUTTON_TOP = 50.dp
private val REGISTER_BUTTON_TOP = 40.dp
private val BUTTON_HEIGHT = 40.dp
private val BUTTON_CORNER = 14.dp
private val LINK_TOP = 8.dp
private val BOTTOM_PADDING = 24.dp
