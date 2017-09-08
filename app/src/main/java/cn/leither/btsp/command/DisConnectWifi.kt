package cn.leither.btsp.command

import cn.leither.btsp.command.CommonCommand
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by lvqiang on 17-8-24.
 */
class DisConnectWifi(input: InputStream, output: OutputStream) : CommonCommand(input, output) {
    var iface: JSONObject? = null
    override fun request(): JSONObject {
        val obj  = JSONObject("{'request': 'disconnectWifi'}")
        obj.put("param", iface)
        return obj
    }
}
