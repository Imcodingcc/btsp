package cn.leither.btsp.fragment

import android.annotation.SuppressLint
import android.app.DialogFragment
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import cn.leither.btsp.R
import cn.leither.btsp.databinding.WeightEnterPasswordBinding
import cn.leither.btsp.state.WifiListState

@SuppressLint("ValidFragment")
class CreateConnDialogFragment @SuppressLint("ValidFragment") constructor
(val state: WifiListState, private val ssid: String, private val item_binding: ViewDataBinding) : DialogFragment(){
    var binding:WeightEnterPasswordBinding? = null
    private var isKnown: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val style = STYLE_NO_TITLE
        val theme = 0
        setStyle(style, theme)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.weight_enter_password, container , false)
        binding!!.wepDevList.adapter = ArrayAdapter(activity, android.R.layout.simple_list_item_single_choice, state.devl)
        binding!!.wepDevList.onItemClickListener = AdapterView.OnItemClickListener { _, _, i, _ ->
            if(state.kwl.any { e -> Log.d("BTSP", "KWL " + e.name); ssid + "@" + state.devl[i] == e.name }){
                binding!!.ePassword.visibility = View.GONE
                binding!!.noOrDeleteConfig.text = "忘记网络"
                binding!!.noOrDeleteConfig.setOnClickListener({
                    dismiss()
                    val position = binding!!.wepDevList.checkedItemPosition
                    if(ListView.INVALID_POSITION != position){
                        val groupName= ssid + "@" + state.devl[position]
                        val uuid = state.kwl.filter { e-> e.name == groupName }[0].uuid
                        val map: MutableMap<String, String> = HashMap()
                        map["uuid"] = uuid
                        state.toStage(WifiListState.Stage.DELETE_WIFI_CONNECTION, state.activity::toDeleteConnection, map)
                    }else{
                        //TODO
                    }

                })
                isKnown = true
            }else{
                binding!!.noOrDeleteConfig.text = "取消"
                binding!!.noOrDeleteConfig.setOnClickListener({})
                binding!!.ePassword.visibility = View.VISIBLE
                isKnown = false
            }
        }
        binding!!.surePassword.setOnClickListener({
            dismiss()
            val position = binding!!.wepDevList.checkedItemPosition
            if(ListView.INVALID_POSITION != position){
                val map: MutableMap<String, String> = HashMap()
                //al.smoothToShow()
                map["iface"] = state.devl[position]
                val groupName= ssid + "@" + state.devl[position]
                map["name"] = groupName
                map["ssid"] = ssid
                if(isKnown){
                    map["uuid"] = state.kwl.filter { e-> e.name == groupName }[0].uuid
                    state.toStage(WifiListState.Stage.CANCEL_SCAN_WIFI, state.activity::toCancelScanWifiForActivateConnect, map)
                }else{
                    map["password"] = binding!!.ePassword.text.toString()
                    state.toStage(WifiListState.Stage.CANCEL_SCAN_WIFI, state.activity::toCancelScanWifiForCreateConnConnect, map)
                }
            }else{
                //TODO
            }
        })
        return binding?.root!!
    }
}