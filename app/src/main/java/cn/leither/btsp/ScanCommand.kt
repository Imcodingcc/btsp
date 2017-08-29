package cn.leither.btsp

import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by lvqiang on 17-8-22.
 */
class ScanCommand(input: InputStream, output: OutputStream) : CommonCommand(input, output) {
    override fun request(): JSONObject {
        return JSONObject("{'request': 'getScanResult'}")}
}