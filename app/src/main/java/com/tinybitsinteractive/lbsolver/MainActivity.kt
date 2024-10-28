package com.tinybitsinteractive.lbsolver

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tinybitsinteractive.lbsolver.ui.theme.LetterboxedSolverTheme
import java.lang.Thread.sleep

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

internal fun logi(message: String) {
    Log.i("LBS", message)
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

@Composable
fun SolverUI(modifier: Modifier = Modifier) {
    var sidesTFV by remember {
        // mutableStateOf(TextFieldValue("", selection = TextRange(0)))
        mutableStateOf(TextFieldValue("slq xti fua eno", selection = TextRange(15)))
    }

    var solutions by remember { mutableStateOf( "") }
    var working by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    Column(
        modifier = modifier
    ) {
        Text(
            modifier = Modifier
                .height(24.dp)
                .align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            text = "Letterboxed Solver"
        )
        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            text = "Sides [e.g.: abc def ghi jkl]"
        )
        Row (
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )
        {
            TextField(
                modifier = Modifier.align(Alignment.CenterVertically),
                value = sidesTFV,
                onValueChange = { tfv ->
                    val cleaned = cleanSides(tfv.text)
                    sidesTFV = TextFieldValue(
                        cleaned,
                        TextRange(cleaned.length)
                    )
                    if (cleaned.length != 15) {
                        synchronized(solutions) {
                            solutions = ""
                            working = 0
                        }
                    }
                }
            )
            Button(
                modifier = Modifier.align(Alignment.CenterVertically),
                onClick = {
                    synchronized(solutions) {
                        solutions = ""
                        working = 1
                    }
                    val solver = Thread(
                        PuzzleSolver(
                            cacheDir = context.cacheDir.toPath(),
                            box = sidesTFV.text
                        ) { results ->
                            synchronized(solutions) {
                                solutions = results
                                working = 0
                            }
                        })
                    solver.start()
                },
                enabled = sidesTFV.text.length == 15 && solutions.isEmpty() && working == 0,
                content = {
                    Text("Solve")
                }
            )
        }
        synchronized(solutions) {
            if (working > 0 || solutions.isNotEmpty()) {
                Text("Solutions:")
                Text(
                    solutions.ifEmpty { "*".repeat(++working % 12) },
                    modifier = Modifier
                        .border(width = 1.dp, color = Color(0.0f, 0.0f, 0.0f))
                        .padding(start = 5.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                )
                if (solutions.isEmpty()) {
                    sleep(30)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SolverUIPreview() {
    LetterboxedSolverTheme {
        SolverUI()
    }
}