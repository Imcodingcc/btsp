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


class ConnectMessage(val msgType: Type, val value: Any?) : EventEmitter.Message {
    override val type = "ConnectMessage"
    enum class Type(val value: String) {
        WIFI_NAME("WIFI_NAME"),
        INTERFACE_NAME("INTERFACE_NAME"),
        SCAN_WIFI_LIST("SCAN_WIFI_LIST"),
        WIFI_LIST_DIALOG_CLOSE("WIFI_LIST_DIALOG_CLOSE"),
        INTERFACE_LIST_DIALOG_CLOSE("INTERFACE_LIST_DIALOG_CLOSE"),
        SCAN_WIFI_LIST_DONE("SCAN_WIFI_LIST_DONE"),
        SCAN_WIFI_LIST_FAILED("SCAN_WIFI_LIST_FAILED"),
        CREATE_CONNECTION("CREATE_CONNECTION"),
        CREATE_CONNECTION_DONE("CREATE_CONNECTION_DONE"),
        CREATE_CONNECTION_FAILED("CREATE_CONNECTION_FAILED"),
        ACTIVE_CONNECTION("ACTIVE_CONNECTION"),
        ACTIVE_CONNECTION_DONE("ACTIVE_CONNECTION_DONE"),
        ACTIVE_CONNECTION_FAILED("ACTIVE_CONNECTION_FAILED"),
    }
}


class WifiDetailMessage(val msgType: Type, val value: Any?): EventEmitter.Message {
    override val type = "WifiDetailMessage"
    enum class Type(val value: String){
        INIT("INIT"),
        GET_INTERFACE_DETAIL("GET_INTERFACE_DETAIL"),
        GET_INTERFACE_DETAIL_FAILED("GET_INTERFACE_DETAIL_FAILED"),
        DISCONNECTION("DISCONNECTION"),
        DISCONNECTION_DONE("DISCONNECTION_DONE"),
        DISCONNECTION_FAILED("DISCONNECTION_FAILED"),
        DELETE_CONNECTION("DELETE_CONNECTION"),
        DELETE_CONNECTION_DONE("DELETE_CONNECTION_DONE"),
        DELETE_CONNECTION_FAILED("DELETE_CONNECTION_FAILED"),
    }
}


class WifiListMessage(val msgType: Type, val value: Any?): EventEmitter.Message {
    override val type = "WifiListMessage"
    enum class Type(val value: String){
        DEFAULT("DEFAULT"),
        INIT("INIT"),
        BACK("BACK"),
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