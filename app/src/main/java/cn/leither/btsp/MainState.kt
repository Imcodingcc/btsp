package cn.leither.btsp

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.Serializable
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Created by lvqiang on 17-8-17.
 */
class MainState(var stage : Stage = Stage.INIT, val activity: MainActivity): Serializable, State{
    lateinit var ba: BluetoothAdapter
    lateinit var devices: CopyOnWriteArraySet<BluetoothDevice>
    lateinit var sockets: MutableList<BluetoothSocket>
    lateinit var connectedSocket: BluetoothSocket
    lateinit var connectTask: ConnectTask

    enum class Stage(value: String) {
        DEFAULT("DEFAULT"),
        INIT("INIT"),
        SCANNING("SCANNING"),
        NOTFOUND("NOTFOUND"),
        CONNECTING("CONNECTING"),
        CONNECTED("CONNECTED"),
        CLOSED("CLOSED")
    }

    fun toStage(stage: Stage, next: (new: MainState)-> Unit) {
        val old = this.stage
        val new = stage
        Log.d("BTSP", String.format("state change: %s -> %s", old, new))
        this.stage.to(stage)
        next(this)
    }
}

