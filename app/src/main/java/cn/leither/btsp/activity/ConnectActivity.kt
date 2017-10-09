package cn.leither.btsp.activity

import android.app.Activity
import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import cn.leither.btsp.R
import cn.leither.btsp.databinding.ActivityConnectBinding
import cn.leither.btsp.entity.ConnectedWifi
import cn.leither.btsp.entity.SearchableWifi
import cn.leither.btsp.fragment.InterfaceDialogFragment
import cn.leither.btsp.fragment.WifiListDialogFragment
import cn.leither.btsp.handlemsg.ConnectMessage
import cn.leither.btsp.handlemsg.EventEmitter
import cn.leither.btsp.handlemsg.WifiListMessage
import cn.leither.btsp.state.ConnectState
import cn.leither.btsp.utile.CommandHandler
import org.json.JSONObject

class ConnectActivity : Activity() {

    private val ee = EventEmitter.default
    lateinit var state: ConnectState
    lateinit var binding: ActivityConnectBinding
    lateinit var device: BluetoothDevice
    private var wifiListDialog: WifiListDialogFragment? = null
    private var interfaceListDialog: InterfaceDialogFragment? = null
    private var loadingDialog: Dialog? = null
    private var backReason = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        state = ConnectState(activity = this)
        device = intent.getParcelableExtra("device")
        state.devl = intent.getStringArrayListExtra("devl")
        state.kwl = intent.getSerializableExtra("kwl") as MutableList<ConnectedWifi>
        state.cwl = intent.getSerializableExtra("cwl") as MutableList<ConnectedWifi>
        binding = DataBindingUtil.setContentView(this, R.layout.activity_connect)
        loadingDialog = Dialog(this, R.style.Dialog_Fullscreen)
        loadingDialog!!.setContentView(R.layout.dialog_fullscreen_loading)
        loadingDialog!!.setOnKeyListener({ _, _, _-> true })
        binding.showWifiList.setOnClickListener({
            wifiListDialog = WifiListDialogFragment(device, state)
            wifiListDialog!!.show(fragmentManager, "wifi_list_dialog_fragment")
        })
        binding.connectSubmit.setOnClickListener({
            val composeName = binding.wifiName + "@" + binding.iFace
            val map: MutableMap<String, String> = HashMap()
            if(isKnown(composeName)){
                map["uuid"] = state.kwl.filter { e-> e.name == composeName }[0].uuid
                val connForm = JSONObject(map)
                state.toStage(ConnectState.Stage.ACTIVE_CONNECTION, this::toActivateWifi, connForm)
            }else{
                map["name"] = composeName
                map["iface"] = binding.iFace as String
                map["ssid"] = binding.wifiName as String
                map["password"] = binding.passWd.text.toString()
                val connForm = JSONObject(map)
                state.toStage(ConnectState.Stage.CREATE_CONNECTION, this::toCreateWifiConn, connForm)
            }
        })
        binding.showInterface.setOnClickListener({
            interfaceListDialog = InterfaceDialogFragment(state)
            interfaceListDialog!!.show(fragmentManager, "interface_list_dialog_fragment")
        })
    }

    init { ee.register("ConnectMessage", this::onWifiName) }
    private fun onWifiName(message: EventEmitter.Message){
        val msg = message as ConnectMessage
        if(msg.msgType == ConnectMessage.Type.WIFI_NAME){
            val searchableWifi = msg.value as SearchableWifi
            binding.wifiName = searchableWifi.name
            state.isLock = searchableWifi.lock
            Log.d("BTSP", "ISLOCK " + state.isLock)
            if(state.isLock == ""){
                binding.ePasswordBox.visibility = View.GONE
                return
            }
            val composeName = binding.wifiName + "@" + binding.iFace
            if(isKnown(composeName)){
                binding.ePasswordBox.visibility = View.GONE
            }else{
                binding.ePasswordBox.visibility = View.VISIBLE
            }
        }
    }

    private fun isKnown(composeName: String): Boolean{
        return state.kwl.any { e -> composeName  == e.name }
    }

    init { ee.register("ConnectMessage", this::onInterfaceName) }
    private fun onInterfaceName(message: EventEmitter.Message){
        val msg = message as ConnectMessage
        if(msg.msgType == ConnectMessage.Type.INTERFACE_NAME){
            binding.iFace = msg.value as String
            val composeName = binding.wifiName + "@" + binding.iFace
            if(isKnown(composeName)){
                binding.ePasswordBox.visibility = View.GONE
            }else{
                binding.ePasswordBox.visibility = View.VISIBLE
            }
        }
    }

    init { ee.register("ConnectMessage", this::onCreateWifiConnDone) }
    private fun onCreateWifiConnDone(message: EventEmitter.Message){
        val msg = message as ConnectMessage
        if(msg.msgType == ConnectMessage.Type.CREATE_CONNECTION_DONE){
            runOnUiThread { backReason = "createConn"; onBackPressed() }
        }
    }

    init { ee.register("ConnectMessage", this::onCreateWifiConnFailed) }
    private fun onCreateWifiConnFailed(message: EventEmitter.Message){
        val msg = message as ConnectMessage
        if(msg.msgType == ConnectMessage.Type.CREATE_CONNECTION_FAILED){
            Toast.makeText(this, msg.value as String, Toast.LENGTH_SHORT).show()
            loadingDialog!!.dismiss()
        }
    }

    init { ee.register("ConnectMessage", this::onActiveWifiConnDone) }
    private fun onActiveWifiConnDone(message: EventEmitter.Message){
        val msg = message as ConnectMessage
        if(msg.msgType == ConnectMessage.Type.ACTIVE_CONNECTION_DONE){
            runOnUiThread { backReason = "activeConn"; onBackPressed() }
        }
    }

    init { ee.register("ConnectMessage", this::onActiveConnFailed) }
    private fun onActiveConnFailed(message: EventEmitter.Message){
        val msg = message as ConnectMessage
        if(msg.msgType == ConnectMessage.Type.ACTIVE_CONNECTION_FAILED){
           Toast.makeText(this, msg.value as String, Toast.LENGTH_SHORT).show()
            loadingDialog!!.dismiss()
        }
    }

    init { ee.register("ConnectMessage", this::onActiveConn) }
    private fun onActiveConn(message: EventEmitter.Message){
        val msg = message as ConnectMessage
        if(msg.msgType == ConnectMessage.Type.ACTIVE_CONNECTION){
            loadingDialog!!.show()
        }
    }

    init { ee.register("ConnectMessage", this::onCreateConn) }
    private fun onCreateConn(message: EventEmitter.Message){
        val msg = message as ConnectMessage
        if(msg.msgType == ConnectMessage.Type.CREATE_CONNECTION){
            loadingDialog!!.show()
        }
    }

    private fun toCreateWifiConn(old: ConnectState, param: Any?){
        ee.emit(ConnectMessage(ConnectMessage.Type.CREATE_CONNECTION, null))
        Thread{
            val receive = CommandHandler.handleCommand("CREATE_WIFI_CONNECTION", device, param as JSONObject)
            if(receive == null){
                runOnUiThread{ ee.emit(ConnectMessage(ConnectMessage.Type.CREATE_CONNECTION_FAILED, "连接超时")) }
            }else{
                when(connectWifiMsg(receive)){
                    "successfully"->
                        runOnUiThread{ ee.emit(ConnectMessage(ConnectMessage.Type.CREATE_CONNECTION_DONE, null)) }
                    "passwordErr"->
                        runOnUiThread { runOnUiThread{ ee.emit(ConnectMessage(ConnectMessage.Type.CREATE_CONNECTION_FAILED, "密码错误"))} }
                    "rangeErr"->
                        runOnUiThread { runOnUiThread{ ee.emit(ConnectMessage(ConnectMessage.Type.CREATE_CONNECTION_FAILED, "超出范围"))} }
                    "timeErr"->
                        runOnUiThread { runOnUiThread{ ee.emit(ConnectMessage(ConnectMessage.Type.CREATE_CONNECTION_FAILED, "连接超时"))} }
                    "unknownErr"->
                        runOnUiThread { runOnUiThread{ ee.emit(ConnectMessage(ConnectMessage.Type.CREATE_CONNECTION_FAILED, "未知错误"))} }
                }
            }
        }.start()
    }

    private fun toActivateWifi(old: ConnectState, param: Any?){
        ee.emit(ConnectMessage(ConnectMessage.Type.ACTIVE_CONNECTION, null))
        Thread{
            val receive = CommandHandler.handleCommand("ACTIVATE_CONNECTION", device, param as JSONObject)
            if(receive == null){
                runOnUiThread { runOnUiThread{ ee.emit(ConnectMessage(ConnectMessage.Type.ACTIVE_CONNECTION_FAILED, "连接失败"))} }
            }else{
                when(activateWifiMsg(receive)){
                    "successfully"->
                        runOnUiThread { runOnUiThread{ ee.emit(ConnectMessage(ConnectMessage.Type.ACTIVE_CONNECTION_DONE, null))} }
                    else->
                        runOnUiThread { runOnUiThread{ ee.emit(ConnectMessage(ConnectMessage.Type.ACTIVE_CONNECTION_FAILED, "连接失败"))} }
                }
            }
        }.start()
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

    private fun activateWifiMsg(re: JSONObject): String{
        val reply = re.getJSONObject("reply").toString()
        return when {
            reply.indexOf("error", 0, false) != -1 -> "error"
            else -> "successfully"
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        ee.emit(WifiListMessage(WifiListMessage.Type.BACK, backReason))
    }

    override fun onDestroy() {
        super.onDestroy()
        ee.unregister(this::onWifiName)
        ee.unregister(this::onActiveConn)
        ee.unregister(this::onCreateConn)
        ee.unregister(this::onInterfaceName)
        ee.unregister(this::onActiveConnFailed)
        ee.unregister(this::onActiveWifiConnDone)
        ee.unregister(this::onCreateWifiConnDone)
        ee.unregister(this::onCreateWifiConnFailed)
    }

}