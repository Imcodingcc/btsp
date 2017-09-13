package cn.leither.btsp.handlemsg

import java.util.concurrent.CopyOnWriteArrayList

typealias Handler = (EventEmitter.Message) -> Unit
typealias HandlerList = CopyOnWriteArrayList<Handler>


