package cn.leither.btsp.command

import android.bluetooth.BluetoothSocket
import org.json.JSONObject

class DeleteWIfiConnection(socket: BluetoothSocket): CommonCommand(socket){
    override fun request(): JSONObject {
        val obj  = JSONObject("{'request': 'deleteWifiConnection'}")
        obj.put("param", param)
        return obj
    }
}

