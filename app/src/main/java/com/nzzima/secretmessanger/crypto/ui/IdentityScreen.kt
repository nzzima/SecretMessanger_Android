package com.nzzima.secretmessanger.crypto.ui

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nzzima.secretmessanger.ui.theme.Ink
import com.nzzima.secretmessanger.ui.theme.InkDim
import com.nzzima.secretmessanger.utils.constants.Constants

/**
 * Развилка ключа: отдельное назначение навигации, мимо которого в список диалогов не попасть.
 *
 * [warning] — причина остановки, [actionTitle] — подпись действия, продолжающего вход.
 * Кнопки «всё равно продолжить без выбора» здесь нет намеренно: любой выход с экрана —
 * либо осознанная публикация поверх, либо выход из аккаунта.
 */
@Composable
fun IdentityScreen(
    warning: String,
    actionTitle: String,
    onAction: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = SIDE_PADDING),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = Constants.IDENTITY_TITLE,
            color = Ink,
            style = TextStyle(fontFamily = FontFamily.Serif, fontSize = 24.sp, letterSpacing = 1.5.sp),
        )

        Text(
            text = warning,
            color = InkDim,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 20.dp),
        )

        Button(
            onClick = onAction,
            modifier = Modifier.padding(top = 32.dp).height(BUTTON_HEIGHT).width(BUTTON_WIDTH),
            shape = RoundedCornerShape(BUTTON_CORNER),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
        ) {
            Text(actionTitle, fontSize = 15.sp, maxLines = 1)
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
