package cn.leither.btsp.fragment

import android.annotation.SuppressLint
import android.app.DialogFragment
import android.bluetooth.BluetoothDevice
import android.databinding.DataBindingUtil
import android.databinding.ObservableArrayList
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import cn.leither.btsp.BR
import cn.leither.btsp.R
import cn.leither.btsp.adapter.WifiListAdapter
import cn.leither.btsp.databinding.WeightWifiListBinding
import cn.leither.btsp.entity.SearchableWifi
import cn.leither.btsp.handlemsg.ConnectMessage
import cn.leither.btsp.handlemsg.EventEmitter
import cn.leither.btsp.state.ConnectState
import cn.leither.btsp.utile.CommandHandler
import com.wang.avi.AVLoadingIndicatorView
import org.json.JSONObject

@SuppressLint("ValidFragment")
class WifiListDialogFragment @SuppressLint("ValidFragment")
    constructor (val device: BluetoothDevice, val state: ConnectState) :
        DialogFragment(){ lateinit var binding: WeightWifiListBinding
    private val ee = EventEmitter.default
    private lateinit var wifiListAdapter: WifiListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val style = STYLE_NO_TITLE
        val theme = 0
        setStyle(style, theme)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.weight_wifi_list, container, false)
        isCancelable = false
        state.toStage(ConnectState.Stage.SCAN_WIFI_LIST, this::toScanWifiList)
        return binding.root!!
    }

    init { ee.register("ConnectMessage", this::onClose) }
    private fun onClose(message: EventEmitter.Message){
        val msg = message as ConnectMessage
        if(msg.msgType == ConnectMessage.Type.WIFI_LIST_DIALOG_CLOSE){
            ee.emit(ConnectMessage(ConnectMessage.Type.WIFI_NAME, msg.value))
            dismiss()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun toScanWifiList(old: ConnectState){
        ee.emit(ConnectMessage(ConnectMessage.Type.SCAN_WIFI_LIST, null))
        Thread{
            val receive = CommandHandler.handleCommand("SCAN_WIFI", device)
            if(receive == null){
                activity.runOnUiThread {
                    ee.emit(ConnectMessage(ConnectMessage.Type.SCAN_WIFI_LIST_FAILED, null))
                }
            }else{
                Log.d("BTSP", "receive " + receive)
                activity.runOnUiThread {
                    state.wl = transWl(receive)
                    ee.emit(ConnectMessage(ConnectMessage.Type.SCAN_WIFI_LIST_DONE, transWl(receive)))
                }
            }
        }.start()
    }

    init { ee.register("ConnectMessage", this::onScanWifiList) }
    private fun onScanWifiList(message: EventEmitter.Message){
        val msg = message as ConnectMessage
        if(msg.msgType == ConnectMessage.Type.SCAN_WIFI_LIST){
            binding.nearWifiList.visibility = View.GONE
            val params = LinearLayout.LayoutParams(60, 60)
            params.gravity = Gravity.CENTER_VERTICAL
            val al = AVLoadingIndicatorView(activity)
            al.layoutParams = params
            al.setIndicatorColor(R.color.gray)
            binding.loadingOrShowWifiList.addView(al)
        }
    }

    init { ee.register("ConnectMessage", this::onScanWifiListDone) }
    private fun onScanWifiListDone(message: EventEmitter.Message){
        val msg = message as ConnectMessage
        if(msg.msgType == ConnectMessage.Type.SCAN_WIFI_LIST_DONE){
            isCancelable = true
            binding.loadingOrShowWifiList.removeViewAt(1)
            wifiListAdapter = WifiListAdapter(activity, state.wl, R.layout.adapter_wifi_list, BR.ssid)
            wifiListAdapter.state = state
            binding.nearWifiListAdapter = wifiListAdapter
            binding.nearWifiList.visibility = View.VISIBLE
        }
    }

    init { ee.register("ConnectMessage", this::onScanWifiListFailed) }
    private fun onScanWifiListFailed(message: EventEmitter.Message){
        val msg = message as ConnectMessage
        if(msg.msgType == ConnectMessage.Type.SCAN_WIFI_LIST_FAILED){
            isCancelable = true
            Toast.makeText(activity, "扫描wifi列表失败", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun transWl(data: JSONObject): List<SearchableWifi>{
        val ja = data.getJSONObject("reply")
        Log.d("REPLY", ja.toString())
        val list: ObservableArrayList<SearchableWifi> = ObservableArrayList()
        ja.keys().forEach { e ->
            val uuid = state.kwl.filter { e2 -> e2.name.split("@")[0] == e }.map { e3-> e3.uuid}
            list.add(SearchableWifi(state.activity.applicationContext, e,
                    ja.getJSONObject(e).getString("signal").toInt(),
                    ja.getJSONObject(e).getString("encryption"),
                    uuid.isNotEmpty(), uuid as MutableList<String>))
        }
        return  list.filter { e1->
            state.cwl.none { e2->
                e2.name == e1.name
            }
        }.sortedByDescending { e->e.signal }
    }

    override fun onDestroy() {
        super.onDestroy()
        ee.unregister(this::onClose)
        ee.unregister(this::onScanWifiList)
        ee.unregister(this::onScanWifiListDone)
        ee.unregister(this::onScanWifiListFailed)
    }
}