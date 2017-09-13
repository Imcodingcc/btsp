package cn.leither.btsp.entity

class KnownWifi(var name: String, var state: String, val lock: String, val uuid: String){

    var showState:String? = null
    var showName: String? = null
    init {
        showName = if(name.split("@")[0].length > 12){
            name.split("@")[0].substring(0, 12) + "..."
        }else{
            name.split("@")[0]
        }
        showState = if(state.length > 6){
            state.substring(0, 5) + "..."
        }else{
            state
        }
    }

}