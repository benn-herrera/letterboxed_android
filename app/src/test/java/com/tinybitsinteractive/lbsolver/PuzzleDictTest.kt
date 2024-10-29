package com.tinybitsinteractive.lbsolver

import org.junit.Test

import org.junit.Assert.*
import java.io.StringReader

class PuzzleDictTest {
    @Test
    fun unfilteredSourceConstructorCheck() {
        val testWords =
"""aabc
Abc
"abcdefghijklm
bcabcabcabcabcabC
mm
"""
        // logging must be mocked for unit tests
        Logger.factory = PrintLoggerFactory()

        val testDict = PuzzleDict(StringReader(testWords).buffered())
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