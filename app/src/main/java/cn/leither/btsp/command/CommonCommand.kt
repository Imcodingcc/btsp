package cn.leither.btsp.command

import android.bluetooth.BluetoothSocket
import android.util.Log
import cn.leither.btsp.handlemsg.JSONLengthPrefixedDecoder
import cn.leither.btsp.handlemsg.JSONLengthPrefixedEncoder
import cn.leither.btsp.utile.SocketInputStream
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

abstract class CommonCommand(private val socket: BluetoothSocket, private val timeout: Long = 25000){
    private val encoder = JSONLengthPrefixedEncoder()
    private val decoder = JSONLengthPrefixedDecoder()
    private val input = SocketInputStream(socket.inputStream)
    private val output = socket.outputStream!!
    var param: JSONObject? = null

    abstract fun request(): JSONObject

    fun send() {
        val req = request()
        Log.d("BTSP", "REQUEST MSG" + req.toString())
        val isOk = encoder.encodeTo(output, req)
        Log.d("BTSP", "REQUEST IS OK " + isOk)
    }

    fun recv(): JSONObject?{
        return try {
            decoder.decodeFromWithTimeout(input, timeout)
        }catch (e: IOException){
            Log.d("BTSP", "DECODE ERR " + e.localizedMessage)
            null
        }
    }
}