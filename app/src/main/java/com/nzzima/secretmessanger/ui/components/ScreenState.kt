package com.nzzima.secretmessanger.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nzzima.secretmessanger.ui.theme.InkDim
import com.nzzima.secretmessanger.utils.constants.Constants

/**
 * Строка посреди пустого экрана: «ничего нет» либо причина отказа.
 *
 * Рассчитана на середину — выравнивание задаёт вмещающий контейнер.
 */
@Composable
fun Notice(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = InkDim,
        fontSize = 15.sp,
        textAlign = TextAlign.Center,
        modifier = modifier.padding(horizontal = 30.dp),
    )
}

/** Причина отказа и кнопка подписаться заново. */
@Composable
fun FailureNotice(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Notice(message)
        TextButton(onClick = onRetry) { Text(Constants.RETRY) }
    }
}
