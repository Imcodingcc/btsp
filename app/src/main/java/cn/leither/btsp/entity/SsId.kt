package cn.leither.btsp.entity

/**
 * Created by lvqiang on 17-8-22.
 */
class SsId(var name:String, val signal: String, val lock: String, val known:Boolean, val uuid: MutableList<String>){
    var showName:String? = null
    init {
        if(name.length > 11){
            showName = name.substring(0, 11) + "..."
        }else{
            showName = name
        }
    }

}