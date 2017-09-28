package cn.leither.btsp.state

import android.bluetooth.BluetoothDevice
import android.util.Log
import cn.leither.btsp.task.ScanWifiListTask
import cn.leither.btsp.handlemsg.State
import cn.leither.btsp.fragment.WifiListFragment
import cn.leither.btsp.entity.ConnectedWifi
import cn.leither.btsp.entity.SearchableWifi
import java.io.Serializable

/**
 * Created by lvqiang on 17-8-28.
 */
class WifiListState(var stage : Stage = Stage.INIT, val activity: WifiListFragment): Serializable, State {
    lateinit var devl: List<String>
    lateinit var device: BluetoothDevice
    lateinit var wl: List<SearchableWifi>
    lateinit var cwl: MutableList<ConnectedWifi>
    lateinit var kwl: MutableList<ConnectedWifi>
    lateinit var scanWifiListTask: ScanWifiListTask

    enum class Stage(value: String) {
        DEFAULT("DEFAULT"),
        INIT("INIT"),
        SCANNING_DEV("SCANNING_DEV"),
        SCANNING_DEV_DONE("SCANNING_DEV_DONE"),
        SCANNING_DEV_FAILED("SCANNING_DEV_FAILED"),
        SCANNING_CONNECTED("SCANNING_CONNECTED"),
        SCAN_CONNECTED_DONE("SCAN_CONNECTED_DONE"),
        SCAN_CONNECTED_FAILED("SCAN_CONNECTED_FAILED"),
        SCANNING_WIFI_LIST("SCANNING_WIFI_LIST"),
        SCAN_WIFI_LIST_DISCOVER("SCAN_WIFI_LIST_DISCOVER"),
        SCAN_WIFI_LIST_FAILED("SCAN_WIFI_LIST_FAILED"),
        CANCEL_SCAN_WIFI("CANCEL_SCAN_WIFI"),
        CANCEL_SCAN_WIFI_DONE("CANCEL_SCAN_WIFI_DONE"),
        CANCEL_SCAN_WIFI_FAILED("CANCEL_SCAN_WIFI_FAILED"),
        DISCONNECT_WIFI("DISCONNECT_WIFI"),
        DISCONNECT_WIFI_DONE("DISCONNECT_WIFI_DONE"),
        DISCONNECT_WIFI_FAILED("DISCONNECT_WIFI_FAILED"),
        ACTIVATE_WIFI("ACTIVATE_WIFI"),
        ACTIVATED_WIFI("ACTIVATED_WIFI"),
        ACTIVATE_WIFI_FAILED("ACTIVATE_WIFI_FAILED"),
        CREATE_WIFI_CONNECTION("CREATE_WIFI_CONNECTION"),
        CREATED_WIFI_CONNECTION("CREATED_WIFI_CONNECTION"),
        CREATE_WIFI_FAILED("CREATE_WIFI_FAILED"),
        DELETE_WIFI_CONNECTION("DELETE_WIFI_CONNECTION"),
        DELETE_WIFI_CONNECTION_DONE("DELETE_WIFI_CONNECTION_DONE"),
        DELETE_WIFI_CONNECTION_FAILED("DELETE_WIFI_CONNECTION_FAILED"),
        DISCONNECTING("DISCONNECTING"),
        DISCONNECTED("DISCONNECTED"),
        DISCONNECT_FAILED("DISCONNECT_FAILED"),
        CLOSED("CLOSED")
    }

    fun toStage(stage: Stage, next: (new: WifiListState)-> Unit) {
        val old = this.stage
        Log.d("BTSP", String.format("state change: %s -> %s", old, stage))
        this.stage = stage
        next(this)
    }

    fun toStage(stage: Stage, next: (new: WifiListState, param:Any?)-> Unit, param: Any?) {
        val old = this.stage
        Log.d("BTSP", String.format("state change: %s -> %s", old, stage))
        this.stage = stage
        next(this, param)
    }

}