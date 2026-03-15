package com.example.effinote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.effinote.navigation.EffiNoteNav
import com.example.effinote.ui.theme.EffiNoteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EffiNoteTheme {
                EffiNoteNav(modifier = Modifier.fillMaxSize())
            }
        }
    }
}