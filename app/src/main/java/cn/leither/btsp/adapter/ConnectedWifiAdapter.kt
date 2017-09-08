package cn.leither.btsp.adapter

import android.app.Activity
import android.content.Context
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.leither.btsp.fragment.ConnectedDetailDialogFragment
import cn.leither.btsp.state.WifiListState

/**
 * Created by lvqiang on 17-9-5.
 */

class ConnectedWifiAdapter<T>(context: Context, list: List<Any>, layoutId: Int, variableId: Int) : CommonAdapter<Any>(context, list, layoutId, variableId){

    var state: WifiListState? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        context as Activity
        var binding: ViewDataBinding? = null
        if (convertView == null) {
            binding = DataBindingUtil.inflate(LayoutInflater.from(context), layoutId, parent, false)
        } else {
            binding = DataBindingUtil.getBinding(convertView)
        }
        binding!!.setVariable(variableId, list[position])
        binding!!.root.setOnClickListener(View.OnClickListener {
            val dialog = ConnectedDetailDialogFragment(state!!, state!!.cwl[position].name, state!!.cwl[position].state, binding!!)
            dialog.show(context.fragmentManager, "WIFI_DETAIL")
        })
        return binding.root
    }
}

