package cn.leither.btsp.command

import android.bluetooth.BluetoothSocket
import org.json.JSONObject

class ScanCommand(socket: BluetoothSocket) : CommonCommand(socket) {
    override fun request(): JSONObject {
        return JSONObject("{'request': 'getScanResult'}")}
}
