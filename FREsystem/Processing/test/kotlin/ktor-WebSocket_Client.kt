import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.*

fun main() {
    val client = HttpClient {
        install(WebSockets)
    }
    runBlocking {
        client.webSocket(method = HttpMethod.Get, host = "kumapi.local", port = 8765, path = "/") {
            val myMessage = "Hello, Server"
            send(myMessage)
            val othersMessage = incoming.receive() as? Frame.Text
            if (othersMessage != null) {
                println(othersMessage.readText())
            }
        }
    }
    client.close()
    println("Connection closed. Goodbye!")
}
