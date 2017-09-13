package cn.leither.btsp.handlemsg

import cn.leither.btsp.utile.SocketInputStream
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.*


/**
 * Created by lvqiang on 17-8-18.
 */

interface Encoder<in T> {
    fun encode(v: T): ByteArray
    fun encodeTo(output: OutputStream, v: T): Boolean
}

interface Decoder<out T> {
    fun decode(b: ByteArray): T?
    fun decodeFrom(input: InputStream): T?
}

class JSONLengthPrefixedEncoder: Encoder<JSONObject> {
    override fun encode(v: JSONObject): ByteArray {
        val string = v.toString().toByteArray(Charset.forName("UTF-8"))
        val buffer = ByteArray(string.size + 2)
        var bb = ByteBuffer.wrap(buffer,0,2)
        bb.putShort(string.size.toShort())
        bb = ByteBuffer.wrap(buffer, 2, string.size)
        bb.put(string)
        return buffer
    }

    override fun encodeTo(output: OutputStream, v: JSONObject): Boolean {
        val buffer = encode(v)
        return try{
            output.write(buffer)
            true
        } catch (e: IOException) {
            false
        }
    }
}

class JSONLengthPrefixedDecoder: Decoder<JSONObject> {
    override fun decode(b: ByteArray): JSONObject? {
        val c = b.slice(IntRange(2, b.size)).toByteArray()
        return try {
            JSONObject(c.toString(Charset.forName("UTF-8")))
        } catch (e: JSONException) {
            null
        }
    }

    fun decodeFromWithTimeout(input: SocketInputStream, timeout:Long): JSONObject? {
        input.deadline = Date(Date().time + timeout)
        return decodeFrom(input)
    }

    override fun decodeFrom(input: InputStream): JSONObject? {
        val head = ByteArray(2)
        val len:Int
        var n = input.read(head, 0, 2)
        while (n < 2) {
            val s = input.read(head, n, 2 - n)
            n += s
        }

        val bb = ByteBuffer.wrap(head)
        len = bb.short.toInt()

        val tail = ByteArray(len)

        var m = input.read(tail, 0, len)
        while (m != len) {
            val s = input.read(tail, m, len-m)
            m += s
        }
        return decode(head+tail)
    }
}

