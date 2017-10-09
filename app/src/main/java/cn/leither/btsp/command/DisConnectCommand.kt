package cn.leither.btsp.command

import android.bluetooth.BluetoothSocket
import org.json.JSONObject

class DisConnectCommand(socket: BluetoothSocket) : CommonCommand(socket) {
    override fun request(): JSONObject {
        val obj  = JSONObject("{'request': 'disconnect'}")
        obj.put("param", param)
        return obj
    }
}
