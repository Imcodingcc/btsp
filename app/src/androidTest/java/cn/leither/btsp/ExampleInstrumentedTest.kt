package cn.leither.btsp

import android.support.test.InstrumentationRegistry
import android.support.test.filters.SmallTest
import android.support.test.runner.AndroidJUnit4
import cn.leither.btsp.command.GetDevCommand

import org.junit.Test
import org.junit.runner.RunWith

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
@SmallTest
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        val buffer = ByteArray(1024)
        val input = ByteArrayInputStream(buffer)
        val output = ByteArrayOutputStream()
        //val wi = GetDevCommand()
        //wi.send()
        //assertEquals("cn.leither.btsp", appContext.packageName)
    }
}
