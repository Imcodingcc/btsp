package cn.leither.btsp.adapter

import android.app.Activity
import android.content.Context
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import cn.leither.btsp.R
import cn.leither.btsp.entity.KnownWifi
import cn.leither.btsp.fragment.ConnectedDetailDialogFragment
import cn.leither.btsp.state.WifiListState
import com.wang.avi.AVLoadingIndicatorView
import kotlinx.android.synthetic.main.adapter_know_wifi_list.view.*

class ConnectedWifiAdapter(context: Context, list: List<Any>, layoutId: Int, variableId: Int) :
        CommonAdapter(context, list, layoutId, variableId){

    var state: WifiListState? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        context as Activity
        val binding: ViewDataBinding? = if (convertView == null) {
            DataBindingUtil.inflate(LayoutInflater.from(context), layoutId, parent, false)
        } else {
            DataBindingUtil.getBinding(convertView)
        }
        binding!!.setVariable(variableId, list[position])
        if((list[position] as KnownWifi).state == "--"){
            binding.root.loading_or_connected.removeAllViews()
            val params = LinearLayout.LayoutParams(60, 60)
            val al = AVLoadingIndicatorView(context)
            al.layoutParams = params
            al.setIndicatorColor(R.color.gray)
            binding.root.loading_or_connected.addView(al)
        }
        binding.root.setOnClickListener({
            val dialog = ConnectedDetailDialogFragment(state!!, state!!.cwl[position].name, state!!.cwl[position].state, binding)
            dialog.show(context.fragmentManager, "WIFI_DETAIL")
        })
        return binding.root
    }
}
