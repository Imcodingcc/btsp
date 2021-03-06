package cn.leither.btsp.adapter

import android.app.Activity
import android.content.Context
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.leither.btsp.fragment.CreateConnDialogFragment
import cn.leither.btsp.handlemsg.ConnectMessage
import cn.leither.btsp.handlemsg.EventEmitter
import cn.leither.btsp.state.ConnectState
import cn.leither.btsp.state.WifiListState
import kotlinx.android.synthetic.main.adapter_wifi_list.view.*

class WifiListAdapter(context: Context, list: List<Any>, layoutId: Int, variableId: Int) : CommonAdapter(context, list, layoutId, variableId){

    lateinit var state: ConnectState
    val ee = EventEmitter.default

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        context as Activity
        val binding: ViewDataBinding? = if (convertView == null) {
            DataBindingUtil.inflate(LayoutInflater.from(context), layoutId, parent, false)
        } else {
            DataBindingUtil.getBinding(convertView)
        }
        binding!!.setVariable(variableId, list[position])
        binding.root.setOnClickListener({
            ee.emit(ConnectMessage(ConnectMessage.Type.WIFI_LIST_DIALOG_CLOSE, state.wl[position]))
        })
        return binding.root
    }
}