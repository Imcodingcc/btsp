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
import android.widget.ListView
import android.widget.Toast
import cn.leither.btsp.*
import cn.leither.btsp.adapter.ConnectedWifiAdapter
import cn.leither.btsp.adapter.WifiListAdapter
import cn.leither.btsp.command.*
import cn.leither.btsp.databinding.FragmentLoadingBinding
import cn.leither.btsp.databinding.FragmentWifiListBinding
import cn.leither.btsp.databinding.WeightChooseWifiListBinding
import cn.leither.btsp.entity.KnownWifi
import cn.leither.btsp.entity.SsId
import cn.leither.btsp.handlemsg.EventEmitter
import cn.leither.btsp.handlemsg.WifiListMessage
import cn.leither.btsp.state.LoadingState
import cn.leither.btsp.state.WifiListState
import cn.leither.btsp.task.ScanWifiListTask
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by lvqiang on 17-8-29.
 */
class WifiListFragment internal constructor(): Fragment(){

    private var binding: FragmentWifiListBinding? = null
    private var choose_wifi_binding: WeightChooseWifiListBinding? = null
    private var loading_binding: FragmentLoadingBinding? = null
    private var state: WifiListState = WifiListState(activity = this)
    private val ee = EventEmitter.default
    var input: InputStream? = null
    var output: OutputStream? = null
    var wifiListAdapter: WifiListAdapter<SsId>? =null
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        input = state.sockets[0].inputStream
        output = state.sockets[0].outputStream
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
            var oldState = message.value as LoadingState
            state.ba = oldState.ba
            state.devices = oldState.devices
            state.sockets = oldState.sockets
            state.connectedSocket = oldState.connectedSocket
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
        val gd = GetDevCommand(input!!, output!!)
        gd.send()
        val re = gd.recv()
        if(re != null){
            val devl:MutableList<String> = ObservableArrayList()
            val json = JSONObject(re.toString())
            val devs = json.getJSONObject("reply")
            devs.keys().forEach { e->
                devl.add(e)
            }
            state.devl = devl
        }
        old.toStage(WifiListState.Stage.SCANNING_CONNECTED, this::toScanningConnected, WifiListState.Stage.SCANNING_DEV)
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

    private fun toScanningConnected(old: WifiListState, param: Any?){
        Thread{
            try {
                old.connectedSocket.connect()
                activity.runOnUiThread {
                    ee.emit(WifiListMessage(WifiListMessage.Type.SCANNING_CONNECTED, null))
                }
                val gw = GetWifiConnection(input!!, output!!)
                gw.send()
                val re = gw.recv()
                if(re != null){
                    old.cwl = transKw(re)
                    Log.d("BTSP", "cwl" + re.toString())
                    activity.runOnUiThread {
                        old.toStage(WifiListState.Stage.SCAN_CONNECTED_DONE, this::toScanConnectedDone)
                    }
                }else{
                    activity.runOnUiThread {
                        old.toStage(WifiListState.Stage.SCANNING_CONNECTED, this::toScanningConnected, WifiListState.Stage.SCANNING_CONNECTED)
                    }
                }
            }catch (e: Exception){
                Log.d("BTSP", "scan catch" + e.message)
                Thread.sleep(3000)
                activity.runOnUiThread {
                    old.toStage(WifiListState.Stage.SCANNING_CONNECTED, this::toScanningConnected, WifiListState.Stage.SCANNING_CONNECTED)
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
            val knownWifiListAdapter: ConnectedWifiAdapter<KnownWifi> = ConnectedWifiAdapter(activity, state.cwl, R.layout.adapter_know_wifi_list, BR.know)
            knownWifiListAdapter.state = state
            choose_wifi_binding!!.connectedWifiListAdapter = knownWifiListAdapter
        }
    }

    private fun toScanningWifiList(old: WifiListState){
        state.scanWifiListTask = ScanWifiListTask(state)
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
            Log.d("BTSP", "cancel_scanning")
        }
    }

    fun toDisConnectWifi(old: WifiListState, param: Any?){
        Thread{
            try {
                old.connectedSocket.connect()
                val dc = DisConnectWifi(input!!, output!!)
                dc.iface = JSONObject(param as MutableMap<Any?, Any?>)
                dc.send()
                val re = dc.recv()
                if(re != null){
                    activity.runOnUiThread {
                        old.toStage(WifiListState.Stage.SCANNING_CONNECTED, this::toScanningConnected, WifiListState.Stage.DISCONNECT_WIFI)
                    }
                }
            }catch (e:Exception){
                Log.d("BTSP", "eeee" + e.message)
                Thread.sleep(3000)
                activity.runOnUiThread {
                    old.toStage(WifiListState.Stage.DISCONNECT_WIFI, this::toDisConnectWifi, param)
                }
            }
        }.start()
    }

    private fun toScanConnectedFailed(old: WifiListState){
        Log.d("BTSP", "失败了")

    }

    fun onScanningConnectedFailed(message: EventEmitter.Message){

    }

    fun toActivateWifi(old: WifiListState, param: Any?){
        Thread{
            try {
                old.connectedSocket.connect()
                val af = ActivateWifiConnection(input!!, output!!)
                param as MutableMap<String, String>
                af.iface = JSONObject(param)
                af.send()
                val re = af.recv()
                if(re != null){
                    activity.runOnUiThread {
                        old.toStage(WifiListState.Stage.SCANNING_CONNECTED, this::toScanningConnected, WifiListState.Stage.DISCONNECT_WIFI)
                    }
                }
            }catch (e:Exception){
                Thread.sleep(3000)
                activity.runOnUiThread {
                    old.toStage(WifiListState.Stage.ACTIVATE_WIFI, this::toActivateWifi, param)
                }
            }

        }.start()
    }

    fun toCreateWifiConn(old: WifiListState, param: Any?){
        Thread{
            try {
                old.connectedSocket.connect()
                val cw = CreateWifiConnection(input!!, output!!)
                param as MutableMap<String, String>
                cw.param = JSONObject(param)
                cw.send()
                val re = cw.recv()
                when(connectWifiMsg(re!!)){
                    "successfully"->
                        activity.runOnUiThread {
                            Toast.makeText(activity, "已连接", Toast.LENGTH_SHORT).show()
                            old.toStage(WifiListState.Stage.SCANNING_CONNECTED, this::toScanningConnected, WifiListState.Stage.SCANNING_CONNECTED)
                        }
                    "passwordErr"->
                         activity.runOnUiThread {
                             Toast.makeText(activity, "密码错误", Toast.LENGTH_SHORT).show()
                             old.toStage(WifiListState.Stage.SCANNING_CONNECTED, this::toScanningConnected, WifiListState.Stage.SCANNING_CONNECTED)
                         }
                    "rangeErr"->
                        activity.runOnUiThread {
                            Toast.makeText(activity, "超出范围", Toast.LENGTH_SHORT).show()
                            old.toStage(WifiListState.Stage.SCANNING_CONNECTED, this::toScanningConnected, WifiListState.Stage.SCANNING_CONNECTED)
                        }
                    "timeErr"->{
                        Thread.sleep(3000)
                        activity.runOnUiThread {
                            Toast.makeText(activity, "连接超时", Toast.LENGTH_SHORT).show()
                            old.toStage(WifiListState.Stage.SCANNING_CONNECTED, this::toScanningConnected, WifiListState.Stage.SCANNING_CONNECTED)
                        }
                    }
                    "unknownErr"->
                            activity.runOnUiThread {
                                Toast.makeText(activity, "连接失败", Toast.LENGTH_SHORT).show()
                                old.toStage(WifiListState.Stage.SCANNING_CONNECTED, this::toScanningConnected, WifiListState.Stage.SCANNING_CONNECTED)
                            }

                }
            }catch (e:Exception){
                Thread.sleep(3000)
                activity.runOnUiThread {
                    old.toStage(WifiListState.Stage.CREATED_WIFI_CONNECTION, this::toCreateWifiConn, param)
                }
            }

        }.start()
    }

    private fun transKw(data: JSONObject): MutableList<KnownWifi>{
        val ja = data.getJSONObject("reply")
        var list: MutableList<KnownWifi> = ObservableArrayList<KnownWifi>()
        var kwl: MutableList<KnownWifi> = ObservableArrayList<KnownWifi>()
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

    private fun connectWifiMsg(re: JSONObject): String{
        val reply = re.getJSONObject("reply").toString()
        if(reply.indexOf("successfully", 0, false) != -1){
            return "successfully"
        }else if(reply.indexOf("provided", 0, false) != -1){
            return "passwordErr"
        }else if(reply.indexOf("No network with", 0, false) != -1){
            return "rangeErr"
        }else if(reply.indexOf("Timeout", 0, false) != -1){
            return "timeErr"
        }
        return "unknownErr"
    }

    private fun setListViewHeight(listView: ListView){
        val la = listView.adapter ?: return
        var totalHeight = 0
        la.count
        for(i in 0 until la.count){
            val listItem = la.getView(i, null, listView)
            listItem.measure(0, 0)
            totalHeight += listItem.measuredHeight
        }
        val params = listView.layoutParams
        params.height = totalHeight + (listView.dividerHeight * (la.count -1))
        listView.layoutParams = params
    }
}

