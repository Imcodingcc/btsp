package cn.leither.btsp.fragment

import android.app.Dialog
import android.app.Fragment
import android.content.Intent
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
import cn.leither.btsp.adapter.ConnectedWifiAdapter
import cn.leither.btsp.databinding.FragmentConnectedListBinding
import cn.leither.btsp.entity.ConnectedWifi
import cn.leither.btsp.handlemsg.EventEmitter
import cn.leither.btsp.handlemsg.WifiListMessage
import cn.leither.btsp.state.LoadingState
import cn.leither.btsp.state.WifiListState
import cn.leither.btsp.utile.CommandHandler
import org.json.JSONObject
import java.io.Serializable

class ConnectedFragment internal constructor(): Fragment(){
    private val ee = EventEmitter.default
    lateinit var binding: FragmentConnectedListBinding
    private var connectedWifiAdapter: ConnectedWifiAdapter? = null
    private var state: WifiListState = WifiListState(activity = this)
    lateinit var loading_dialog: Dialog
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_connected_list, container, false)
        state.toStage(WifiListState.Stage.INIT, this::toInit)
        loading_dialog = Dialog(activity, R.style.Dialog_Fullscreen)
        loading_dialog.setContentView(R.layout.dialog_fullscreen_loading)
        loading_dialog.setOnKeyListener({_, _, _-> true })
        return binding.root
    }

    init { ee.register("WifiListMessage", this::onDefault) }
    private fun onDefault(message: EventEmitter.Message){
        val msg = message as WifiListMessage
        if(msg.msgType == WifiListMessage.Type.DEFAULT){
            val oldState = message.value as LoadingState
            state.cwl = oldState.cwl
            state.kwl = oldState.kwl
            state.devl = oldState.devl
            state.device = oldState.device
        }
    }

    init { ee.register("WifiListMessage", this::onBack) }
    private fun onBack(message: EventEmitter.Message){
        val msg = message as WifiListMessage
        if(msg.msgType == WifiListMessage.Type.BACK){
            if(msg.value != ""){
                state.toStage(WifiListState.Stage.SCANNING_CONNECTED, this::toScanningConnected)
            }
        }
    }

    private fun toInit(old: WifiListState){
        ee.emit(WifiListMessage(WifiListMessage.Type.INIT, null))
    }

    init { ee.register("WifiListMessage", this::onInit) }
    @RequiresApi(Build.VERSION_CODES.M)
    private fun onInit(message: EventEmitter.Message){
        val msg = message as WifiListMessage
        if(msg.msgType == WifiListMessage.Type.INIT){
            connectedWifiAdapter = ConnectedWifiAdapter(activity, state.cwl, R.layout.adapter_know_wifi_list, BR.know)
            connectedWifiAdapter!!.state = state
            binding.connectedWifiListAdapter = connectedWifiAdapter
            binding.addMoreConnection.setOnClickListener({
                val intent = Intent("connect")
                intent.putExtra("cwl", state.cwl as Serializable)
                intent.putExtra("kwl", state.kwl as Serializable)
                intent.putStringArrayListExtra("devl", state.devl)
                intent.putExtra("device", state.device )
                activity.startActivity(intent)
            })
        }
    }

    private fun toScanningConnected(old: WifiListState){
        ee.emit(WifiListMessage(WifiListMessage.Type.SCANNING_CONNECTED, null))
        Thread{
            val receive = CommandHandler.handleCommand("GET_WIFI_CONNECTION", state.device)
            if(receive == null){
                activity.runOnUiThread {
                    old.toStage(WifiListState.Stage.SCANNING_CONNECTED, this::toScanningConnected)
                }
            }else{
                old.cwl = transKw(receive)
                activity.runOnUiThread {
                    old.toStage(WifiListState.Stage.SCAN_CONNECTED_DONE, this::toScanConnectedDone)
                }
            }
        }.start()
    }

    init { ee.register("WifiListMessage", this::onScanningConnected) }
    private fun onScanningConnected(message: EventEmitter.Message){
        val msg = message as WifiListMessage
        if(msg.msgType == WifiListMessage.Type.SCANNING_CONNECTED){
            Toast.makeText(activity, "正在获取连接信息", Toast.LENGTH_SHORT).show()
            loading_dialog.dismiss()
            loading_dialog.show()
        }
    }

    private fun toScanConnectedDone(old: WifiListState){
        ee.emit(WifiListMessage(WifiListMessage.Type.SCAN_CONNECTED_DONE, null))
    }

    init { ee.register("WifiListMessage", this::onScanConnectedDone) }
    private fun onScanConnectedDone(message: EventEmitter.Message){
        val msg = message as WifiListMessage
        if(msg.msgType == WifiListMessage.Type.SCAN_CONNECTED_DONE){
            connectedWifiAdapter = ConnectedWifiAdapter(activity, state.cwl, R.layout.adapter_know_wifi_list, BR.know)
            connectedWifiAdapter!!.state = state
            binding.connectedWifiListAdapter = connectedWifiAdapter
            loading_dialog.dismiss()
        }
    }

    private fun transKw(data: JSONObject): MutableList<ConnectedWifi>{
        val ja = data.getJSONObject("reply")
        val list = ObservableArrayList<ConnectedWifi>()
        val kwl: MutableList<ConnectedWifi> = ObservableArrayList<ConnectedWifi>()
        ja.keys().forEach { e ->
            val known = ConnectedWifi(ja.getJSONObject(e).getString("name"), ja.getJSONObject(e).getString("device"), "加密的", e)
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
}

