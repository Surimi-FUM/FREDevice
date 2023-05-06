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
    PApplet.main("ImuMove_visual")
}

//PROCESSING用クラス
class ImuMove_visual: PApplet() {
    lateinit var client: FREWebSocketClient
    var acc = FloatArray(3)
    var velocity = FloatArray(3)
    var difference = FloatArray(3)
    var lowpassValue = FloatArray(3)
    var highpassValue = FloatArray(3)
    var K = 0.0f
    var pos = FloatArray(3)

    var base_time = 0
    var sampling_time = 0.0f

    // 画面サイズ設定
    override fun settings()  {
        size(displayWidth / 2,displayHeight / 2, P3D)
    }

    // 初期処理関数
    override fun setup() {
        smooth()
        background(200)
        //frameRate(60f)
        val uri = URI("ws://kumapi.local:8765/")
        client = FREWebSocketClient(uri)
        client.connectBlocking()
        base_time = millis()
    }

    // 描画処理関数
    override fun draw()  {
        // 最初に背景色で塗りつぶすことで書き直し(refresh)を実装。これがないと前回描いたboxが消えない。
        background(200)
        client.send("GET:ACC:0")
        client.send("GET:GYR:0")

        sampling_time = (millis() - base_time) / 1000.0f
        estimateMove()
        base_time = millis()

        lights()
        drawBoll()
        drawText()
        kotlin.io.println("K=${K}")
    }
    
    // 操作対象であるボールを描画する
    fun drawBoll(){
        pushMatrix()
        // 移動処理。描画位置をずらしていくことで実装する
        pos[0] = width/2f + difference[0] * 10000f * 0f
        pos[1] = height/2f - difference[2] * 1000f
        pos[2] = 100f + difference[1] * 1000f * 0f
        translate(pos[0], pos[1], pos[2])

        fill(200f, 50f, 50f)  // 図形の色指定(Red, Green, Blue)
        // 輪郭線がなく、中心が320, 240で、半径が150（＝直径が300）の球体を描く
        noStroke();
        sphere(50f);
        popMatrix()
    }

    // センサ値から位置を推定する
    fun estimateMove(){
        acc = floatArrayOf(client.acc_data[0], client.acc_data[1], client.acc_data[2])
        val fc = 1.0f  // カット周波数
        val dt = sampling_time // サンプリングタイム s
        K = (1.0f / 2.0f * PI * fc) / ((1.0f / 2.0f * PI * fc) + dt)

        // TODO 移動推定処理
        // ローパスフィルター(現在の値=係数 * ひとつ前の値 ＋ (1 - 係数) * センサの値)
        for ((num, data) in acc.withIndex()){
            // ローパスフィルター(現在の値=係数 * ひとつ前の値 ＋ (1 - 係数) * センサの値)
            lowpassValue[num] = lowpassValue[num] * sampling_time + data * (1.0f - K)
            // ハイパスフィルター(センサの値 - ローパスフィルターの値)
            highpassValue[num] = data - lowpassValue[num]
            // 速度計算(加速度を台形積分する)
            velocity[num] = highpassValue[num] * sampling_time + velocity[num]
            // 変位計算(速度を台形積分する)
            difference[num] = (highpassValue[num] * sampling_time * sampling_time) / 2.0f + velocity[num] * sampling_time
        }
    }

    // デバック用。センサ値と推定値の変化を可視化する
    fun drawText(){
        var H = 1.0f
        var word: String
        for ((num, data) in acc.withIndex()){
            H = 8f - num * 2f
            if (num == 0){
                word = "Accel_X = ${data.toString()}"
            }
            else if(num == 1){
                word = "Accel_Y = ${data.toString()}"
            }
            else{
                word = "Accel_Z = ${data.toString()}"
                H = 4.7f
            }
            text(word, width / 2f-330f, height/ H)
        }

        for ((num, data) in pos.withIndex()){
            H = 8f - num * 2f
            if (num == 0){
                word = "Pos_X = ${data.toString()}"
            }
            else if(num == 1){
                word = "Pos_Y = ${data.toString()}"
            }
            else{
                word = "Pos_Z = ${data.toString()}"
                H = 4.7f
            }
            text(word, width / 2f-70f, height/ H)
        }

        for ((num, data) in difference.withIndex()){
            H = 8f - num * 2f
            if (num == 0){
                word = "Difference_X = ${data.toString()}"
            }
            else if(num == 1){
                word = "Difference_Y = ${data.toString()}"
            }
            else{
                word = "Difference_Z = ${data.toString()}"
                H = 4.7f
            }
            text(word, width / 2f+200f, height/ H)
        }
    }

    // デコンストラクタ
    override fun dispose() {
        client.close()
        clear()
    }
}
