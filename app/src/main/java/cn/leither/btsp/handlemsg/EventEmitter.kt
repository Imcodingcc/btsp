package cn.leither.btsp.handlemsg

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Created by lvqiang on 17-8-18.
 */

class EventEmitter{
    val handlers = ConcurrentHashMap<String, HandlerList>()

    interface Message {
        val type: String
    }

    companion object {
        val default = EventEmitter()
    }

    fun emit(msg: Message) {
        val list = handlers[msg.type]
        list?.forEach { f -> f(msg) }
    }

    fun register(type: String, handler: Handler): Boolean {
        val list = handlers[type] ?: CopyOnWriteArrayList<Handler>()
        val added = list.addIfAbsent(handler)
        handlers[type] = list
        return added
    }

    fun unregister(handler: Handler): Boolean {
        var deleted = 0
        handlers.values.forEach { l ->
            val d = l.remove(handler)
            if (d) {
                deleted ++
            }
        }
        return deleted > 0
    }
}