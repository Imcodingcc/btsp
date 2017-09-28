package cn.leither.btsp.activity

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import cn.leither.btsp.R
import cn.leither.btsp.databinding.ActivityWifiDetailBinding
import cn.leither.btsp.entity.NetworkDetail
import cn.leither.btsp.handlemsg.EventEmitter
import cn.leither.btsp.handlemsg.WifiDetailMessage
import cn.leither.btsp.state.WifiDetailState
import cn.leither.btsp.utile.CommandHandler
import org.json.JSONObject

class WifiDetailActivity: Activity(){
    private lateinit var iFace: String
    private lateinit var wifiName: String
    private val ee = EventEmitter.default
    lateinit var device: BluetoothDevice
    lateinit var binding: ActivityWifiDetailBinding
    lateinit var state: WifiDetailState
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_wifi_detail)
        state = WifiDetailState(activity = this)
        state.toStage(WifiDetailState.Stage.INIT, this::toInit)
    }

    private fun toInit(old: WifiDetailState){
        wifiName = intent.getStringExtra("name").split("@")[0]
        iFace= intent.getStringExtra("name").split("@")[1]
        device = intent.getParcelableExtra("device")
        ee.emit(WifiDetailMessage(WifiDetailMessage.Type.INIT, null))
        state.toStage(WifiDetailState.Stage.GET_INTERFACE_DETAIL, this::toGetInterfaceDetail)
    }

    init { ee.register("WifiDetailMessage", this::onInit) }
    private fun onInit(message: EventEmitter.Message){
        val msg = message as WifiDetailMessage
        if(msg.msgType == WifiDetailMessage.Type.INIT){
            Log.d("BTSP", "fetching")
            Toast.makeText(this, "正在获取", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toGetInterfaceDetail(old: WifiDetailState){
        Thread{
            val receive = CommandHandler.handleCommand("GET_INTERFACE_DETAIL", device, JSONObject("{iface: $iFace}"))
            if(receive != null){
                val content = receive.getJSONObject("reply")
                val ipv4 = content.getJSONObject("ipv4")
                val v4Address = ipv4.getJSONArray("address").get(0) as String
                val v4Gateway = ipv4.getString("gateway")
                val hdAddress = content.getJSONObject("general").getString("hdaddr")
                val map: MutableMap<String, String> = HashMap()
                map["v4Address"] = v4Address
                map["v4Gateway"] = v4Gateway
                map["hdAddress"] = hdAddress
                runOnUiThread{
                    ee.emit(WifiDetailMessage(WifiDetailMessage.Type.GET_INTERFACE_DETAIL, map))
                }
            }
        }.start()
    }

    init { ee.register("WifiDetailMessage", this::onGetInterfaceDetail) }
    private fun onGetInterfaceDetail(message: EventEmitter.Message){
        val msg = message as WifiDetailMessage
        if(msg.msgType == WifiDetailMessage.Type.GET_INTERFACE_DETAIL){
            val value = msg.value as HashMap<*, *>
            binding.networkDetail =
                    NetworkDetail(value["v4Address"] as String, value["v4Gateway"] as String,
                            iFace, value["hdAddress"] as String, wifiName)
        }
    }
}