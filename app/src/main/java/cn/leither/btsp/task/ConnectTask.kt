package cn.leither.btsp.task

import android.bluetooth.BluetoothSocket
import android.os.AsyncTask
import android.util.Log
import cn.leither.btsp.state.LoadingState
import cn.leither.btsp.utile.Const
import java.io.IOException

class ConnectTask(val state: LoadingState): AsyncTask<Unit, BluetoothSocket?, Boolean>(){

    override fun doInBackground(vararg devices: Unit): Boolean {
        return arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0).any {
            Log.d("BTSP", "CONNECTION RETRY")
            isConnected()
        }
    }

    private fun isConnected(): Boolean{
        val device = state.device
        val socket = device.createInsecureRfcommSocketToServiceRecord(Const.uuid)
        try {
            socket.connect()
        }catch (e: IOException) {

        }
        publishProgress(socket)
        return socket.isConnected
    }

    override fun onPostExecute(result: Boolean) {
        super.onPostExecute(result)
        if (result) {
            state.toStage(LoadingState.Stage.CONNECTED, state.activity::toScanningDev)
        } else {
            Log.d("BTSP", "连接失败了")
            state.toStage(LoadingState.Stage.CONNECT_FAILED, state.activity::toConnectFailed)
        }
    }
}