package cn.leither.btsp.entity

/**
 * Created by lvqiang on 17-8-24.
 */
class KnownWifi(var name: String, var state: String, val lock: String, val uuid: String){

    var showState:String? = null
    var showName: String? = null
    init {
        if(name.length > 12){
            showName = name.substring(0, 12) + "..."
        }else{
            showName = name
        }
        if(state.length > 6){
            showState = state.substring(0, 5) + "..."
        }else{
            showState = state
        }
    }

}