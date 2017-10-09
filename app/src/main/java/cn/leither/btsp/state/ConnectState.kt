package cn.leither.btsp.state

import android.util.Log
import cn.leither.btsp.activity.ConnectActivity
import cn.leither.btsp.entity.ConnectedWifi
import cn.leither.btsp.entity.SearchableWifi
import cn.leither.btsp.handlemsg.State
import java.io.Serializable

class ConnectState(var stage : Stage = Stage.INIT, val activity: ConnectActivity): Serializable, State {
    lateinit var devl: ArrayList<String>
    lateinit var wl: List<SearchableWifi>
    lateinit var cwl: MutableList<ConnectedWifi>
    lateinit var kwl: MutableList<ConnectedWifi>
    lateinit var isLock: String

    enum class Stage(value: String) {
        DEFAULT("DEFAULT"),
        INIT("INIT"),
        SCAN_WIFI_LIST("SCAN_WIFI_LIST"),
        SCAN_WIFI_LIST_DONE("SCAN_WIFI_LIST_DONE"),
        SCAN_WIFI_LIST_FAILED("SCAN_WIFI_LIST_FAILED"),
        CREATE_CONNECTION("CREATE_CONNECTION"),
        CREATE_CONNECTION_DONE("CREATE_CONNECTION_DONE"),
        CREATE_CONNECTION_FAILED("CREATE_CONNECTION_FAILED"),
        ACTIVE_CONNECTION("ACTIVE_CONNECTION"),
        ACTIVE_CONNECTION_DONE("ACTIVE_CONNECTION_DONE"),
        ACTIVE_CONNECTION_FAILED("ACTIVE_CONNECTION_FAILED"),

    }

    fun toStage(stage: Stage, next: (new: ConnectState)-> Unit) {
        val old = this.stage
        Log.d("BTSP", String.format("state change: %s -> %s", old, stage))
        this.stage = stage
        next(this)
    }

    fun toStage(stage: Stage, next: (new: ConnectState, param:Any?)-> Unit, param: Any?) {
        val old = this.stage
        Log.d("BTSP", String.format("state change: %s -> %s", old, stage))
        this.stage = stage
        next(this, param)
    }

}