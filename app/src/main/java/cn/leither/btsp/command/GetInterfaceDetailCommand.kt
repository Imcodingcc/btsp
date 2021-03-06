package cn.leither.btsp.command

import android.bluetooth.BluetoothSocket
import org.json.JSONObject

class GetInterfaceDetailCommand(socket: BluetoothSocket) : CommonCommand(socket) {
    override fun request(): JSONObject {
        val obj  = JSONObject("{'request': 'getInterfaceDetail'}")
        obj.put("param", param)
        return obj
    }
}