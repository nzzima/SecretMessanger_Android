package com.nzzima.secretmessanger.main.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.nzzima.secretmessanger.ui.theme.SecretMessangerTheme

/** Единственная Activity приложения. Экраны — назначения графа [AppNavHost]. */
class RootActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SecretMessangerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { insets ->
                    AppNavHost(modifier = Modifier.padding(insets))
                }
            }
        }
    }
}
