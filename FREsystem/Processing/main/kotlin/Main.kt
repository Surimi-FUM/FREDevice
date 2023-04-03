/**
 * WebSocketを使用したProcessing Client
 * Raspi上のServerからセンサ値を取得して可視化する
 * Processing 3.3.7
 * Java-WebSocket 1.5.3
*/
import processing.core.PApplet
import java.net.URI


//メイン関数
fun main() {
    //SketchSampleクラスを呼び出す
    PApplet.main("FREsystem")
}

//PROCESSING用クラス
class FREsystem: PApplet() {
    lateinit var client: FREWebSocketClient

    // 画面サイズ設定
    override fun settings() {
        size(displayWidth / 2, displayHeight / 2, P3D)
    }

    // 初期処理関数
    override fun setup() {
        smooth()
        background(200)
        //frameRate(60f)
        val uri = URI("ws://kumapi.local:8765/")
        client = FREWebSocketClient(uri)
        client.connectBlocking()
    }

    // 描画処理関数
    override fun draw() {
        // 最初に背景色で塗りつぶすことで書き直し(refresh)を実装。これがないと前回描いたboxが消えない。
        background(200)
        client.send("GET:POS:0")
        lights()
        drawBoll()
        drawText()
    }

    fun drawBoll(){
        val pos = FloatArray(3)
        pushMatrix()
        // 移動処理。描画位置をずらしていくことで実装する
        pos[0] = width/2f + client.pos[0] * 100f
        pos[1] = height/2f - client.pos[1] * 100f
        pos[2] = 100f + client.pos[2] * 100f * 0f
        translate(pos[0], pos[1], pos[2])

        fill(200f, 50f, 50f)  // 図形の色指定(Red, Green, Blue)
        // 輪郭線がなく、中心が320, 240で、半径が150（＝直径が300）の球体を描く
        noStroke();
        sphere(50f);
        popMatrix()
    }

    fun drawText() {

    }

    // デコンストラクタ
    override fun dispose() {
        client.close()
        clear()
    }
}
