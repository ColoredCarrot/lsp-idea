package info.voidev.lspidea.lspex.debug

import com.google.gson.Gson
import java.io.InputStream

class JrpcDebugger {

    lateinit var gson: Gson

    /*
    Contains state shared between the message consumer for sent and
    the message consumer for received messages.
     */

    var messageObserver: JrpcMessageObserver? = null

    @Volatile
    var captureContents = true // TODO should be false by default
    @Volatile
    var captureStackTraces = false

    var serverStderr: InputStream? = null

}
