package cn.leither.btsp.entity

class SsId(var name:String, val signal: String, val lock: String, val known:Boolean, val uuid: MutableList<String>){
    var showName:String? = null
    init {
        showName = if(name.length > 11){
            name.substring(0, 11) + "..."
        }else{
            name
        }
    }

}