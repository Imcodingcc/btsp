package cn.leither.btsp.state

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import cn.leither.btsp.task.ConnectTask
import cn.leither.btsp.fragment.LoadingFragment
import cn.leither.btsp.handlemsg.State
import java.io.Serializable
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet

class LoadingState(var stage : Stage = Stage.INIT, val activity: LoadingFragment): Serializable, State {
    lateinit var ba: BluetoothAdapter
    lateinit var devices: CopyOnWriteArraySet<BluetoothDevice>
    var sockets: MutableList<BluetoothSocket> = CopyOnWriteArrayList<BluetoothSocket>()
    lateinit var connectedSocket: BluetoothSocket
    lateinit var connectTask: ConnectTask

    enum class Stage(value: String) {
        DEFAULT("DEFAULT"),
        INIT("INIT"),
        SCANNING("SCANNING"),
        SCANDONE("SCANDONE"),
        NOTFOUND("NOTFOUND"),
        CONNECTING("CONNECTING"),
        CONNECTED("CONNECTED"),
        CONNECTFAILED("CONNECTFAILED"),
        CLOSED("CLOSED")
    }

    fun toStage(stage: Stage, next: (new: LoadingState)-> Unit) {
        val old = this.stage
        Log.d("BTSP", String.format("state change: %s -> %s", old, stage))
        this.stage = stage
        next(this)
    }
}