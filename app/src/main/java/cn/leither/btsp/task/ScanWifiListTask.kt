package cn.leither.btsp.task

import android.databinding.ObservableArrayList
import android.os.AsyncTask
import android.util.Log
import cn.leither.btsp.handlemsg.EventEmitter
import cn.leither.btsp.handlemsg.WifiListMessage
import cn.leither.btsp.entity.SsId
import cn.leither.btsp.state.WifiListState
import cn.leither.btsp.utile.CommandHandler
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class ScanWifiListTask(val state: WifiListState): AsyncTask<Unit, JSONObject, Boolean>(){
    private var param: MutableMap<*, *>? = null
    private var nextAction: WifiListState.Stage = WifiListState.Stage.DEFAULT
    private val ee = EventEmitter.default
    override fun doInBackground(vararg p0: Unit?): Boolean? {
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                try {
                    val receive = CommandHandler.handleCommand("SCAN_WIFI", state.device)
                    publishProgress(null)
                    if(receive == null){
                        publishProgress(JSONObject("{'reply': ''}"))
                    }else{
                        publishProgress(receive)
                    }
                }catch (e: InterruptedException){
                    Log.d("BTSP", "SCAN_RESULT cancelling")
                }catch (e: IOException) {
                    Log.d("BTSP", "SCAN_RESULT IO error" + e.localizedMessage)
                }

            }
        }, 100, 10000)
        while(true){
            if(isCancelled){
                Log.d("BTSP", "isCancelled")
                timer.cancel()
                return isCancelled
            }
        }
    }

    override fun onProgressUpdate(vararg values: JSONObject?) {
        super.onProgressUpdate(*values)
        if(!isCancelled){
            values.map { v ->
                if(v!= null){
                    if(v.getString("reply" ) == ""){
                        state.wl = ArrayList()
                        ee.emit(WifiListMessage(WifiListMessage.Type.SCAN_WIFI_LIST_DISCOVER, null))
                    }else{
                        val wl = transWl(v)
                        state.wl = wl
                        ee.emit(WifiListMessage(WifiListMessage.Type.SCAN_WIFI_LIST_DISCOVER, wl))
                    }
                }else{
                    ee.emit(WifiListMessage(WifiListMessage.Type.SCANNING_WIFI_LIST, null))
                }
            }
        }
    }

    private fun preCancel(myInterruptRunning: Boolean, param: MutableMap<*, *>?){
        this.param = param
        this.cancel(myInterruptRunning)
    }

    fun cancelForDisconnect(myInterruptRunning: Boolean, param: Any?){
        preCancel(myInterruptRunning, param as MutableMap<*, *>)
        this.nextAction = WifiListState.Stage.DISCONNECT_WIFI
    }

    fun cancelForActivateConnect(myInterruptRunning: Boolean, param: Any?){
        preCancel(myInterruptRunning, param as MutableMap<*, *>)
        this.nextAction = WifiListState.Stage.ACTIVATE_WIFI
    }

    fun cancelForCreateConnect(myInterruptRunning: Boolean, param: Any?){
        Log.d("BTSP", "CANCEL FOR CREATE")
        preCancel(myInterruptRunning, param as MutableMap<*, *>)
        this.nextAction = WifiListState.Stage.CREATED_WIFI_CONNECTION
    }

    override fun onCancelled() {
        super.onCancelled()
        Log.d("BTSP", "nextAction" + nextAction.toString())
        when(nextAction){
            WifiListState.Stage.DISCONNECT_WIFI ->{
                state.toStage(nextAction, state.activity::toDisConnectWifi, param)
            }
            WifiListState.Stage.ACTIVATE_WIFI ->{
                state.toStage(nextAction, state.activity::toActivateWifi, param)
            }
            WifiListState.Stage.CREATED_WIFI_CONNECTION ->{
                state.toStage(nextAction, state.activity::toCreateWifiConn, param)
            }
            else -> {//Nothing to do
            }
        }
    }

    private fun transWl(data: JSONObject): List<SsId>{
        val ja = data.getJSONObject("reply")
        val list: ObservableArrayList<SsId> = ObservableArrayList()
        ja.keys().forEach { e ->
            val uuid = state.kwl.filter { e2 -> e2.name.split("@")[0] == e }.map { e3-> e3.uuid}
            list.add(SsId(e, ja.getJSONObject(e).getString("signal"), "加密的", uuid.isNotEmpty(), uuid as MutableList<String>))
        }
        return  list.filter { e1->
            state.cwl.none { e2->
                e2.name == e1.name
            }
        }.sortedByDescending { e->e.signal }
    }
}