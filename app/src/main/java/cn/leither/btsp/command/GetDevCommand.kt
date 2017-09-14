package cn.leither.btsp.command

import android.bluetooth.BluetoothSocket
import org.json.JSONObject

class GetDevCommand(socket: BluetoothSocket) : CommonCommand(socket) {
    override fun request(): JSONObject {
        return JSONObject("{'request': 'getWifiInterface'}")
    }
}