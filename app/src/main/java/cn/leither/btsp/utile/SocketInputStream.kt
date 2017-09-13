package cn.leither.btsp.utile

import java.io.IOException
import java.io.InputStream
import java.util.*

class SocketInputStream(private val i: InputStream): InputStream() {
    var deadline: Date = Date(Date().time + 60000)

    override fun available(): Int {
        return i.available()
    }

    override fun close() {
        i.close()
    }

    override fun mark(readlimit: Int) {
        i.mark(readlimit)
    }

    override fun markSupported(): Boolean {
        return i.markSupported()
    }

    override fun read(): Int {
        while(true){
            if(Date().after(deadline)){
                throw IOException("timeout")
            }
            if (available() > 0) {
                val buffer = ByteArray(1)
                if(i.read(buffer, 0, 1) < 0 ) {
                    return -1
                } else {
                    return buffer[0].toInt()
                }
            } else {
                Thread.sleep(50)
            }
        }
    }

    override fun reset() {
        i.reset()
    }

    override fun skip(n: Long): Long {
        return i.skip(n)
    }
}

