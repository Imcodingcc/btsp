package cn.leither.btsp

import org.json.JSONObject
import org.junit.Test

import org.junit.Assert.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val buffer = ByteArray(1024)
        val input = ByteArrayInputStream(buffer)
        val output = ByteArrayOutputStream()
        val wi = WiCommand(input, output)
        wi.send()
        val expect = JSONObject("{'cmd': 'getWifiInterface'}")
        assertEquals(expect, output.toByteArray())
    }
}
