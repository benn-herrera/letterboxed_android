package com.tinybitsinteractive.lbsolver

import android.util.Log
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

private fun log(msg: String) {
    Log.i("PuzzleSolver", msg)
}

internal class PuzzleSolver(
    box: String,
    private val cacheDir: Path,
    private val onComplete: (solutions: String) -> Unit
) : Runnable {
    private val sides = boxToSides(box)
    private val combinedSides = Word(box.replace(" ", ""))
    private var dict: PuzzleDict? = null

    companion object {
        private val wordsUrl =
            URL("https://raw.githubusercontent.com/benn-herrera/letterboxed/refs/heads/main/src/letterboxed/words_alpha.txt")

        private fun boxToSides(box: String): Array<Word> {
            assert(box.length == 15) { "invalid puzzle. too few letters." }
            val sides = arrayOf(
                Word(box.substring(0, 3)),
                Word(box.substring(4, 7)),
                Word(box.substring(8, 11)),
                Word(box.substring(12, 15))
            )
            for (s in sides) {
                assert(s.chars.size == 3) {"bad puzzle. side has non-unique or letters."}
            }
            return sides
        }

        private fun downloadDict(): PuzzleDict? {
            try {
                with(wordsUrl.openConnection() as HttpURLConnection) {
                    inputStream.bufferedReader().use {
                        return@downloadDict PuzzleDict(it)
                    }
                }
            } catch (_: Throwable) {
            }
            return null
        }
    }

    private fun setup() {
        if (dict?.isNotEmpty() != true) {
            val dictCachePath: Path = cacheDir / "puzzle_dict.txt"
            if (!dictCachePath.exists()) {
                val preCacheTime = measureTime {
                    downloadDict()?.save(dictCachePath)
                }
                log("setup precache: ${preCacheTime}")
            }
            if (dictCachePath.exists()) {
                val filterLoadTime = measureTime {
                    dict = PuzzleDict(dictCachePath) { word -> worksForPuzzle(word) }
                }
                log("setup filtered load time: ${filterLoadTime}")
            }
        }
    }

    // we want the throw behavior - if the side isn't found there's been an error in filtering
    private fun sideIdx(c: Char): Int = sides.indices.first { sides[it].chars.contains(c) }

    private fun worksForPuzzle(word: Word): Boolean {
        // has letters not in puzzle
        if (word.chars.union(combinedSides.chars).size > combinedSides.chars.size) {
            return false
        }
        // has successive letters on the same side
        var side = sideIdx(word.text.first())
        for(i in (1 ..<word.text.length)) {
            val prev = side
            side = sideIdx(word.text[i])
            if (side == prev) {
                return false
            }
        }
        return true
    }

    private fun solve(): String {
        val solutions = mutableListOf<String>()
        dict?.let { dict ->
            for (start0 in combinedSides.chars) {
                for (word0 in dict.bucket(start0)) {
                    // one word solution
                    if (word0.chars.size == combinedSides.chars.size) {
                        solutions.add(word0.text)
                        continue
                    }
                    val start1 = word0.text.last()
                    for (word1 in dict.bucket(start1)) {
                        // two word solution
                        if (word0.chars.union(word1.chars).size == combinedSides.chars.size) {
                            solutions.add("${word0.text} -> ${word1.text}")
                        }
                    }
                }
            }
        }

        log("${solutions.size} solutions found")

        solutions.sortWith { a, b -> a.length - b.length }

        return buildString {
            solutions.forEach{ append( "$it\n" ) }
        }
    }

    override fun run() {
        log("run()")

        setup()

        if (dict?.isNotEmpty() != true) {
            onComplete("DOWNLOAD ERROR")
            return
        }

        val (solutions, elapsed) = measureTimedValue {
            solve()
        }

        log("solved in $elapsed")

        onComplete(solutions.ifEmpty { "No solutions found." })
    }
}
