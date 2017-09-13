package cn.leither.btsp.fragment

import android.app.Fragment
import android.databinding.DataBindingUtil
import android.databinding.ObservableArrayList
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import cn.leither.btsp.*
import cn.leither.btsp.adapter.ConnectedWifiAdapter
import cn.leither.btsp.adapter.WifiListAdapter
import cn.leither.btsp.command.*
import cn.leither.btsp.databinding.FragmentLoadingBinding
import cn.leither.btsp.databinding.FragmentWifiListBinding
import cn.leither.btsp.databinding.WeightChooseWifiListBinding
import cn.leither.btsp.entity.KnownWifi
import cn.leither.btsp.entity.Wi
import cn.leither.btsp.handlemsg.EventEmitter
import cn.leither.btsp.handlemsg.WifiListMessage
import cn.leither.btsp.state.LoadingState
import cn.leither.btsp.state.WifiListState
import cn.leither.btsp.task.ScanWifiListTask
import cn.leither.btsp.utile.CommandHandler
import cn.leither.btsp.utile.Const
import org.json.JSONObject
import java.io.IOException

class WifiListFragment internal constructor(): Fragment(){

    private var binding: FragmentWifiListBinding? = null
    private var choose_wifi_binding: WeightChooseWifiListBinding? = null
    private var loading_binding: FragmentLoadingBinding? = null
    private var state: WifiListState = WifiListState(activity = this)
    private val ee = EventEmitter.default
    private var wifiListAdapter: WifiListAdapter? =null
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_wifi_list, container, false)
        choose_wifi_binding = DataBindingUtil.inflate(inflater, R.layout.weight_choose_wifi_list, container, false)
        loading_binding = DataBindingUtil.inflate(inflater, R.layout.fragment_loading, container, false)
        state.toStage(WifiListState.Stage.INIT, this::toInit)
        return binding!!.root
    }

    init { ee.register("WifiListMessage", this::onDefault) }
    private fun onDefault(message: EventEmitter.Message){
        val msg = message as WifiListMessage
        if(msg.msgType == WifiListMessage.Type.DEFAULT){
            val oldState = message.value as LoadingState
            state.device = oldState.devices.iterator().next()
        }
    }

    private fun toInit(old: WifiListState){
        ee.emit(WifiListMessage(WifiListMessage.Type.INIT, null))
        old.toStage(WifiListState.Stage.SCANNING_DEV, this::toScanningDev)
    }

    init { ee.register("WifiListMessage", this::onInit) }
    private fun onInit(message: EventEmitter.Message){
        val msg = message as WifiListMessage
        if(msg.msgType == WifiListMessage.Type.INIT){
        }
    }

    private fun toScanningDev(old: WifiListState){
        ee.emit(WifiListMessage(WifiListMessage.Type.SCANNING_DEV, null))
        Thread{
            val receive = CommandHandler.handleCommand("GET_WIFI_DEV", state.device)
            if(receive == null){
                activity.runOnUiThread {
                    old.toStage(WifiListState.Stage.SCANNING_DEV, this::toScanningDev)
                }
            }else{
                saveDeviceList(receive)
                activity.runOnUiThread {
                    old.toStage(WifiListState.Stage.SCANNING_CONNECTED, this::toScanningConnected)
                }
            }
        }.start()
    }

    init { ee.register("WifiListMessage", this::onScanningDev) }
    private fun onScanningDev(message: EventEmitter.Message){
        val msg = message as WifiListMessage
        if(msg.msgType == WifiListMessage.Type.SCANNING_DEV){
            binding!!.wlContainer.gravity = Gravity.CENTER
            binding!!.wlContainer.removeAllViews()
            binding!!.wlContainer.addView(loading_binding!!.root)
            loading_binding!!.textPrompt.paint.isFakeBoldText = true
            loading_binding!!.prompt = "正在扫描网卡"
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
            loading_binding!!.prompt = "正在扫描wifi列表"
        }
    }

    private fun toScanConnectedDone(old: WifiListState){
        ee.emit(WifiListMessage(WifiListMessage.Type.SCAN_CONNECTED_DONE, null))
        old.toStage(WifiListState.Stage.SCANNING_WIFI_LIST, this::toScanningWifiList)
    }

    init { ee.register("WifiListMessage", this::onScanDone) }
    private fun onScanDone(message: EventEmitter.Message){
        val msg = message as WifiListMessage
        if(msg.msgType == WifiListMessage.Type.SCAN_CONNECTED_DONE){
            binding!!.wlContainer.gravity = Gravity.TOP
            binding!!.wlContainer.removeAllViews()
            binding!!.wlContainer.addView(choose_wifi_binding!!.root)
            val cp = choose_wifi_binding!!.chooseWifiTitle.paint
            cp.isFakeBoldText = true
            val knownWifiListAdapter = ConnectedWifiAdapter(activity, state.cwl, R.layout.adapter_know_wifi_list, BR.know)
            knownWifiListAdapter.state = state
            choose_wifi_binding!!.connectedWifiListAdapter = knownWifiListAdapter
        }
    }

    private fun toScanningWifiList(old: WifiListState){
        state.scanWifiListTask = ScanWifiListTask(old)
        state.scanWifiListTask.execute()
    }

    init { ee.register("WifiListMessage", this::onScanningWifiList) }
    private fun onScanningWifiList(message: EventEmitter.Message){
        val msg = message as WifiListMessage
        if(msg.msgType == WifiListMessage.Type.SCANNING_WIFI_LIST){
            choose_wifi_binding!!.chooseWifiSearch.smoothToShow()
            Log.d("BTSP", "start scan wifi list")
        }
    }

    init { ee.register("WifiListMessage", this::onScanningWifiListDiscover) }
    private fun onScanningWifiListDiscover(message: EventEmitter.Message){
        val msg = message as WifiListMessage
        if(msg.msgType == WifiListMessage.Type.SCAN_WIFI_LIST_DISCOVER){
            choose_wifi_binding!!.chooseWifiSearch.smoothToHide()
            if(wifiListAdapter == null){
                wifiListAdapter = WifiListAdapter(activity, state.wl, R.layout.adapter_wifi_list, BR.ssid)
                wifiListAdapter!!.state = state
                choose_wifi_binding!!.nearWifiListAdapter = wifiListAdapter
                choose_wifi_binding!!.nearWifiList.dividerHeight = 0
            }
            //TODO I can not set Bidirectional binding
            wifiListAdapter!!.setItems(state.wl)
            wifiListAdapter!!.notifyDataSetChanged()
        }
    }

    fun toCancelScanWifiForDisconnect(old: WifiListState, param: Any?){
        ee.emit(WifiListMessage(WifiListMessage.Type.CANCEL_SCAN_WIFI, null))
        old.scanWifiListTask.cancelForDisconnect(true, param)
    }

    fun toCancelScanWifiForActivateConnect(old: WifiListState, param: Any?){
        ee.emit(WifiListMessage(WifiListMessage.Type.CANCEL_SCAN_WIFI, null))
        old.scanWifiListTask.cancelForActivateConnect(true, param)
    }

    fun toCancelScanWifiForCreateConnConnect(old: WifiListState, param: Any?){
        ee.emit(WifiListMessage(WifiListMessage.Type.CANCEL_SCAN_WIFI, null))
        old.scanWifiListTask.cancelForCreateConnect(true, param)
    }

    init { ee.register("WifiListMessage", this::onCancelScanWifi) }
    private fun onCancelScanWifi(message: EventEmitter.Message){
        val msg = message as WifiListMessage
        if(msg.msgType == WifiListMessage.Type.CANCEL_SCAN_WIFI){
            choose_wifi_binding!!.chooseWifiSearch.smoothToHide()
            choose_wifi_binding!!.nearWifiList.isClickable = false
            choose_wifi_binding!!.connectedWifiList.isClickable = false
        }
    }

    fun toDisConnectWifi(old: WifiListState, param: Any?){
        Thread{
            val receive = CommandHandler.handleCommand("DISCONNECT_WIFI", state.device, JSONObject(param as MutableMap<*, *>))
            if(receive == null){
                activity.runOnUiThread {
                    old.toStage(WifiListState.Stage.DISCONNECT_WIFI, this::toDisConnectWifi, param)
                }
            }else{
                activity.runOnUiThread {
                    old.toStage(WifiListState.Stage.SCANNING_CONNECTED, this::toScanningConnected)
                }
            }
        }.start()
    }

    private fun toScanConnectedFailed(old: WifiListState){
        TODO()
    }

    fun onScanningConnectedFailed(message: EventEmitter.Message){
        TODO()
    }

    fun toActivateWifi(old: WifiListState, param: Any?){
        Thread{
            val receive = CommandHandler.handleCommand("ACTIVATE_WIFI_CONNECTION", state.device, JSONObject(param as MutableMap<*, *>))
            if(receive == null){
                activity.runOnUiThread {
                    old.toStage(WifiListState.Stage.ACTIVATE_WIFI, this::toActivateWifi, param)
                }
            }else{
                activity.runOnUiThread {
                    old.toStage(WifiListState.Stage.SCANNING_CONNECTED, this::toScanningConnected)
                }
            }
        }.start()
    }

    fun toCreateWifiConn(old: WifiListState, param: Any?){
        Thread{
            val receive = CommandHandler.handleCommand("ACTIVATE_WIFI_CONNECTION", state.device, JSONObject(param as MutableMap<*, *>))
            if(receive == null){
                activity.runOnUiThread {
                    old.toStage(WifiListState.Stage.CREATED_WIFI_CONNECTION, this::toCreateWifiConn, param)
                }
            }else{
                when(connectWifiMsg(receive)){
                    "successfully"->
                        activity.runOnUiThread {
                            Toast.makeText(activity, "已连接", Toast.LENGTH_SHORT).show()
                            old.toStage(WifiListState.Stage.SCANNING_CONNECTED, this::toScanningConnected)
                        }
                    "passwordErr"->
                        activity.runOnUiThread {
                            Toast.makeText(activity, "密码错误", Toast.LENGTH_SHORT).show()
                            old.toStage(WifiListState.Stage.SCANNING_CONNECTED, this::toScanningConnected)
                        }
                    "rangeErr"->
                        activity.runOnUiThread {
                            Toast.makeText(activity, "超出范围", Toast.LENGTH_SHORT).show()
                            old.toStage(WifiListState.Stage.SCANNING_CONNECTED, this::toScanningConnected)
                        }
                    "timeErr"->{
                        Thread.sleep(3000)
                        activity.runOnUiThread {
                            Toast.makeText(activity, "连接超时", Toast.LENGTH_SHORT).show()
                            old.toStage(WifiListState.Stage.SCANNING_CONNECTED, this::toScanningConnected)
                        }
                    }
                    "unknownErr"->
                        activity.runOnUiThread {
                            Toast.makeText(activity, "连接失败", Toast.LENGTH_SHORT).show()
                            old.toStage(WifiListState.Stage.SCANNING_CONNECTED, this::toScanningConnected)
                        }
                }
            }
        }.start()
    }

    fun toDeleteConnection(old: WifiListState, param: Any?){
        Thread{
            val receive = CommandHandler.handleCommand("DELETE_WIFI_CONNECTION", state.device, JSONObject(param as MutableMap<*, *>))
            if(receive == null){
                activity.runOnUiThread {
                    old.toStage(WifiListState.Stage.DELETE_WIFI_CONNECTION, this::toScanningConnected)
                }
            }else{
                Log.d("BTSP", "DELETE_SUCCESS")
            }
        }.start()

    }

    private fun transKw(data: JSONObject): MutableList<KnownWifi>{
        val ja = data.getJSONObject("reply")
        val list = ObservableArrayList<KnownWifi>()
        val kwl: MutableList<KnownWifi> = ObservableArrayList<KnownWifi>()
        ja.keys().forEach { e ->
            val known = KnownWifi(ja.getJSONObject(e).getString("name"), ja.getJSONObject(e).getString("device"), "加密的", e)
            if(ja.getJSONObject(e).getString("device") != "--"){
                Log.d("BTSP", "DEVICE_SCANNING" + ja.getJSONObject(e).getString("device"))
                list.add(known)
            }
            kwl.add(known)
        }
        state.kwl = kwl
        return list
    }

    private fun saveDeviceList(re: JSONObject){
        val devl:MutableList<String> = ObservableArrayList()
        val json = JSONObject(re.toString())
        val devs = json.getJSONObject("reply")
        devs.keys().forEach { e->
            devl.add(e)
        }
        state.devl = devl
    }

    private fun connectWifiMsg(re: JSONObject): String{
        val reply = re.getJSONObject("reply").toString()
        return when {
            reply.indexOf("successfully", 0, false) != -1 -> "successfully"
            reply.indexOf("provided", 0, false) != -1 -> "passwordErr"
            reply.indexOf("No network with", 0, false) != -1 -> "rangeErr"
            reply.indexOf("Timeout", 0, false) != -1 -> "timeErr"
            else -> "unknownErr"
        }
    }
}

