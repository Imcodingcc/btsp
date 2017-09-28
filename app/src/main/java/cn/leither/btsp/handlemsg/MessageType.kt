package cn.leither.btsp.handlemsg

import android.bluetooth.BluetoothDevice

class AdapterMessage(val msgType: Type, val value: Any?) : EventEmitter.Message {
    override val type = "AdapterMessage"

    enum class Type(val value: String) {
        STARTED("SCAN STARTED"),
        STOPPED("SCAN FINISHED")
    }
}

class DeviceMessage(val msgType: Type, val value: BluetoothDevice) : EventEmitter.Message {
    override val type = "DeviceMessage"
    enum class Type(val value: String) {
        FOUND("FOUND"),
    }
}

class LoadingMessage(val msgType: Type, val value: Any?): EventEmitter.Message {
    override val type = "LoadingMessage"
    enum class Type(val value: String) {
        DEFAULT("DEFAULT"),
        INIT("INIT"),
        SCANNING("SCANNING"),
        SCANDONE("SCAN_DONE"),
        CONNECTING("CONNECTING"),
        CONNECTED("CONNECTED"),
        CONNECTFAILED("CONNECT_FAILED")
    }
}

class IntermediateMessage(val msgType: Type, val value: Any?): EventEmitter.Message {
    override val type = "IntermediateMessage"
    enum class Type(val value: String){
        SWITCH_VIEW("SWITCH_VIEW")
    }
}


class WifiDetailMessage(val msgType: Type, val value: Any?): EventEmitter.Message {
    override val type = "WifiDetailMessage"
    enum class Type(val value: String){
        INIT("INIT"),
        GET_INTERFACE_DETAIL("GET_INTERFACE_DETAIL")
    }
}


class WifiListMessage(val msgType: Type, val value: Any?): EventEmitter.Message {
    override val type = "WifiListMessage"
    enum class Type(val value: String){
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
        CANCEL_SCAN_WIFI_FOR_CONNECTION("CANCEL_SCAN_WIFI_FOR_CONNECTION"),
        CANCEL_SCAN_WIFI_DONE("CANCEL_SCAN_WIFI_DONE"),
        CANCEL_SCAN_WIFI_FAILED("CANCEL_SCAN_WIFI_FAILED"),
        DISCONNECT_WIFI("DISCONNECT_WIFI"),
        DISCONNECT_WIFI_DONE("DISCONNECT_WIFI_DONE"),
        DISCONNECT_WIFI_FAILED("DISCONNECT_WIFI_FAILED"),
    }
}