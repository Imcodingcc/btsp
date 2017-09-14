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

    override fun mark(readLimit: Int) {
        i.mark(readLimit)
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
                return if(i.read(buffer, 0, 1) < 0 ) {
                    -1
                } else {
                    buffer[0].toInt()
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

