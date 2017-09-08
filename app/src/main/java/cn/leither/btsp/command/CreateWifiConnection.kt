package cn.leither.btsp.command

import cn.leither.btsp.command.CommonCommand
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by lvqiang on 17-8-24.
 */
class CreateWifiConnection(input: InputStream, output: OutputStream) : CommonCommand(input, output) {
    var param: JSONObject? = null
    override fun request(): JSONObject {
        val obj  = JSONObject("{'request': 'createWifiConnection'}")
        obj.put("param", param)
        return obj
    }
}
