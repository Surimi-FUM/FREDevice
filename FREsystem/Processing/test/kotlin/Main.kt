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
    // (ローカル座標)位置推定用
    var local_lowpass_acc = FloatArray(3)
    var local_highpass_acc = FloatArray(3)
    var local_velocity = FloatArray(3)
    var local_difference = FloatArray(3)
    // (グローバル座標)位置推定用
    var global_lowpass_acc = FloatArray(3)
    var global_highpass_acc = FloatArray(3)
    var global_velocity = FloatArray(3)
    var global_difference = FloatArray(3)

    // 角度推定用
    var acc_angle_offset = FloatArray(2)
    var acc_offset_flag: Boolean = true
    var acc_angle = FloatArray(2)
    var gyro_angle = FloatArray(3)
    var angle = FloatArray(3)
    var lowpass_gyro = FloatArray(3)
    var highpass_gyro = FloatArray(3)

    // その他
    var global_acc = FloatArray(3)
    var local_pos = FloatArray(3)
    var global_pos = FloatArray(3)
    var K = 0.0f
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
        client.send("GetImuValue")

        sampling_time = (millis() - base_time) / 1000.0f
        acc_postureEstimate()
        complementaryFilter_postureEstimate()
        estimateLocalMove()
        estimateGlobalMove()
        base_time = millis()

        lights()
        drawLocalBoll()
        drawGlobalBoll()
        drawText()
    }

    fun drawLocalBoll(){
        pushMatrix()
        // 移動処理。描画位置をずらいしていくことで実装する
        local_pos[0] = (width/2f - 100f) + local_difference[0] * 50000f
        local_pos[1] = height/2f - local_difference[2] * 1000f
        local_pos[2] = 100f + local_difference[1] * 0f

        if (local_pos[0] >= width) local_pos[0] = width.toFloat()
        if (local_pos[0] <= 0) local_pos[0] = 0f
        if (local_pos[1] >= height) local_pos[1] = height.toFloat()
        if (local_pos[1] <= 0) local_pos[1] = 0f

        translate(local_pos[0], local_pos[1], local_pos[2])

        fill(200f, 50f, 50f)  // 図形の色指定(Red, Green, Blue)
        // 輪郭線がなく、中心が320, 240で、半径が150（＝直径が300）の球体を描く
        noStroke();
        sphere(50f);
        popMatrix()
    }

    fun drawGlobalBoll(){
        pushMatrix()
        // 移動処理。描画位置をずらいしていくことで実装する
        global_pos[0] = (width/2f + 100f) + global_difference[0] * 50000f
        global_pos[1] = height/2f - global_difference[2] * 1000f
        global_pos[2] = 100f + global_difference[1] * 0f

        if (global_pos[0] >= width) {global_pos[0] = width.toFloat()}
        if (global_pos[0] <= 0) {global_pos[0] = 0f}
        if (global_pos[1] >= height) {global_pos[1] = height.toFloat()}
        if (global_pos[1] <= 0) {global_pos[1] = 0f}

        translate(global_pos[0], global_pos[1], global_pos[2])

        fill(50f, 200f, 50f)  // 図形の色指定(Red, Green, Blue)
        // 輪郭線がなく、中心が320, 240で、半径が150（＝直径が300）の球体を描く
        noStroke();
        sphere(50f);
        popMatrix()
    }

    fun acc_postureEstimate(){
        val acc = arrayOf(client.acc_data[0], client.acc_data[1], client.acc_data[2])

        if (acc_offset_flag){
            acc_angle_offset[0] = atan2(acc[1], sqrt( acc[0] * acc[0] + acc[2] * acc[2])) // roll [rad]
            acc_angle_offset[1] = atan2(-acc[0], sqrt( acc[1] * acc[1] + acc[2] * acc[2])) // pitch [rad]

            if (acc_angle_offset[0] != 0.0f){
                acc_offset_flag = false
            }
        }
        // acc_rp[0] = atan2(acc[1], acc[2]) // roll [rad]
        acc_angle[0] = atan2(acc[1], sqrt( acc[0] * acc[0] + acc[2] * acc[2])) - acc_angle_offset[0] // roll [rad]
        acc_angle[1] = atan2(-acc[0], sqrt( acc[1] * acc[1] + acc[2] * acc[2])) - acc_angle_offset[1] // pitch [rad]
    }

    fun gyro_postureEstimate(){
        val gyro = arrayOf(client.gyro_data[0], client.gyro_data[1], client.gyro_data[2])
        for ((num, data) in gyro.withIndex()){
            lowpass_gyro[num] = lowpass_gyro[num] * K + data * (1f - K)
            highpass_gyro[num] = data - lowpass_gyro[num]
        }

        // 角速度を積分して角度に変換
        val dt = sampling_time
        gyro_angle[0] += highpass_gyro[0] * dt * processing.core.PApplet.PI / 180f // roll [rad]
        gyro_angle[1] += highpass_gyro[1] * dt * processing.core.PApplet.PI / 180f // pitch [rad]
        gyro_angle[2] += highpass_gyro[2] * dt * processing.core.PApplet.PI / 180f // yaw [rad]
    }

    fun  complementaryFilter_postureEstimate(){
        val gyro = arrayOf(client.gyro_data[0], client.gyro_data[1], client.gyro_data[2])
        for ((num, data) in gyro.withIndex()){
            lowpass_gyro[num] = lowpass_gyro[num] * K + data * (1.0f - K)
            highpass_gyro[num] = data - lowpass_gyro[num]
        }
        val fc = 2.0f  // カット周波数 Hz ->100ぐらいためす
        val dt = sampling_time // サンプリングタイム s
        K = (1.0f / 2.0f * PI * fc) / ((1.0f / 2.0f * PI * fc) + dt)

        angle[0] = K * (angle[0] + highpass_gyro[0] * dt * PI / 180f) + (1.0f - K) * acc_angle[0]
        angle[1] = K * (angle[1] + highpass_gyro[1] * dt * PI / 180f) + (1.0f - K) * acc_angle[1]
        angle[2] += highpass_gyro[2] * dt * processing.core.PApplet.PI / 180f // yaw [rad]
    }

    fun estimateLocalMove(){
        val acc = floatArrayOf(client.acc_data[0], client.acc_data[1], client.acc_data[2])
        val dt = sampling_time // サンプリングタイム s

        // TODO 移動推定処理
        // ローパスフィルター(現在の値=係数 * ひとつ前の値 ＋ (1 - 係数) * センサの値)
        for ((num, data) in acc.withIndex()){
            local_lowpass_acc[num] = local_lowpass_acc[num] * K + data * (1.0f - K)
            local_highpass_acc[num] = data - local_lowpass_acc[num]
            // v = at + v_o
            local_velocity[num] = local_highpass_acc[num] * dt + local_velocity[num]
            // x = 1/2 * at^2 + vt
            local_difference[num] = (local_highpass_acc[num] * dt * dt) / 2.0f + local_velocity[num] * dt
        }
    }

    fun estimateGlobalMove(){
        val acc = floatArrayOf(client.acc_data[0], client.acc_data[1], client.acc_data[2])
        // 角度変数設定
        val sinRoll = sin(angle[0])
        val cosRoll = cos(angle[0])
        val sinPitch = sin(angle[1])
        val cosPitch = cos(angle[1])
        val sinYaw = sin(angle[2])
        val cosYaw = cos(angle[2])

        // センサ座標をグローバル座標に変換。A_globalXYZ = R(-yaw)*R(-roll)*R(-pitch)*A_localXYZ
        // X-Y軸回りの計算
        global_acc[0] = acc[0] * cosYaw * cosPitch + acc[1] * (cosYaw * sinPitch * sinRoll - sinYaw * cosRoll) + acc[2] * (cosYaw * sinPitch * cosRoll + sinYaw * sinRoll)
        global_acc[1] = acc[0] * sinYaw * cosPitch + acc[1] * (sinYaw * sinPitch * sinRoll + cosYaw * cosRoll) + acc[2] * (sinYaw * sinPitch * cosRoll - cosYaw * sinRoll)
        global_acc[2] = acc[0] * (-sinPitch) + acc[1] * cosPitch * sinRoll + acc[2] * cosPitch * cosRoll

        val dt = sampling_time // サンプリングタイム s
        // ローパスフィルター(現在の値=係数 * ひとつ前の値 ＋ (1 - 係数) * センサの値)
        for ((num, data) in global_acc.withIndex()){
            global_lowpass_acc[num] = global_lowpass_acc[num] * K + data * (1.0f - K)
            global_highpass_acc[num] = data - global_lowpass_acc[num]
            // ローパスフィルター(現在の値=係数 * ひとつ前の値 ＋ (1 - 係数) * センサの値)
            // v = at + v_o
            global_velocity[num] = global_highpass_acc[num] * dt + global_velocity[num]
            // x = 1/2 * at^2 + vt
            global_difference[num] = (global_highpass_acc[num] * dt * dt) / 2.0f + global_velocity[num] * dt
        }
    }

    fun drawText(){
        var H = 0.0f
        var word: String
        for ((num, data) in client.acc_data.withIndex()){
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
            fill(50f, 50f, 50f)
            text(word, width / 2f-330f, height/ H)
        }

        for ((num, pos) in local_highpass_acc.withIndex()){
            H = 8f - num * 2f
            if (num == 0){
                word = "X = ${pos.toString()}"
            }
            else if(num == 1){
                word = "Y = ${pos.toString()}"
            }
            else{
                word = "Z = ${pos.toString()}"
                H = 4.7f
            }
            fill(0)
            text(word, width / 2f-70f, height/ H)
        }

        for ((num, pos) in global_highpass_acc.withIndex()){
            H = 8f - num * 2f
            if (num == 0){
                word = "X = ${pos.toString()}"
            }
            else if(num == 1){
                word = "Y = ${pos.toString()}"
            }
            else{
                word = "Z = ${pos.toString()}"
                H = 4.7f
            }
            fill(0)
            text(word, width / 2f+70f, height/ H)
        }
    }

    // デコンストラクタ
    override fun dispose() {
        kotlin.io.println(K)
        client.showIMUdata()
        client.close()
        clear()
    }
}
