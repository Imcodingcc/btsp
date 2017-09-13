package cn.leither.btsp.task

import android.bluetooth.BluetoothSocket
import android.os.AsyncTask
import android.util.Log
import cn.leither.btsp.state.LoadingState
import cn.leither.btsp.utile.Const
import java.io.IOException
import java.util.*

class ConnectTask(val state: LoadingState): AsyncTask<Unit, BluetoothSocket?, Boolean>(){

    override fun doInBackground(vararg devices: Unit): Boolean {
        return arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0).any {
            Log.d("BTSP", "CONNECTION RETRY")
            isConnected()
        }
    }

    private fun isConnected(): Boolean{
        val deviceSet = state.devices
        val it = deviceSet.iterator()
        while(it.hasNext()){
            val d = it.next()
            val socket: BluetoothSocket
            try {
                socket = d.createInsecureRfcommSocketToServiceRecord(Const.uuid)
                socket.connect()
                if(socket.isConnected){
                    state.sockets.add(socket)
                    state.connectedSocket = socket
                }
                publishProgress(socket)
                return state.sockets.isNotEmpty()
            } catch (e: IOException) {
                Thread.sleep(100)
            } finally {
            }
        }
        return false
    }

    override fun onPostExecute(result: Boolean) {
        super.onPostExecute(result)
        if (result) {
            Log.d("BTSP", String.format("connected"))
            state.toStage(LoadingState.Stage.CONNECTED, state.activity::toConnected)
        } else {
            state.toStage(LoadingState.Stage.CONNECTFAILED, state.activity::toConnectFailed)
            Log.d("BTSP", String.format("connect failed"))
        }
    }

}