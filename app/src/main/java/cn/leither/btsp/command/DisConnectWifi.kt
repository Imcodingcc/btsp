package cn.leither.btsp.command

import android.bluetooth.BluetoothSocket
import org.json.JSONObject

class DisConnectWifi(socket: BluetoothSocket) : CommonCommand(socket) {
    override fun request(): JSONObject {
        val obj  = JSONObject("{'request': 'disconnectWifi'}")
        obj.put("param", param)
        return obj
    }
}
