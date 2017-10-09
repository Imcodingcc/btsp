package cn.leither.btsp.fragment

import android.annotation.SuppressLint
import android.app.DialogFragment
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.leither.btsp.BR
import cn.leither.btsp.R
import cn.leither.btsp.adapter.InterfaceListAdapter
import cn.leither.btsp.databinding.WeightIfaceListBinding
import cn.leither.btsp.handlemsg.ConnectMessage
import cn.leither.btsp.handlemsg.EventEmitter
import cn.leither.btsp.state.ConnectState

@SuppressLint("ValidFragment")
class InterfaceDialogFragment @SuppressLint("ValidFragment") constructor
(val state: ConnectState) : DialogFragment(){
    lateinit var binding: WeightIfaceListBinding
    private val ee = EventEmitter.default
    lateinit var interfaceListAdapter: InterfaceListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val style = STYLE_NO_TITLE
        val theme = 0
        setStyle(style, theme)
    }
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.weight_iface_list, container, false)
        return binding.root!!
    }

    override fun onStart() {
        super.onStart()
        Log.d("BTSP", "DEVLLL " + state.devl.toString())
        interfaceListAdapter = InterfaceListAdapter(activity, state.devl, R.layout.adapter_interface_name, BR.interface_name)
        interfaceListAdapter.state = state
        binding.ifaceListAdapter = interfaceListAdapter
    }

    init { ee.register("ConnectMessage", this::onClose) }
    private fun onClose(message: EventEmitter.Message){
        val msg = message as ConnectMessage
        if(msg.msgType == ConnectMessage.Type.INTERFACE_LIST_DIALOG_CLOSE){
            ee.emit(ConnectMessage(ConnectMessage.Type.INTERFACE_NAME, msg.value))
            dismiss()
        }
    }
}