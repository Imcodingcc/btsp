package cn.leither.btsp

import android.annotation.SuppressLint
import android.app.DialogFragment
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.leither.btsp.databinding.WeightEnterPasswordBinding
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream

@SuppressLint("ValidFragment")
/**
 * Created by lvqiang on 17-8-25.
 */
class EnterPasswordDialogFragment @SuppressLint("ValidFragment") constructor
(val state: MainState, val ssid: String, val iface: String, val input: InputStream, val output: OutputStream) : DialogFragment(){


    var ew:WeightEnterPasswordBinding? = null
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        ew = DataBindingUtil.inflate(inflater, R.layout.weight_enter_password , container , false)
        ew!!.surePassword.setOnClickListener(View.OnClickListener { view ->
            Log.d("BTSP", "onClick")
            state.connectedSocket.connect()
            val cw = CreateWifiConnection(input, output)
            val json:MutableMap<String, String> = HashMap<String, String>()
            json["ssid"] = ssid
            json["iface"] = iface
            Log.d("BTSP", "password" +  ew!!.ePassword.text.toString())
            json["password"] = ew!!.ePassword.text.toString()
            cw.iface = JSONObject(json)
            cw.send()
            Log.d("BTSP", "cw send")
            val re = cw.recv()
            if(re != null){
                Log.d("BTSP", re.toString())
            }
        })
        Log.d("BTSP", "onCreateView")
        return ew?.root!!
    }
}