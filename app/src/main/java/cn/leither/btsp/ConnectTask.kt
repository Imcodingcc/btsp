package cn.leither.btsp

import android.bluetooth.BluetoothSocket
import android.os.AsyncTask
import android.util.Log
import java.io.IOException
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Created by lvqiang on 17-8-17.
 */
class ConnectTask(val state: LoadingState): AsyncTask<Unit, BluetoothSocket?, Boolean>(){
    val uuid = UUID.fromString("3a75d027-49b6-40e7-8c22-e08bf79988d3")
    override fun doInBackground(vararg devices: Unit): Boolean {
        return arrayOf(0, 0, 0, 0, 0).any {
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
                socket = d.createInsecureRfcommSocketToServiceRecord(uuid)
                socket.connect()
                if(socket.isConnected) state.sockets.add(socket)
                publishProgress(socket)
                return state.sockets.isNotEmpty()
                publishProgress(socket)
            } catch (e: IOException) {
                Thread.sleep(2000)
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