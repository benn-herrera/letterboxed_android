package com.tinybitsinteractive.lbsolver

import org.junit.Test

import org.junit.Assert.*
import java.io.StringReader

class PuzzleDictTest {
    @Test
    fun unfilteredSourceConstructorCheck() {
        val testWords = listOf(
            "aabc", // double letter - no
            "Abc", // should be added
            "abcdefghijklm", // too many unique letters - no
            "bcabcabcabcabcabC", // should be added
            "mm", // too short, no
        )
        val testDict = PuzzleDict(
            StringReader(
                buildString {
                    testWords.forEach{
                        append("$it\n")
                    } }).buffered()
        )
        assertEquals(testDict.size, 2)
        assertEquals(testDict.bucket('a').size, 1)
        assertEquals(testDict.bucket('a').first().text, "abc")
        assertEquals(testDict.bucket('b').size, 1)
        assertEquals(testDict.bucket('b').first().text, "bcabcabcabcabcabc")
    }

    @Test
    fun saveCheck() {

    }

    @Test
    fun filteredCacheSourceConstructorCheck() {

    }
}