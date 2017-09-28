package cn.leither.btsp.state

import android.util.Log
import cn.leither.btsp.activity.WifiDetailActivity
import cn.leither.btsp.handlemsg.State
import java.io.Serializable

class WifiDetailState(var stage : Stage = Stage.INIT, val activity: WifiDetailActivity): Serializable, State {

    enum class Stage(value: String) {
        INIT("INIT"),
        GET_INTERFACE_DETAIL("GET_INTERFACE_DETAIL"),
        DISCONNECTION("DISCONNECTION"),
        FORGET_CONNECTION("FORGET_CONNECTION"),
    }

    fun toStage(stage: Stage, next: (new: WifiDetailState)-> Unit) {
        val old = this.stage
        Log.d("BTSP", String.format("state change: %s -> %s", old, stage))
        this.stage = stage
        next(this)
    }
}