package androidx.test.gradletests.monitor

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.io.PlatformTestStorageRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.io.OutputStreamWriter

/** Simplest possible test  */
@RunWith(JUnit4::class)
class PlatformTestStorageTest {


        @Test
        fun inputFile() {

            val f = File(InstrumentationRegistry.getInstrumentation().context.externalCacheDir, "testcontent.txt")
            BufferedReader(InputStreamReader(FileInputStream(f))).use {
                assertEquals(it.readText().trim(), "test content")
            }
        }



    @Test
    fun readNonExistentInputFile() {
        val storage = PlatformTestStorageRegistry.getInstance()
        assertThrows(FileNotFoundException::class.java) { storage.openInputFile("not/here") }
    }

    /**
     * Simple test for writing a file. Should be executed on every Android API to test for
     * incompatibilities.
     */
    @Test
    fun writeFile() {
        val storage = PlatformTestStorageRegistry.getInstance()
        BufferedWriter(OutputStreamWriter(storage.openOutputFile("testcontent.txt"))).use {
            it.write("test content\n")
        }
    }
}
