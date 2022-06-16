package info.voidev.lspidea.lspex

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.InstanceCreator
import info.voidev.lspidea.lspex.debug.DebugMessageConsumer
import info.voidev.lspidea.lspex.debug.JrpcDebugger
import info.voidev.lspidea.lspex.debug.JrpcMessageDirection
import org.eclipse.lsp4j.ServerCapabilities
import org.eclipse.lsp4j.jsonrpc.Launcher
import org.eclipse.lsp4j.jsonrpc.MessageConsumer
import org.eclipse.lsp4j.jsonrpc.RemoteEndpoint
import org.eclipse.lsp4j.jsonrpc.json.StreamMessageConsumer
import org.eclipse.lsp4j.jsonrpc.validation.ReflectiveMessageValidator
import org.eclipse.lsp4j.launch.LSPLauncher
import org.eclipse.lsp4j.services.LanguageClient
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Type

/**
 * @see LSPLauncher
 */
object LSPLauncherEx {

    /**
     * Create a new Launcher for a language client and an input and output stream, and set up message validation and tracing.
     *
     * @param client the client that receives method calls from the remote server
     * @param in input stream to listen for incoming messages
     * @param out output stream to send outgoing messages
     * @param validate whether messages should be validated with the [ReflectiveMessageValidator]
     * @param trace a writer to which incoming and outgoing messages are traced, or `null` to disable tracing
     *
     * @see LSPLauncher.createClientLauncher
     */
    fun createClientLauncher(
        client: LanguageClient,
        `in`: InputStream, out: OutputStream,
        validate: Boolean,
        setDebugger: ((JrpcDebugger) -> Unit)? = null,
        setGson: ((Gson) -> Unit)? = null,
    ): Launcher<LanguageServerEx> {
        return BuilderEx<LanguageServerEx>(setDebugger)
            .setLocalService(client)
            .setRemoteInterface(LanguageServerEx::class.java)
            .setInput(`in`)
            .setOutput(out)
            .validateMessages(validate)
            .configureGson {
                configureGson(it)
                setGson?.invoke(it.create())
            }
            .create()
    }

    private fun configureGson(gson: GsonBuilder) {
        gson.registerTypeAdapter(ServerCapabilities::class.java, object : InstanceCreator<ServerCapabilities> {
            override fun createInstance(t: Type?): ServerCapabilities {
                // This does nothing, but if we were to return a subtype here,
                // that's how we could add more fields to existing types (probably)
                return ServerCapabilities()
            }
        })
    }

    private class BuilderEx<T>(
        setDebugger: ((JrpcDebugger) -> Unit)?,
    ) : Launcher.Builder<T>() {

        private val debugger: JrpcDebugger?

        init {
            if (setDebugger == null) {
                debugger = null
            } else {
                debugger = JrpcDebugger()
                (setDebugger)(debugger)
            }
        }

        override fun wrapMessageConsumer(consumer: MessageConsumer): MessageConsumer {
            val direction = when (consumer) {
                is StreamMessageConsumer -> JrpcMessageDirection.SENT
                is RemoteEndpoint -> JrpcMessageDirection.RECEIVED
                else -> null
            }
            return super.wrapMessageConsumer(debugger?.let { DebugMessageConsumer(it, consumer, direction) } ?: consumer)
        }
    }

}
