package cn.leither.btsp.activity

import android.app.Activity
import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.widget.Toast
import cn.leither.btsp.R
import cn.leither.btsp.databinding.ActivityWifiDetailBinding
import cn.leither.btsp.entity.NetworkDetail
import cn.leither.btsp.handlemsg.EventEmitter
import cn.leither.btsp.handlemsg.WifiDetailMessage
import cn.leither.btsp.handlemsg.WifiListMessage
import cn.leither.btsp.state.WifiDetailState
import cn.leither.btsp.utile.CommandHandler
import org.json.JSONObject

class WifiDetailActivity: Activity(){
    private lateinit var iFace: String
    private lateinit var wifiName: String
    private val ee = EventEmitter.default
    lateinit var device: BluetoothDevice
    lateinit var binding: ActivityWifiDetailBinding
    lateinit var uuid: String
    lateinit var state: WifiDetailState
    private var loadingDialog: Dialog? = null
    var backReason: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_wifi_detail)
        loadingDialog = Dialog(this, R.style.Dialog_Fullscreen)
        loadingDialog!!.setContentView(R.layout.dialog_fullscreen_loading)
        loadingDialog!!.setOnKeyListener({ _, _, _-> true })
        state = WifiDetailState(activity = this)
        state.toStage(WifiDetailState.Stage.INIT, this::toInit)
    }

    private fun toInit(old: WifiDetailState){
        uuid = intent.getStringExtra("uuid")
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
            binding.disconnInDetail.setOnClickListener({
                state.toStage(WifiDetailState.Stage.DISCONNECTION, this::toDisConnection)
            })
            binding.forgetInDetail.setOnClickListener({
                state.toStage(WifiDetailState.Stage.DELETE_CONNECTION, this::toDeleteConnection)
            })
            loadingDialog!!.show()
            Toast.makeText(this, "正在获取网络信息", Toast.LENGTH_SHORT).show()
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
            }else{
                runOnUiThread{
                    ee.emit(WifiDetailMessage(WifiDetailMessage.Type.GET_INTERFACE_DETAIL_FAILED, null))
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
            loadingDialog!!.dismiss()
        }
    }
    init { ee.register("WifiDetailMessage", this::onGetInterfaceDetailFailed) }
    private fun onGetInterfaceDetailFailed(message: EventEmitter.Message){
        val msg = message as WifiDetailMessage
        if(msg.msgType == WifiDetailMessage.Type.GET_INTERFACE_DETAIL_FAILED){
            Toast.makeText(this, "信息获取失败", Toast.LENGTH_SHORT).show()
            loadingDialog!!.dismiss()
        }
    }

    private fun toDisConnection(old: WifiDetailState){
        ee.emit(WifiDetailMessage(WifiDetailMessage.Type.DISCONNECTION, null))
        Thread{
            val receive = CommandHandler.handleCommand("DISCONNECT", device, JSONObject("{iface: $iFace}"))
            if(receive == null){
                runOnUiThread { ee.emit(WifiDetailMessage(WifiDetailMessage.Type.DISCONNECTION_FAILED, null)) }
            }else{
                runOnUiThread{ backReason = "disconnection"; onBackPressed() }
            }
        }.start()
    }

    private fun toDeleteConnection(old: WifiDetailState){
        ee.emit(WifiDetailMessage(WifiDetailMessage.Type.DELETE_CONNECTION, null))
        Thread{
            val receive = CommandHandler.handleCommand("DELETE_CONNECTION", device, JSONObject("{uuid: $uuid}"))
            if(receive == null){
                runOnUiThread { runOnUiThread { ee.emit(WifiDetailMessage(WifiDetailMessage.Type.DELETE_CONNECTION_FAILED, null)) } }
            }else{
                runOnUiThread { backReason = "deleteConnection"; onBackPressed() }
            }
        }.start()
    }

    init { ee.register("WifiDetailMessage", this::onDisconnection) }
    private fun onDisconnection(message: EventEmitter.Message){
        val msg = message as WifiDetailMessage
        if(msg.msgType == WifiDetailMessage.Type.DISCONNECTION){
            loadingDialog!!.show()
        }

    }

    init { ee.register("WifiDetailMessage", this::onDisconnectionFailed) }
    private fun onDisconnectionFailed(message: EventEmitter.Message){
        val msg = message as WifiDetailMessage
        if(msg.msgType == WifiDetailMessage.Type.DISCONNECTION_FAILED){
            Toast.makeText(this, "操作失败了", Toast.LENGTH_SHORT).show()
            loadingDialog!!.dismiss()
        }
    }


    init { ee.register("WifiDetailMessage", this::onDeleteConnection) }
    private fun onDeleteConnection(message: EventEmitter.Message){
        val msg = message as WifiDetailMessage
        if(msg.msgType == WifiDetailMessage.Type.DELETE_CONNECTION){
            loadingDialog!!.show()
        }
    }

    init { ee.register("WifiDetailMessage", this::onDeleteConnectionFailed) }
    private fun onDeleteConnectionFailed(message: EventEmitter.Message){
        val msg = message as WifiDetailMessage
        if(msg.msgType == WifiDetailMessage.Type.DELETE_CONNECTION_FAILED){
            Toast.makeText(this, "操作失败了", Toast.LENGTH_SHORT).show()
            loadingDialog!!.dismiss()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        ee.emit(WifiListMessage(WifiListMessage.Type.BACK, backReason))
    }

    override fun onDestroy() {
        super.onDestroy()
        ee.unregister(this::onInit)
        ee.unregister(this::onDisconnection)
        ee.unregister(this::onDeleteConnection)
        ee.unregister(this::onGetInterfaceDetail)
        ee.unregister(this::onDisconnectionFailed)
        ee.unregister(this::onDeleteConnectionFailed)
        ee.unregister(this::onGetInterfaceDetailFailed)
    }
}