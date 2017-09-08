package cn.leither.btsp.adapter

import android.app.Activity
import android.content.Context
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.leither.btsp.fragment.ActivateConnDialogFragment
import cn.leither.btsp.fragment.CreateConnDialogFragment
import cn.leither.btsp.state.WifiListState
import kotlinx.android.synthetic.main.adapter_wifi_list.view.*

/**
 * Created by lvqiang on 17-9-5.
 */

class WifiListAdapter<T>(context: Context, list: List<Any>, layoutId: Int, variableId: Int) : CommonAdapter<Any>(context, list, layoutId, variableId){

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
        binding!!.root.will_connect_loading.removeAllViews()
        binding!!.root.setOnClickListener(View.OnClickListener {
            val dialog = CreateConnDialogFragment(state!!, state!!.wl[position].name, state!!.wl[position].uuid, binding!!)
            dialog.show(context.fragmentManager, "CONNECT_WIFI")
        })
        return binding.root
    }
}