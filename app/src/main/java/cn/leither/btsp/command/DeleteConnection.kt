package cn.leither.btsp.command

import android.bluetooth.BluetoothSocket
import org.json.JSONObject

class DeleteConnection(socket: BluetoothSocket): CommonCommand(socket){
    override fun request(): JSONObject {
        val obj  = JSONObject("{'request': 'deleteConnection'}")
        obj.put("param", param)
        return obj
    }
}

