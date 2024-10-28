package com.tinybitsinteractive.lbsolver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.tinybitsinteractive.lbsolver.ui.theme.LetterboxedSolverTheme

internal fun cleanSides(sides: String): String {
    var cleaned = ""
    var liveCount = 0
    for (c in sides) {
        val lc = c.lowercase()[0]
        if (lc.isLetter() && lc !in cleaned) {
            cleaned += lc
            ++liveCount
            if (liveCount == 12) {
                break
            }
            if ((liveCount % 3) == 0) {
                cleaned += ' '
            }
        }
    }
    return cleaned.trim()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LetterboxedSolverTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize())
                {
                    innerPadding ->
                    SolverUI(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}
