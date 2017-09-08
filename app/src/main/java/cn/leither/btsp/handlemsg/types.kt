package cn.leither.btsp.handlemsg

import java.util.concurrent.CopyOnWriteArrayList

/**
 * Created by lvqiang on 17-8-18.
 */
typealias Handler = (EventEmitter.Message) -> Unit
typealias HandlerList = CopyOnWriteArrayList<Handler>


