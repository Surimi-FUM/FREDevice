/**
 * WebSocketを使用したProcessing Client
 * Raspi上のServerからセンサ値を取得して可視化する
 * Processing 3.3.7
 * Java-WebSocket 1.5.3
 */
import controlP5.Chart
import controlP5.ControlP5
import processing.core.PApplet
import java.net.URI


//メイン関数
fun main() {
    //SketchSampleクラスを呼び出す
    PApplet.main("ImuEKF_Plot")
}

//PROCESSING用クラス
class ImuEKF_Plot: PApplet() {
    lateinit var client: FREWebSocketClientRecEKF

    // 画面サイズ設定
    override fun settings()  {
        size(displayWidth / 2,displayHeight / 2, P3D)
    }

    // 初期処理関数
    override fun setup() {
        smooth()
        background(100)
        // frameRate(10F)

        val uri = URI("ws://kumapi.local:8765/")
        client = FREWebSocketClientRecEKF(uri)
        client.connectBlocking()
    }

    // 描画処理関数
    override fun draw()  {
        // 最初に背景色で塗りつぶすことで書き直し(refresh)を実装。これがないと前回描いたboxが消えない。
        background(200)
        client.send("GetImuValue")

        lights()
        pushMatrix()

        translate(width/2f, height/2f); // set position to centre

        applyMatrix( -1f, 0f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 0f, 1f);

        applyMatrix( client.Rxyz[0], client.Rxyz[1], client.Rxyz[2], 0f,
            client.Rxyz[3], client.Rxyz[4], client.Rxyz[5], 0f,
            client.Rxyz[6], client.Rxyz[7], client.Rxyz[8], 0f,
            0f, 0f, 0f, 1f);

        stroke(0f, 90f, 90f); // set outline colour to darker teal
        fill(0f, 130f, 130f); // set fill colour to lighter teal
        box(200f, 400f, 20f); // draw Arduino board base shape

        popMatrix()
    }

        // デコンストラクタ
    override fun dispose() {
        client.showIMUdata()
        client.close()
    }
}
