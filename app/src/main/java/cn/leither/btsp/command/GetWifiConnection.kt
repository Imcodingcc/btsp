package cn.leither.btsp.command

import android.bluetooth.BluetoothSocket
import org.json.JSONObject

class GetWifiConnection(socket: BluetoothSocket) : CommonCommand(socket) {
    override fun request(): JSONObject {
        return JSONObject("{'request': 'getWifiConnection'}")}
}
