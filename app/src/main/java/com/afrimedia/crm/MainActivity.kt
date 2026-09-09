package com.afrimedia.crm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.afrimedia.crm.ui.nav.AppRoot
import com.afrimedia.crm.ui.theme.AfriMediaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as AfriMediaApp
        setContent {
            AfriMediaTheme {
                AppRoot(session = app.session, repository = app.repository)
            }
        }
    }
}
