package cn.leither.btsp

import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.DialogInterface
import android.content.IntentFilter
import android.databinding.DataBindingUtil
import android.databinding.ObservableArrayList
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.AdapterView
import cn.leither.btsp.databinding.ActivityMainBinding
import org.json.JSONObject
import java.util.concurrent.CopyOnWriteArraySet


class MainActivity : AppCompatActivity() {
    private lateinit var state: MainState
    private var mainReceiver :BtspReceiver? =null
    private var binding: ActivityMainBinding? = null
    private var wifiListAdapter: CommonAdapter<Wi>? = null
    private val ee = EventEmitter.default
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("BTSP", "onCreate")
        binding = DataBindingUtil.setContentView (this, R.layout.activity_main);
        state = MainState(activity = this)
        state.devices = CopyOnWriteArraySet<BluetoothDevice>()
        registerReceiver()
        state.toStage(MainState.Stage.INIT, this::toInit)
        mainReceiver = BtspReceiver()
    }


    init{ ee.register("AdapterMessage", this::onDiscover) }
    private fun onDiscover(message: EventEmitter.Message) {
        val msg = message as AdapterMessage
        Log.d("BTSP", "onDiscover "+ msg.msgType.value)

        when(msg.msgType) {
            AdapterMessage.Type.STARTED -> Log.d("BTSP", "started")
            AdapterMessage.Type.STOPPED -> {
                state.toStage(MainState.Stage.CONNECTING, this::toConnect)
            }
        }
    }

    init { ee.register("DeviceMessage", this::onFound) }
    private fun onFound(message: EventEmitter.Message) {
        val msg = message as DeviceMessage
        val device = msg.value

        if (msg.msgType == DeviceMessage.Type.FOUND) {
            Log.d("BTSP", String.format("%s: %s %s", msg.msgType.value, device.address, device.name))
            state.devices.add(device)
        }
    }

    fun toInit(old: MainState) {
        old.ba= BluetoothAdapter.getDefaultAdapter()
        old.toStage(MainState.Stage.SCANNING, {
            it.ba.cancelDiscovery()
            it.ba.startDiscovery()
        })
    }

    private fun toConnect(old: MainState) {
        //state.connectTask = ConnectTask(state)
        state.connectTask.execute()
    }

    fun toConnected(old: MainState) {
        toShowWifiConnected()
    }

    private fun toShowWifiList(data: JSONObject){
        val input = state.sockets[0].inputStream
        val output = state.sockets[0].outputStream
        val list:MutableList<SsId> = transWl(data.getJSONObject("reply"))
        wifiListAdapter = CommonAdapter(this, list, R.layout.adapter_wifi_list, BR.ssid)
        binding!!.adapter= wifiListAdapter
        binding!!.wifiListView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            state.connectedSocket.connect()
            val wi = WiCommand(input, output)
            wi.send()
            val re = wi.recv()
            if(re != null){
                createWiDevDialog(re, false, false, list[i].name)
            }
        }
    }

    private fun toShowWifiConnected(){
        val input = state.sockets[0].inputStream
        val output = state.sockets[0].outputStream
        val gw = GetWifiConnection(input, output)
        gw.send()
        val re = gw.recv()
       if(re != null){
           Log.d("BTSP",  "re " + re.toString())
           val list:MutableList<KnownWifi> = transKw(re.getJSONObject("reply"))
           val knownWifiListAdapter: CommonAdapter<KnownWifi> = CommonAdapter(this, list, R.layout.adapter_know_wifi_list, BR.know)
           binding!!.knownWifiListAdapter = knownWifiListAdapter
           binding!!.knownWifiListView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
               state.connectedSocket.connect()
               val wi = WiCommand(input, output)
               wi.send()
               val re = wi.recv()
               if(re != null){
                   val e = list[i].state.length > 2
                   createWiDevDialog(re, true, e, "")
               }
           }
        }
        val sc = ScanCommand(input, output)
        sc.send()
        val rs = sc.recv()
        if(rs != null){
            toShowWifiList(rs)
        }
    }

    private fun transKw(data: JSONObject): MutableList<KnownWifi>{
        var list: MutableList<KnownWifi> = ObservableArrayList()
        data.keys().forEach { e ->
            list.add(KnownWifi(data.getJSONObject(e).getString("name"), data.getJSONObject(e).getString("device"),  "加密的"))
        }
        return list
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mainReceiver)
        ee.unregister( this::onDiscover)
        ee.unregister(this::onFound)
    }

    private fun transWi(data: JSONObject): MutableList<Wi>{
        var list: MutableList<Wi> = ObservableArrayList()
        data.keys().forEach { e ->
            list.add(Wi(e,  data.getString(e)))
        }
        return list
    }

    private fun transWl(data: JSONObject): MutableList<SsId>{
        var list: MutableList<SsId> = ObservableArrayList()
        data.keys().forEach { e ->
            list.add(SsId(e,  data.getJSONObject(e).getString("signal"), "加密的"))
        }
        return list
    }

    private fun registerReceiver() {
        val intentFilter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND)
        this.registerReceiver(mainReceiver, intentFilter)
    }

    private fun createWiDevDialog(data: JSONObject, isKnown: Boolean, isConnected: Boolean, ssid: String){
        val input = state.sockets[0].inputStream
        val output = state.sockets[0].outputStream
        val builder = AlertDialog.Builder(this)
        builder.setTitle("interface list")
        val list:MutableList<Wi> = transWi(data.getJSONObject("reply"))
        val listAdapter:CommonAdapter<Wi> = CommonAdapter(this, list, R.layout.adapter_wifi_dev, BR.wi)
        builder.setAdapter(listAdapter, DialogInterface.OnClickListener {
            dialogInterface, i ->
            if(isKnown && isConnected){
                // 断开网络
            }else if(isKnown && !isConnected){
                // 忘记网络 || 连接到网络
            }else{
                createEnPasswordDialog(ssid, list[i].dev)
            }
        })
        builder.create().show()
    }

    private fun createEnPasswordDialog(ssid: String, iface: String ){
        val input = state.sockets[0].inputStream
        val output = state.sockets[0].outputStream
        val dialog = EnterPasswordDialogFragment(state, ssid, iface, input, output)
        dialog.show(fragmentManager, "password")
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
