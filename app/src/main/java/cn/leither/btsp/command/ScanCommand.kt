package cn.leither.btsp.command

import android.bluetooth.BluetoothSocket
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream

class ScanCommand(socket: BluetoothSocket) : CommonCommand(socket) {
    override fun request(): JSONObject {
        return JSONObject("{'request': 'getScanResult'}")}
}
