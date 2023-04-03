import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class FREWebSocketClient(uri: URI) : WebSocketClient(uri) {
    var pos = FloatArray(3)
    var ang = FloatArray(3)
    var acc_data = FloatArray(3) // [g]
    var gyro_data = FloatArray(3) // [deg/s]

    override fun onOpen(handshakedata: ServerHandshake?) {
        send("CONNECT:CHECK:OK")
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        println("closed")
    }

    override fun onMessage(message: String?) {
        val rec_message = message?.split(':', ',')
        val data_type : String? = rec_message?.get(0)

        if (data_type.equals("POS")){
            for (i in 0..2){
                if (rec_message != null) {
                    pos[i] = rec_message[i + 1].toFloat()
                }
            }
        }
        else if (data_type.equals("ANG")){
            for (i in 0..2){
                if (rec_message != null) {
                    ang[i] = rec_message[i + 1].toFloat()
                }
            }
        }
        else if (data_type.equals("ACC")){
            for (i in 0..2){
                if (rec_message != null) {
                    acc_data[i] = rec_message[i + 1].toFloat()
                }
            }
        }
        else if (data_type.equals("GYR")){
            for (i in 0..2){
                if (rec_message != null) {
                    gyro_data[i] = rec_message[i + 1].toFloat()
                }
            }
        }
        else{
            println(message.toString())
        }
    }

    override fun onError(ex: Exception?) {
        println("Error")
    }
}
