package cn.leither.btsp

import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by lvqiang on 17-8-24.
 */
class GetWifiConnection(input: InputStream, output: OutputStream) : CommonCommand(input, output) {
    override fun request(): JSONObject {
        return JSONObject("{'request': 'getWifiConnection'}")}
}
