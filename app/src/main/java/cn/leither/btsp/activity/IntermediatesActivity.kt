package cn.leither.btsp.activity

import android.app.Activity
import android.app.Fragment
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
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
        Log.d("BTSP", "onCreate")
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            moveTaskToBack(false)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("BTSP", "MAIN_ACTIVITY_DESTROY")
    }
}