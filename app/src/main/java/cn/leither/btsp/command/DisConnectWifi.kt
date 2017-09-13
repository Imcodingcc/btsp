package cn.leither.btsp.command

import android.bluetooth.BluetoothSocket
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream

class DisConnectWifi(socket: BluetoothSocket) : CommonCommand(socket) {
    override fun request(): JSONObject {
        val obj  = JSONObject("{'request': 'disconnectWifi'}")
        obj.put("param", param)
        return obj
    }
}
