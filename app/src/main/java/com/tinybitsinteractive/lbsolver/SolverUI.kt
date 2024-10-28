package com.tinybitsinteractive.lbsolver

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

@Composable
fun SolverUI(modifier: Modifier = Modifier) {
    var sidesTFV by remember {
        // start with example puzzle to make it easy to try out
        mutableStateOf(TextFieldValue("rpj cit owl aks", selection = TextRange(15)))
    }

    var solutions by remember { mutableStateOf( "") }
    var working by remember { mutableStateOf(false) }
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
        Row (
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )
        {
            TextField(
                modifier = Modifier.align(Alignment.CenterVertically),
                label = { Text(text = "Enter Puzzle Sides") },
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
                            working = false
                        }
                    }
                }
            )
            Button(
                modifier = Modifier.align(Alignment.CenterVertically),
                onClick = {
                    synchronized(solutions) {
                        solutions = ""
                        working = true
                    }
                    val solver = Thread(
                        PuzzleSolver(
                            cacheDir = context.cacheDir.toPath(),
                            box = sidesTFV.text
                        ) { results ->
                            synchronized(solutions) {
                                solutions = results
                                working = false
                            }
                        })
                    solver.start()
                },
                enabled = sidesTFV.text.length == 15 && solutions.isEmpty() && !working,
                content = {
                    Text("Solve")
                }
            )
        }
        synchronized(solutions) {
            if (working || solutions.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text("Solutions:")
                    val mod = Modifier
                        .border(width = 1.dp, color = Color(0.0f, 0.0f, 0.0f))
                        .padding(start = 5.dp)
                        .fillMaxWidth()
                    if (solutions.isNotEmpty()) {
                        Text(
                            solutions,
                            modifier = mod
                                .verticalScroll(rememberScrollState())
                        )
                    } else {
                        WorkingIndicator(modifier = mod)
                    }
                }
            }
        }
    }
}

@Composable
fun WorkingIndicator(modifier: Modifier) {
    val barLength = 12
    val trans = rememberInfiniteTransition(label = "stepTransition")
    val fstep by trans.animateFloat(
        initialValue = 0.0f,
        targetValue = barLength.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "stepValue"
    )
    val istep = (fstep + 0.5f).toInt()
    Text(
        text = "=".repeat(istep)
                + "(*)"
                + "=".repeat(barLength - istep),
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun SolverUIPreview() {
    LetterboxedSolverTheme {
        SolverUI()
    }
}