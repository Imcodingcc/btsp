package cn.leither.btsp.activity

import android.app.Activity
import android.app.Fragment
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatDelegate
import android.util.Log
import cn.leither.btsp.handlemsg.EventEmitter
import cn.leither.btsp.handlemsg.IntermediateMessage
import cn.leither.btsp.fragment.LoadingFragment
import cn.leither.btsp.R
import cn.leither.btsp.databinding.ActivityIntermediatesBinding

class IntermediatesActivity : Activity() {

    private val ee = EventEmitter.default
    var binding: ActivityIntermediatesBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_intermediates)
        val map: MutableMap<String, Any> = HashMap<String, Any>()
        map["fragmentID"] = R.id.container
        map["fragment"] = LoadingFragment()
        ee.emit(IntermediateMessage(IntermediateMessage.Type.SWITCH_VIEW, map))
    }

    init { ee.register("IntermediateMessage", this::onSwitchView) }
    private fun onSwitchView(message: EventEmitter.Message){
        val msg = message as IntermediateMessage
        val param = msg.value as MutableMap<*, *>
        val fi = param["fragmentID"] as Int
        val fg = param["fragment"] as Fragment
        val fm = fragmentManager
        val ft = fm.beginTransaction()
        if(msg.msgType == IntermediateMessage.Type.SWITCH_VIEW){
            ft.replace(fi, fg)
            ft.commit()
        }
    }
}