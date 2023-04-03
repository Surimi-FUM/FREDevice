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
    PApplet.main("ImuDisp_Plot")
}

//PROCESSING用クラス
class ImuDisp_Plot: PApplet() {
    lateinit var client: FREWebSocketClient_recDisp
    var pos = FloatArray(3)
    var direction = floatArrayOf(1000f, 1000f, 0f)

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
        client = FREWebSocketClient_recDisp(uri)
        client.connectBlocking()

        pos = floatArrayOf(width / 2f, height / 2f, 100f)
    }

    // 描画処理関数
    override fun draw()  {
        // 最初に背景色で塗りつぶすことで書き直し(refresh)を実装。これがないと前回描いたboxが消えない。
        background(200)
        client.send("GetImuValue")

        lights()
        pushMatrix()

        calcPosition()
        translate(pos[0], pos[1], pos[2])

        fill(200f, 50f, 50f)  // 図形の色指定(Red, Green, Blue)
        // 輪郭線がなく、中心が320, 240で、半径が150（＝直径が300）の球体を描く
        noStroke();
        sphere(50f);
        popMatrix()

        drawText()
    }

    fun calcPosition(){
        // 移動処理。描画位置をずらしていくことで実装する
        pos[0] = (width / 2f) + client.displacement[0] * direction[0]
        pos[1] = (height / 2f) - client.displacement[2] * direction[1]
        pos[2] = 100f + client.displacement[1] * direction[2]

        if (pos[0].toInt() < 200) {
            pos[0] = 200f
        }else if (pos[0].toInt() > width - 200){
            pos[0] = width.toFloat() - 200f
        }

        if (pos[1].toInt() < 100) {
            pos[1] = 100f
        }else if (pos[1].toInt() > height - 20){
            pos[1] = height.toFloat() - 20f
        }
    }

    fun drawText() {
        var H = 0.0f
        var word: String
        for ((num, data) in pos.withIndex()) {
            H = 8f - num * 2f
            if (num == 0) {
                word = "Position X = ${data.toString()}"
            } else if (num == 1) {
                word = "Position Y = ${data.toString()}"
            } else {
                word = "Position Z = ${data.toString()}"
                H = 4.7f
            }
            fill(50f, 50f, 50f)
            text(word, width / 2f - 330f, height / H)
        }

        for ((num, pos) in client.displacement.withIndex()){
            H = 8f - num * 2f
            if (num == 0){
                word = "Displacement X = ${pos.toString()}"
            }
            else if(num == 1){
                word = "Displacement Y = ${pos.toString()}"
            }
            else{
                word = "Displacement Z = ${pos.toString()}"
                H = 4.7f
            }
            fill(0)
            text(word, width / 2f-70f, height/ H)
        }
    }

        // デコンストラクタ
    override fun dispose() {
        client.showIMUdata()
        client.close()
    }
}
