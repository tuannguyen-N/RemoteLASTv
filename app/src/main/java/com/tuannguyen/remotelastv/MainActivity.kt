package com.tuannguyen.remotelastv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tuannguyen.remotelastv.ui.navigation.AppNavHost
import com.tuannguyen.remotelastv.ui.theme.RemoteLASTvTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RemoteLASTvTheme {
                AppNavHost()
            }
        }
    }
}
