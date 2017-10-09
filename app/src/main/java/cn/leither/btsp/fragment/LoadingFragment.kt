package cn.leither.btsp.fragment

import android.animation.ValueAnimator
import android.app.Activity
import android.app.Fragment
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.databinding.DataBindingUtil
import android.databinding.ObservableArrayList
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import cn.leither.btsp.*
import cn.leither.btsp.databinding.FragmentLoadingBinding
import cn.leither.btsp.entity.ConnectedWifi
import cn.leither.btsp.handlemsg.*
import cn.leither.btsp.receiver.BtspReceiver
import cn.leither.btsp.state.LoadingState
import cn.leither.btsp.task.ConnectTask
import cn.leither.btsp.utile.CommandHandler
import cn.leither.btsp.view.Animators
import org.json.JSONObject

class LoadingFragment: Fragment(){
    var binding: FragmentLoadingBinding? = null
    private lateinit var state: LoadingState
    private val OPEN_BLUETOOTH_SUCCESS: Int = 1
    private val ee = EventEmitter.default
    private var mainReceiver: BtspReceiver? = null
    private var mAnimator:ValueAnimator? = null
    private var ba: BluetoothAdapter? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mainReceiver = BtspReceiver()
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_loading, container, false)
        state = LoadingState(activity = this)
        state.toStage(stage = LoadingState.Stage.INIT) { toInit(it) }
        return binding!!.root
    }

    private fun toInit(old: LoadingState){
        registerReceiver()
        ba = BluetoothAdapter.getDefaultAdapter()
        if (!ba!!.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, OPEN_BLUETOOTH_SUCCESS)
        }else{
            ee.emit(LoadingMessage(LoadingMessage.Type.INIT, true))
            old.toStage(LoadingState.Stage.SCANNING, this::toScanning)
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
            binding!!.prompt = ""
            mAnimator = Animators.scaleValueAnimator(binding!!.loadingLogo)
            mAnimator!!.start()
        }
    }

    private fun toScanning(old: LoadingState){
        ba!!.startDiscovery()
    }

    init{ee.register("AdapterMessage", this::onScanning) }
    private fun onScanning(message: EventEmitter.Message){
        val msg = message as AdapterMessage
        when(msg.msgType){
            AdapterMessage.Type.STARTED -> {
                binding!!.loadingScreen.setOnClickListener {  }
                binding!!.prompt = "正在扫描盒子"
            }
            AdapterMessage.Type.STOPPED -> {
                state.toStage(LoadingState.Stage.CONNECTING, this::toScanDone)
            }
        }
    }

    init { ee.register("DeviceMessage", this::onFound) }
    private fun onFound(message: EventEmitter.Message) {
        val msg = message as DeviceMessage
        val device = msg.value
        if (msg.msgType == DeviceMessage.Type.FOUND) {
            Log.d("BTSP", String.format("%s: %s %s", msg.msgType.value, device.address, device.name))
            if(device.name == "raspberrypi"){
                state.device = device
                ba!!.cancelDiscovery()
            }
        }
    }

    private fun toScanDone(old: LoadingState){
        try {
            state.device.address
            old.toStage(LoadingState.Stage.CONNECTING, this::toConnecting)
        }catch (e: UninitializedPropertyAccessException){
            old.toStage(LoadingState.Stage.CONNECT_FAILED, this::toConnectFailed)
        }
    }

    private fun toConnecting(old: LoadingState) {
        ee.emit(LoadingMessage(LoadingMessage.Type.CONNECTING, null))
        state.connectTask = ConnectTask(old)
        state.connectTask.execute()
    }

    init{ ee.register("LoadingMessage", this::onConnecting) }
    private fun onConnecting(message: EventEmitter.Message){
        val msg = message as LoadingMessage
        if(msg.msgType == LoadingMessage.Type.CONNECTING){
            binding!!.prompt = "正在连接盒子"
        }
    }

    fun toScanningDev(old: LoadingState){
        Thread{
            val receive = CommandHandler.handleCommand("GET_WIFI_DEV", state.device)
            if(receive == null){
                activity.runOnUiThread {
                    old.toStage(LoadingState.Stage.SCANNING_DEV, this::toScanningDev)
                }
            }else{
                saveDeviceList(receive)
                //TODO there is an error waiting to be resolved here
                activity.runOnUiThread {
                    old.toStage(LoadingState.Stage.SCANNING_CONNECTED, this::toScanningConnected)
                }
            }
        }.start()
    }

    private fun toScanningConnected(old: LoadingState){
        Thread{
            val receive = CommandHandler.handleCommand("GET_WIFI_CONNECTION", state.device)
            if(receive == null){
                activity.runOnUiThread {
                    old.toStage(LoadingState.Stage.SCANNING_CONNECTED, this::toScanningConnected)
                }
            }else{
                old.cwl = transKw(receive)
                activity.runOnUiThread {
                    old.toStage(LoadingState.Stage.SCAN_CONNECTED_DONE, this::toScanConnectedDone)
                }
            }
        }.start()
    }

    private fun toScanConnectedDone(old: LoadingState){
        activity.unregisterReceiver(mainReceiver)
        val map = HashMap<String, Any>()
        map["fragmentID"] = R.id.container
        map["fragment"] = ConnectedFragment()
        ee.emit(LoadingMessage(LoadingMessage.Type.CONNECTED, null))
        ee.emit(IntermediateMessage(IntermediateMessage.Type.SWITCH_VIEW, map))
        ee.emit(WifiListMessage(WifiListMessage.Type.DEFAULT, old))
    }

    fun toConnectFailed(old: LoadingState){
        //TODO toStage
        ee.emit(LoadingMessage(LoadingMessage.Type.CONNECTFAILED, null))
    }

    init { ee.register("LoadingMessage", this::onConnectFailed) }
    private fun onConnectFailed(message: EventEmitter.Message){
        val msg = message as LoadingMessage
        if(msg.msgType == LoadingMessage.Type.CONNECTFAILED){
            binding!!.prompt = "连接失败了, 点击屏幕重试"
            mAnimator!!.end()
            mAnimator!!.removeAllListeners()
            binding!!.loadingLogo.scaleX = 1f
            binding!!.loadingLogo.scaleY = 1f
            binding!!.loadingScreen.setOnClickListener {
                ee.emit(LoadingMessage(LoadingMessage.Type.INIT, true))
                state.toStage(LoadingState.Stage.SCANNING, this::toScanning)
            }
        }
    }

    private fun registerReceiver() {
        val intentFilter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND)
        activity.registerReceiver(mainReceiver, intentFilter)
    }

    private fun saveDeviceList(re: JSONObject){
        val devl:ArrayList<String> = ObservableArrayList()
        val json = JSONObject(re.toString())
        val devs = json.getJSONObject("reply")
        devs.keys().forEach { e->
            devl.add(e)
        }
        state.devl = devl
    }

    private fun transKw(data: JSONObject): MutableList<ConnectedWifi>{
        val ja = data.getJSONObject("reply")
        Log.d("BTSP", "CONNECTED " + ja.toString())
        val list = ObservableArrayList<ConnectedWifi>()
        val kwl: MutableList<ConnectedWifi> = ObservableArrayList<ConnectedWifi>()
        ja.keys().forEach { e ->
            val known = ConnectedWifi(ja.getJSONObject(e).getString("name"),
                    ja.getJSONObject(e).getString("device"), "加密的", e)
            if(ja.getJSONObject(e).getString("device") != "--"){
                Log.d("BTSP", "DEVICE_SCANNING" + ja.getJSONObject(e).getString("device"))
                list.add(known)
            }
            Log.d("BTSP", "KNOW WIFI " + known.name)
            kwl.add(known)
        }
        state.kwl = kwl
        return list
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== -1){
            ee.emit(LoadingMessage(LoadingMessage.Type.INIT, true))
            state.toStage(LoadingState.Stage.SCANNING, this::toScanning)
        }else{
            binding!!.prompt = "请打开蓝牙"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ee.unregister(this::onInit)
        ee.unregister(this::onFound)
        ee.unregister(this::onScanning)
        ee.unregister(this::onConnecting)
        ee.unregister(this::onConnectFailed)
    }
}