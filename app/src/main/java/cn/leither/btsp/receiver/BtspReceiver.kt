package cn.leither.btsp.receiver

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import cn.leither.btsp.handlemsg.AdapterMessage
import cn.leither.btsp.handlemsg.DeviceMessage
import cn.leither.btsp.handlemsg.EventEmitter

class BtspReceiver: BroadcastReceiver(){

    override fun onReceive(p0: Context?, p1: Intent?) {
        val ee = EventEmitter.default
        when(p1?.action){
            BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                ee.emit(AdapterMessage(AdapterMessage.Type.STARTED, null))
            }
            BluetoothAdapter.ACTION_DISCOVERY_FINISHED-> {
                ee.emit(AdapterMessage(AdapterMessage.Type.STOPPED, null))
            }
            BluetoothDevice.ACTION_FOUND -> {
                val device = p1.getParcelableExtra<Parcelable>(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice
                ee.emit(DeviceMessage(DeviceMessage.Type.FOUND, device))
            }
        }
    }
}


