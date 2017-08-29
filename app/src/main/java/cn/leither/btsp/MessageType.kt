package cn.leither.btsp

import android.bluetooth.BluetoothDevice

/**
 * Created by lvqiang on 17-8-29.
 */
class AdapterMessage(val msgType: Type, val value: Any?) : EventEmitter.Message{
    override val type = "AdapterMessage"

    enum class Type(val value: String) {
        STARTED("SCAN STARTED"),
        STOPPED("SCAN FINISHED")
    }
}

class DeviceMessage(val msgType: Type, val value: BluetoothDevice) :EventEmitter.Message{
    override val type = "DeviceMessage"
    enum class Type(val value: String) {
        FOUND("FOUND"),
    }
}

class LoadingMessage(val msgType: Type, val value: Any?): EventEmitter.Message{
    override val type = "LoadingMessage"
    enum class Type(val value: String) {
        DEFAULT("DEFAULT"),
        INIT("INIT"),
        SCANNING("SCANNING"),
        SCANDONE("SCANDONE"),
        CONNECTING("CONNECTING"),
        CONNECTED("CONNECTED"),
        CONNECTFAILED("CONNECTFAILED")
    }
}

class IntermediateMessage(val msgType: Type, val value: Any): EventEmitter.Message{
    override val type = "IntermediateMessage"
    enum class Type(val value: String){
        SWITCH_VIEW("SWITCH_VIEW")
    }
}
