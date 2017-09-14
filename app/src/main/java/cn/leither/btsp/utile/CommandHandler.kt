package cn.leither.btsp.utile

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import cn.leither.btsp.command.*
import org.json.JSONObject
import java.io.IOException

class CommandHandler {
    companion object {
        private fun getCommand(command: String, socket: BluetoothSocket): CommonCommand? {
            return when (command) {
                "ACTIVATE_WIFI_CONNECTION" -> ActivateWifiConnection(socket)
                "DELETE_WIFI_CONNECTION" -> DeleteWIfiConnection(socket)
                "CREATE_WIFI_CONNECTION" -> CreateWifiConnection(socket)
                "GET_WIFI_CONNECTION" -> GetWifiConnection(socket)
                "DISCONNECT_WIFI" -> DisConnectWifi(socket)
                "GET_WIFI_DEV" -> GetDevCommand(socket)
                "SCAN_WIFI" -> ScanCommand(socket)
                else -> null
            }
        }

        fun handleCommand(commandStr: String, device: BluetoothDevice): JSONObject? {
            synchronized(device){
                Log.w("BTSP", "LOCKED")
                try {
                    val socket = device.createInsecureRfcommSocketToServiceRecord(Const.uuid)
                    socket.connect()
                    val command = getCommand(commandStr, socket)
                    command!!.send()
                    val receive = command.recv()
                    if (receive != null) {
                        Log.w("BTSP", "UNLOCKED")
                        return receive
                    }
                    return null
                } catch (e: Exception) {
                    Log.w("BTSP", "UNLOCKED")
                    return null
                }
            }
        }

        fun handleCommand(commandStr: String, device: BluetoothDevice, param: JSONObject): JSONObject? {
            synchronized(device){
                try {
                    val socket = device.createInsecureRfcommSocketToServiceRecord(Const.uuid)
                    try {
                        socket.connect()
                        if (socket.isConnected) {
                            val command = getCommand(commandStr, socket)
                            command!!.param = param
                            command.send()
                            val receive = command.recv()
                            if (receive != null) {
                                return receive
                            }
                            return null
                        } else {
                            throw Exception("SOCKET CONNECT ERR")
                        }
                    } catch (e: Exception) {
                        Log.d("BTSP", "RECEIVE ERR " + e.localizedMessage)
                        return null
                    } finally {
                        Log.d("BTSP", "PRE CLOSE")
                        socket.close()
                    }
                } catch (e: IOException) {
                    Log.d("BTSP", "Create Error " + e.localizedMessage)
                    return null
                } finally {
                    Thread.sleep(50)
                }
            }
        }
    }
}