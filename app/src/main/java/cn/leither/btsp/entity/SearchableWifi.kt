package cn.leither.btsp.entity

import android.content.Context
import android.graphics.drawable.Drawable
import cn.leither.btsp.R

class SearchableWifi(val context: Context, var name:String, val signal: Int, val lock: String, val known:Boolean, val uuid: MutableList<String>){
    var showName:String? = null
    var showIcon: Drawable? =  context.resources.getDrawable(R.drawable.ic_signal_wifi_4_bar_lock_black_24dp)
    init {
        showName = if(name.length > 22){
            name.substring(0, 22) + "..."
        }else{
            name
        }
        showIcon = when{
            (lock) != "" ->
            when {
                signal > 80 -> context.resources.getDrawable(R.drawable.ic_signal_wifi_4_bar_lock_black_24dp)
                signal > 60 -> context.resources.getDrawable(R.drawable.ic_signal_wifi_3_bar_lock_black_24dp)
                signal > 40 -> context.resources.getDrawable(R.drawable.ic_signal_wifi_2_bar_lock_black_24dp)
                signal > 20 -> context.resources.getDrawable(R.drawable.ic_signal_wifi_1_bar_lock_black_24dp)
                signal > 0 ->  context.resources.getDrawable(R.drawable.ic_signal_wifi_1_bar_lock_black_24dp)
                else -> null
            }else ->{
                when{
                    signal > 80 -> context.resources.getDrawable(R.drawable.ic_signal_wifi_4_bar_black_24dp)
                    signal > 60 -> context.resources.getDrawable(R.drawable.ic_signal_wifi_3_bar_black_24dp)
                    signal > 40 -> context.resources.getDrawable(R.drawable.ic_signal_wifi_2_bar_black_24dp)
                    signal > 20 -> context.resources.getDrawable(R.drawable.ic_signal_wifi_1_bar_black_24dp)
                    signal > 0 -> context.resources.getDrawable(R.drawable.ic_signal_wifi_0_bar_black_24dp)
                    else -> null
                }
            }
        }
    }
}