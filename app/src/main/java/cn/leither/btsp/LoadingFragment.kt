package cn.leither.btsp

import android.app.Fragment
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.IntentFilter
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import cn.leither.btsp.databinding.FragmentLoadingBinding
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Created by lvqiang on 17-8-29.
 */
class LoadingFragment: Fragment(){

    var binding: FragmentLoadingBinding? = null
    private lateinit var state: LoadingState
    private val OPEN_BLUETOOTH_SUCCESS: Int = 1
    private val ee = EventEmitter.default
    private var mainReceiver: BtspReceiver? = null
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_loading, container, false)
        state = LoadingState(activity = this)
        state.devices = CopyOnWriteArraySet<BluetoothDevice>()
        state.toStage(LoadingState.Stage.INIT, this::toInit)
        return binding!!.root
    }


    fun toInit(old: LoadingState){
        registerReceiver()
        old.ba = BluetoothAdapter.getDefaultAdapter()
        if(old.ba == null){
            ee.emit(LoadingMessage(LoadingMessage.Type.INIT, false))
        }else{
            if (!old.ba.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, OPEN_BLUETOOTH_SUCCESS)
            }else{
                ee.emit(LoadingMessage(LoadingMessage.Type.INIT, true))
                old.toStage(LoadingState.Stage.SCANNING, this::toScanning)
            }
        }
    }

    init { ee.register("LoadingMessage", this::onInit) }
    private fun onInit(message: EventEmitter.Message){
        val msg = message as LoadingMessage
        if(msg.msgType == LoadingMessage.Type.INIT && msg.value == false){
            val prompt = "Device does not support Bluetooth"
            Toast.makeText(activity, prompt, Toast.LENGTH_SHORT).show()
            binding!!.prompt = prompt
        }else if(msg.msgType == LoadingMessage.Type.INIT && msg.value as Boolean){
            binding!!.ballClipRotatePulseIndicator.smoothToHide()
            binding!!.prompt = ""
        }
    }

    fun toScanning(old: LoadingState){
        old.ba.cancelDiscovery()
        old.ba.startDiscovery()
    }

    init{ee.register("AdapterMessage", this::onScanning) }
    private fun onScanning(message: EventEmitter.Message){
        Log.d("BTSP", "SCANNING")
        val msg = message as AdapterMessage
        when(msg.msgType){
            AdapterMessage.Type.STARTED -> {
                binding!!.ballClipRotatePulseIndicator.smoothToShow()
                binding!!.prompt = "scanning"
            }
            AdapterMessage.Type.STOPPED -> {
                state.toStage(LoadingState.Stage.CONNECTING, this::toScanDone)
            }
        }
    }

    fun toScanDone(old: LoadingState){
        ee.emit(LoadingMessage(LoadingMessage.Type.SCANDONE, null))
        old.toStage(LoadingState.Stage.CONNECTING, this::toConnecting)
    }

    init{ ee.register("LoadingMessage", this::onScanDone) }
    private fun onScanDone(message: EventEmitter.Message){
        val msg = message as LoadingMessage
        if(msg.msgType == LoadingMessage.Type.SCANDONE){
            binding!!.prompt = "scan done"
        }
    }

    fun toConnecting(old: LoadingState) {
        ee.emit(LoadingMessage(LoadingMessage.Type.CONNECTING, null))
        state.connectTask = ConnectTask(state)
        state.connectTask.execute()
    }

    init{ ee.register("LoadingMessage", this::onConnecting) }
    fun onConnecting(message: EventEmitter.Message){
        val msg = message as LoadingMessage
        if(msg.msgType == LoadingMessage.Type.CONNECTING){
            binding!!.prompt = "connecting"
        }
    }

    fun toConnected(old: LoadingState){
        //TODO toStage
        ee.emit(LoadingMessage(LoadingMessage.Type.CONNECTED, null))
    }

    init{ ee.register("LoadingMessage", this::onConnected) }
    fun onConnected(message: EventEmitter.Message){
        val msg = message as LoadingMessage
        if(msg.msgType == LoadingMessage.Type.CONNECTED){
            binding!!.prompt = "connected"
            binding!!.ballClipRotatePulseIndicator.hide()
        }
    }

    fun toConnectFailed(old: LoadingState){
        //TODO toStage
        ee.emit(LoadingMessage(LoadingMessage.Type.CONNECTFAILED, null))
    }

    init { ee.register("LoadingMessage", this::onConnectFailed) }
    private fun onConnectFailed(message: EventEmitter.Message){
        val msg = message as LoadingMessage
        if(msg.msgType == LoadingMessage.Type.CONNECTFAILED){
            binding!!.prompt = "connectFailed"
            binding!!.ballClipRotatePulseIndicator.hide()
        }
    }

    init { ee.register("DeviceMessage", this::onFound) }
    private fun onFound(message: EventEmitter.Message) {
        val msg = message as DeviceMessage
        val device = msg.value
        if (msg.msgType == DeviceMessage.Type.FOUND) {
            Log.d("BTSP", String.format("%s: %s %s", msg.msgType.value, device.address, device.name))
            if(device.name == "lvqiang") state.devices.add(device)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == OPEN_BLUETOOTH_SUCCESS){
            state.toStage(LoadingState.Stage.SCANNING, this::toScanning)
        }
    }

    private fun registerReceiver() {
        val intentFilter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND)
        activity.registerReceiver(mainReceiver, intentFilter)
    }
}