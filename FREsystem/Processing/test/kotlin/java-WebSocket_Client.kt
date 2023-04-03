/**
 * WebSocketを使用したProcessing Client
 * Raspi上のServerからセンサ値を取得して可視化する
 * Processing 3.3.7
 * Ktor 2.0.0
 */
import java.net.URI
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

fun main(){
    val uri = URI("ws://kumapi.local:8765/")
    val client = MyWebSocketClient(uri)

    // 連続で処理される場合、connet()だとエラーが起きる
    client.connectBlocking()
    Thread.sleep(10000)
    client.send("Hello")
    client.close()
}

class MyWebSocketClient(uri: URI) : WebSocketClient(uri) {

    override fun onOpen(handshakedata: ServerHandshake?) {
        send("Connect")
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        println("closed")
    }

    override fun onMessage(message: String?) {
        println(message)
    }

    override fun onError(ex: Exception?) {
        println("Error")
    }
}
