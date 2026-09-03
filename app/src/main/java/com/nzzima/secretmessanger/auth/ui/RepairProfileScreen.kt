package com.nzzima.secretmessanger.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nzzima.secretmessanger.ui.components.Field
import com.nzzima.secretmessanger.ui.theme.Ink
import com.nzzima.secretmessanger.ui.theme.InkDim
import com.nzzima.secretmessanger.utils.constants.Constants

/**
 * Достройка оборванной регистрации: один логин и кнопка.
 *
 * Отдельное назначение навигации, как и развилка ключа: без профиля дальше не пройти, и
 * обойти экран нечем — остаётся либо назвать логин, либо выйти из аккаунта.
 *
 * [error] — причина неудавшейся попытки, `null` при первом показе.
 */
@Composable
fun RepairProfileScreen(
    error: String?,
    onSubmit: (String) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var login by remember { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = SIDE_PADDING),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = Constants.REPAIR_TITLE,
            color = Ink,
            style = TextStyle(fontFamily = FontFamily.Serif, fontSize = 24.sp, letterSpacing = 1.5.sp),
            textAlign = TextAlign.Center,
        )

        Text(
            text = Constants.REPAIR_EXPLANATION,
            color = InkDim,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 20.dp),
        )

        Field(
            value = login,
            onValueChange = { login = it.trim() },
            placeholder = Constants.LOGIN_PLACEHOLDER,
            modifier = Modifier.padding(top = 28.dp),
        )

        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        Button(
            onClick = { onSubmit(login) },
            enabled = login.isNotEmpty(),
            modifier = Modifier.padding(top = 24.dp).height(BUTTON_HEIGHT).width(BUTTON_WIDTH),
            shape = RoundedCornerShape(BUTTON_CORNER),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
        ) {
            Text(Constants.REPAIR_SUBMIT, fontSize = 15.sp, maxLines = 1)
        }

        TextButton(onClick = onSignOut, modifier = Modifier.padding(top = 4.dp)) {
            Text(Constants.SIGN_OUT, color = MaterialTheme.colorScheme.error, fontSize = 15.sp)
        }
    }
}

private val SIDE_PADDING = 30.dp
private val BUTTON_HEIGHT = 40.dp
private val BUTTON_WIDTH = 260.dp
private val BUTTON_CORNER = 14.dp
