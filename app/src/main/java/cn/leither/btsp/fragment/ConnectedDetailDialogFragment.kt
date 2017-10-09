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
import android.widget.LinearLayout
import cn.leither.btsp.R
import cn.leither.btsp.databinding.WeightConnectedDetailBinding
import cn.leither.btsp.state.WifiListState
import com.wang.avi.AVLoadingIndicatorView
import kotlinx.android.synthetic.main.adapter_know_wifi_list.view.*

@SuppressLint("ValidFragment")
class ConnectedDetailDialogFragment @SuppressLint("ValidFragment") constructor
(private val state: WifiListState, private val ssid: String, private val iface: String, private val item_binding:ViewDataBinding) : DialogFragment(){
    var binding: WeightConnectedDetailBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val style = DialogFragment.STYLE_NO_TITLE
        val theme = 0
        setStyle(style, theme)
    }
    @SuppressLint("ResourceAsColor")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.weight_connected_detail, container, false)
        binding!!.ssid = ssid
        binding!!.wifiDetailName.paint.isFakeBoldText = true
        binding!!.wifiDetailDisconnect.setOnClickListener({
            dismiss()
            item_binding.root.loading_or_connected.removeAllViews()
            val params = LinearLayout.LayoutParams(60, 60)
            val al = AVLoadingIndicatorView(it.context)
            al.layoutParams = params
            al.setIndicatorColor(R.color.gray)
            item_binding.root.loading_or_connected.addView(al)
            val param: MutableMap<String, String> = HashMap()
            param["iface"] = iface
            //state.toStage(WifiListState.Stage.CANCEL_SCAN_WIFI, state.activity::toCancelScanWifiForDisconnect, param)
        })
        return binding?.root!!
    }
}