package cn.leither.btsp.state

import android.bluetooth.BluetoothDevice
import android.util.Log
import cn.leither.btsp.entity.ConnectedWifi
import cn.leither.btsp.task.ConnectTask
import cn.leither.btsp.fragment.LoadingFragment
import cn.leither.btsp.handlemsg.State
import java.io.Serializable

class LoadingState(var stage : Stage = Stage.INIT, val activity: LoadingFragment): Serializable, State {
    lateinit var devl: List<String>
    lateinit var device: BluetoothDevice
    lateinit var connectTask: ConnectTask
    lateinit var cwl: MutableList<ConnectedWifi>
    lateinit var kwl: MutableList<ConnectedWifi>

    enum class Stage(value: String) {
        DEFAULT("DEFAULT"),
        INIT("INIT"),
        SCANNING("SCANNING"),
        SCAN_DONE("SCAN_DONE"),
        NOT_FOUND("NOT_FOUND"),
        CONNECTING("CONNECTING"),
        CONNECTED("CONNECTED"),
        CONNECT_FAILED("CONNECT_FAILED"),
        SCANNING_DEV("SCANNING_DEV"),
        SCANNING_DEV_DONE("SCANNING_DEV_DONE"),
        SCANNING_DEV_FAILED("SCANNING_DEV_FAILED"),
        SCANNING_CONNECTED("SCANNING_CONNECTED"),
        SCAN_CONNECTED_DONE("SCAN_CONNECTED_DONE"),
        SCAN_CONNECTED_FAILED("SCAN_CONNECTED_FAILED"),
        SCANNING_WIFI_LIST("SCANNING_WIFI_LIST"),
        SCAN_WIFI_LIST_DISCOVER("SCAN_WIFI_LIST_DISCOVER"),
        SCAN_WIFI_LIST_FAILED("SCAN_WIFI_LIST_FAILED"),
        CLOSED("CLOSED")
    }

    fun toStage(stage: Stage, next: (new: LoadingState)-> Unit) {
        val old = this.stage
        Log.d("BTSP", String.format("state change: %s -> %s", old, stage))
        this.stage = stage
        next(this)
    }
}