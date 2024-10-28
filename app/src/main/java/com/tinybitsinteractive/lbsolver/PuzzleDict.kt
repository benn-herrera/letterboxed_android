package com.tinybitsinteractive.lbsolver

import java.io.BufferedReader
import java.nio.file.Path
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

internal class Word(wordText: String) {
    val text: String = wordText.lowercase()
    val chars: Set<Char> = text.toSet()
}

internal class PuzzleDict {
    private var buckets = createBuckets()

    companion object {
        private fun createBuckets() : Array<List<Word>> {
            return Array(26) { listOf() }
        }

        private fun toUsableWord(wordText: String): Word? {
            // can't be shorter than 3 or have more than 12 unique letters
            if (wordText.length < 3) {
                return null
            }
            val word = Word(wordText)
            if (word.chars.size > 12) {
                return null
            }
            // can't have double letters
            for (i in 0..(wordText.length - 2)) {
                if (wordText[i] == wordText[i+1]) {
                    return null
                }
            }
            return word
        }

        private fun bucketIndex(word: Word) = word.text.first() - 'a'
    }

    constructor(cachePath: Path, filter: (word: Word) -> Boolean) {
        try {
            val mutableBuckets = Array<MutableList<Word>>(26) { mutableListOf() }
            cachePath.inputStream().bufferedReader().use { cache ->
                var rawCount = 0
                cache.forEachLine {
                    val word = Word(it)
                    if (filter(word)) {
                        mutableBuckets[bucketIndex(word)].add(word)
                    }
                    ++rawCount
                }
                buckets.indices.forEach { buckets[it] = mutableBuckets[it] }
                logi("PuzzleDict[$size] loaded and filtered from $rawCount cached words.")
            }
        } catch (_: Throwable) {
        }
    }

    constructor(unfilteredReader: BufferedReader) {
        var rawCount = 0
        val mutableBuckets = Array<MutableList<Word>>(26) { mutableListOf() }
        unfilteredReader.forEachLine { wordText ->
            toUsableWord(wordText)?.let { mutableBuckets[bucketIndex(it)].add(it) }
            ++rawCount
        }
        buckets.indices.forEach { buckets[it] = mutableBuckets[it] }
        logi("PuzzleDict[${size}] created from $rawCount unfiltered words.")
    }

    val size: Int
        get() {
            var count = 0
            buckets.forEach { count += it.size }
            return count
        }

    fun clear() {
        buckets = createBuckets()
    }

    fun isEmpty() = buckets.firstOrNull { it.isNotEmpty() } == null
    fun isNotEmpty() = buckets.firstOrNull { it.isNotEmpty() } != null

    fun bucket(letter: Char): List<Word> {
        assert(letter in 'a'..'z')
        return buckets[letter - 'a']
    }

    fun save(path: Path) {
        path.outputStream().bufferedWriter().use { cache ->
            for (bucket in buckets) {
                for (word in bucket) {
                    cache.write(word.text)
                    cache.newLine()
                }
            }
        }
        logi("PuzzleDict[$size] saved to cache.")
    }
}
