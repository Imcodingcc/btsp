package cn.leither.btsp.task

import android.databinding.ObservableArrayList
import android.os.AsyncTask
import android.util.Log
import cn.leither.btsp.handlemsg.EventEmitter
import cn.leither.btsp.handlemsg.WifiListMessage
import cn.leither.btsp.command.ScanCommand
import cn.leither.btsp.entity.SsId
import cn.leither.btsp.state.WifiListState
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by lvqiang on 17-8-30.
 */
class ScanWifiListTask(val state: WifiListState): AsyncTask<Unit, JSONObject, Boolean>(){
    var input: InputStream? = null
    var output: OutputStream? = null
    var param: MutableMap<*, *>? = null
    var nextAction: WifiListState.Stage = WifiListState.Stage.DEFAULT
    private val ee = EventEmitter.default
    init {
        input = state.sockets[0].inputStream
        output = state.sockets[0].outputStream
    }
    override fun doInBackground(vararg p0: Unit?): Boolean? {
        val sc = ScanCommand(input!!, output!!)
        while(true){
            if(isCancelled){
                return isCancelled
            }
            try {
                state.connectedSocket.connect()
                sc.send()
                publishProgress(null)
                val re = sc.recv()
                Log.d("BTSP", "SCAN_RESULT" + re.toString())
                if(re != null){
                    publishProgress(re)
                    Thread.sleep(10000)
                }else{
                    publishProgress(JSONObject("{'reply': ''}"))
                    Thread.sleep(10000)
                }
            }catch (e: Exception){
                publishProgress(null)
                Log.d("BTSP", "connect failed")
                Thread.sleep(3000)

            }
        }
    }

    override fun onProgressUpdate(vararg values: JSONObject?) {
        super.onProgressUpdate(*values)
            values.map { v ->
                if(v!= null){
                    if(v.getString("reply" ) == ""){
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

    private fun preCancel(myInterruptRunning: Boolean, param: MutableMap<*, *>){
        this.param = param
        this.cancel(myInterruptRunning)
        //TODO wait to test
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
        preCancel(myInterruptRunning, param as MutableMap<*, *>)
        this.nextAction = WifiListState.Stage.CREATED_WIFI_CONNECTION
    }


    override fun onCancelled() {
        super.onCancelled()
        when(nextAction){
            WifiListState.Stage.DISCONNECT_WIFI ->{
                state.toStage(nextAction, state.activity::toDisConnectWifi, param)
            }
            WifiListState.Stage.ACTIVATE_WIFI ->{
                state.toStage(nextAction, state.activity::toActivateWifi, param)
            }
            WifiListState.Stage.CREATED_WIFI_CONNECTION ->{
                var map:MutableMap<String, String> = HashMap()
                state.toStage(nextAction, state.activity::toCreateWifiConn, param)
            }
        }
    }

    private fun transWl(data: JSONObject): List<SsId>{
        val ja = data.getJSONObject("reply")
        var list: ObservableArrayList<SsId> = ObservableArrayList()
        ja.keys().forEach { e ->
            val uuid = state.kwl.filter { e2 -> e2.name.split("@")[0] == e }.map { e-> e.uuid}
            list.add(SsId(e, ja.getJSONObject(e).getString("signal"), "加密的", uuid.isNotEmpty(), uuid as MutableList<String>))
        }
        return list.filter { e1->
            state.cwl.none { e2->
                e2.name == e1.name
            }
        }
    }
}