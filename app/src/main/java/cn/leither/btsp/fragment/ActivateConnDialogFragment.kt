package cn.leither.btsp.fragment

import android.annotation.SuppressLint
import android.app.DialogFragment
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import cn.leither.btsp.R
import cn.leither.btsp.databinding.WeightConnectKnownBinding
import cn.leither.btsp.state.WifiListState
import com.wang.avi.AVLoadingIndicatorView
import kotlinx.android.synthetic.main.adapter_wifi_list.view.*

@SuppressLint("ValidFragment")
/**
 * Created by lvqiang on 17-8-25.
 */
class ActivateConnDialogFragment @SuppressLint("ValidFragment") constructor
(val state: WifiListState, val ssid: String, val uuid: String, val item_binding: ViewDataBinding) : DialogFragment(){
    var binding: WeightConnectKnownBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val style = STYLE_NO_TITLE
        val theme = 0
        setStyle(style, theme)
    }
    @SuppressLint("ResourceAsColor")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.weight_connect_known, container, false)
        binding!!.ssid = ssid
        binding!!.devList.adapter = ArrayAdapter(activity, android.R.layout.simple_list_item_single_choice, state.devl)
        binding!!.wifiDetailName.paint.isFakeBoldText = true
        binding!!.wifiDetailConnect.setOnClickListener(View.OnClickListener {
            dismiss()
            item_binding.root.will_connect_loading.removeAllViews()
            val params = LinearLayout.LayoutParams(60, 60)
            val al = AVLoadingIndicatorView(it.context)
            al.layoutParams = params
            al.setIndicatorColor(R.color.gray)
            item_binding.root.will_connect_loading.addView(al)
            val position = binding!!.devList.checkedItemPosition
            if(ListView.INVALID_POSITION != position){
                val map: MutableMap<String, String> = HashMap<String, String>()
                map["iface"] = state.devl[position]
                map["uuid"] = uuid
                state.toStage(WifiListState.Stage.CANCEL_SCAN_WIFI, state.activity::toCancelScanWifiForActivateConnect, map)
            }else{
                //TODO
            }
        })
        return binding?.root!!
    }
}