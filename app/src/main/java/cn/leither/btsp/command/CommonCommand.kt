package cn.leither.btsp.command

import cn.leither.btsp.handlemsg.JSONLengthPrefixedDecoder
import cn.leither.btsp.handlemsg.JSONLengthPrefixedEncoder
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by lvqiang on 17-8-21.
 */
abstract class CommonCommand(input:InputStream, output: OutputStream ){
    val encoder = JSONLengthPrefixedEncoder()
    val decoder = JSONLengthPrefixedDecoder()
    val input: InputStream
    val output: OutputStream
    init {
        this.input = input
        this.output = output
    }

    abstract fun request(): JSONObject

    fun send() {
        val req = request()
        encoder.encodeTo(output, req)
    }

    fun recv(): JSONObject?{
        return decoder.decodeFrom(input)
    }
}