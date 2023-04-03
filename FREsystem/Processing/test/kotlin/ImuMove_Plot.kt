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
    PApplet.main("ImuMove_Plot")
}

//PROCESSING用クラス
class ImuMove_Plot: PApplet() {
    lateinit var client: FREWebSocketClient
    lateinit var cp5: ControlP5
    lateinit var chart_accl: Chart
    lateinit var chart_gyro: Chart

    // (ローカル座標)位置推定用
    var local_velocity = FloatArray(3)
    var local_difference = FloatArray(3)
    var local_lowpassValue = FloatArray(3)
    var local_highpassValue = FloatArray(3)

    // (グローバル座標)位置推定用
    var global_velocity = FloatArray(3)
    var global_difference = FloatArray(3)
    var global_highpassValue = FloatArray(3)

    // 角度推定用
    var acc_rp_offset = FloatArray(2)
    var acc_offset_flag: Boolean = true
    var acc_rp = FloatArray(2)
    var gyro_rpy = FloatArray(3)
    var cf_rpy = FloatArray(3)

    // その他
    var local_pos = FloatArray(3)
    var global_pos = FloatArray(3)
    var K = 0.0f
    var base_time = 0
    var sampling_time = 0.0f
    val flag = 0
    var word = "none"

    // 画面サイズ設定
    override fun settings()  {
        size(displayWidth / 2,displayHeight / 2)
    }

    // 初期処理関数
    override fun setup() {
        smooth()
        background(100)
        frameRate(10F)
        base_time = millis()

        val uri = URI("ws://kumapi.local:8765/")
        client = FREWebSocketClient(uri)
        client.connectBlocking()

        cp5 = ControlP5(this)
        chart_accl = cp5.addChart("Local Values");
        chart_accl.setView(Chart.LINE)                             /* グラフの種類（折れ線グラフ） */
            .setRange(-2f, 2f)                         /* Y値の範囲（最小値、最大値） */
            .setSize(displayWidth / 2, 200)                               /* グラフの表示サイズ */
            .setPosition(0f, 0f)                            /* グラフの表示位置 */
            .setColorCaptionLabel(color(0,0,255))            /* キャプションラベルの色 */
            .setStrokeWeight(1.5f)                            /* グラフの線の太さを設定する */
            .color.background = color(224, 224, 224)  /* グラフの背景色を設定する */
        ;

        setupChartAttr(chart_accl, "LocalX", color(200, 0, 0))
        setupChartAttr(chart_accl, "LocalY", color(0, 200, 0))
        setupChartAttr(chart_accl, "LocalZ", color(0, 0, 200))

        chart_gyro = cp5.addChart("Global Values")
        chart_gyro.setView(Chart.LINE)
            .setRange(-300f, 300f)
            .setSize(displayWidth / 2, 200)                               /* グラフの表示サイズ */
            .setPosition(0f, 250f)                            /* グラフの表示位置 */
            .setColorCaptionLabel(color(0,0,255))
            .setStrokeWeight(1.5f)
            .color.background = color(224, 224, 224)

        setupChartAttr(chart_gyro, "GlobalX", color(200, 0, 0))
        setupChartAttr(chart_gyro, "GlobalY", color(0, 200, 0))
        setupChartAttr(chart_gyro, "GlobalZ", color(0, 0, 200))

        // テキストの設定
        word = "X:Red, Y:Green, Z:Blue"
        drawText(word, -330f)
    }

    // 描画処理関数
    override fun draw()  {
        client.send("GetImuValue")

        sampling_time = (millis() - base_time) / 1000.0f
        accPostureestimate()
        gyroPostureestimate()
        complementaryfilterPostureestimate()
        estimateLocalMove()
        estimateGlobalMove()
        base_time = millis()

        when (flag){
            0 -> {
                word = "Data is Acc"
                drawText(word, 0f)
                plotLGAcc()
            }
            1 -> {
                word = "Data is Difference"
                drawText(word, 0f)
                plotLGDifferences()
            }
            else -> {
                word = "Data is Position"
                drawText(word, 0f)
                plotLGPoses()
            }
        }
    }

    private fun accPostureestimate(){
        val acc = arrayOf(client.acc_data[0], client.acc_data[1], client.acc_data[2])

        if (acc_offset_flag){
            acc_rp_offset[0] = atan2(acc[1], sqrt( acc[0] * acc[0] + acc[2] * acc[2])) // roll [rad]
            acc_rp_offset[1] = atan2(-acc[0], sqrt( acc[1] * acc[1] + acc[2] * acc[2])) // pitch [rad]

            if (acc_rp_offset[0] != 0.0f){
                acc_offset_flag = false
            }
        }
        // acc_rp[0] = atan2(acc[1], acc[2]) // roll [rad]
        acc_rp[0] = atan2(acc[1], sqrt( acc[0] * acc[0] + acc[2] * acc[2])) - acc_rp_offset[0] // roll [rad]
        acc_rp[1] = atan2(-acc[0], sqrt( acc[1] * acc[1] + acc[2] * acc[2])) - acc_rp_offset[1] // pitch [rad]
    }

    private fun gyroPostureestimate(){
        val gyro = arrayOf(client.gyro_data[0], client.gyro_data[1], client.gyro_data[2])

        // 角速度を積分して角度に変換
        val dt = sampling_time
        gyro_rpy[0] += gyro[0] * dt * processing.core.PApplet.PI / 180f // roll [rad]
        gyro_rpy[1] += gyro[1] * dt * processing.core.PApplet.PI / 180f // pitch [rad]
        gyro_rpy[2] += gyro[2] * dt * processing.core.PApplet.PI / 180f // yaw [rad]
    }

    private fun complementaryfilterPostureestimate(){
        val gyro = arrayOf(client.gyro_data[0], client.gyro_data[1], client.gyro_data[2])
        val fc = 1.0f  // カット周波数 Hz ->100ぐらいためす
        val dt = sampling_time // サンプリングタイム s
        K = (1.0f / 2.0f * PI * fc) / ((1.0f / 2.0f * PI * fc) + dt)
        K = 0.5f

        cf_rpy[0] = K * (cf_rpy[0] + gyro[0] * dt * PI / 180f) + (1.0f - K) * acc_rp[0]
        cf_rpy[1] = K * (cf_rpy[1] + gyro[1] * dt * PI / 180f) + (1.0f - K) * acc_rp[1]
        cf_rpy[2] = gyro_rpy[2]
    }

    private fun estimateLocalMove(){
        val acc = floatArrayOf(client.acc_data[0], client.acc_data[1], client.acc_data[2])
        val dt = sampling_time // サンプリングタイム s

        // TODO 移動推定処理
        // ローパスフィルター(現在の値=係数 * ひとつ前の値 ＋ (1 - 係数) * センサの値)
        for ((num, data) in acc.withIndex()){
            // ローパスフィルター(現在の値=係数 * ひとつ前の値 ＋ (1 - 係数) * センサの値)
            local_lowpassValue[num] = local_lowpassValue[num] * dt + data * (1.0f - K)
            // ハイパスフィルター(センサの値 - ローパスフィルターの値)
            local_highpassValue[num] = data - local_lowpassValue[num]
            // v = at + v_o
            local_velocity[num] = local_highpassValue[num] * dt + local_velocity[num]
            // x = 1/2 * at^2 + vt
            local_difference[num] = (local_highpassValue[num] * dt * dt) / 2.0f + local_velocity[num] * dt
        }
    }

    private fun estimateGlobalMove(){
        global_highpassValue[0] = local_highpassValue[0] * cos(cf_rpy[1]) + local_highpassValue[2] * sin(cf_rpy[1])
        global_highpassValue[1] = local_highpassValue[0] * sin(cf_rpy[0]) * sin(cf_rpy[1]) +
                local_highpassValue[1] * cos(cf_rpy[1]) -
                local_highpassValue[2] * sin(cf_rpy[0]) * cos(cf_rpy[1])
        global_highpassValue[2] = -local_highpassValue[0] * cos(cf_rpy[0]) * sin(cf_rpy[1]) +
                local_highpassValue[1] * sin(cf_rpy[0]) * cos(cf_rpy[1]) +
                local_highpassValue[2] * cos(cf_rpy[0]) * cos(cf_rpy[1])

        val dt = sampling_time // サンプリングタイム s

        // TODO 移動推定処理
        // ローパスフィルター(現在の値=係数 * ひとつ前の値 ＋ (1 - 係数) * センサの値)
        for ((num, data) in global_highpassValue.withIndex()){
            // ローパスフィルター(現在の値=係数 * ひとつ前の値 ＋ (1 - 係数) * センサの値)
            // v = at + v_o
            global_velocity[num] = data * dt + global_velocity[num]
            // x = 1/2 * at^2 + vt
            global_difference[num] = (data * dt * dt) / 2.0f + global_velocity[num] * dt
        }
    }

    private fun plotLGPoses(){

        chart_accl.unshift("LocalX", local_pos[0])
        chart_accl.unshift("LocalY", local_pos[1])
        chart_accl.unshift("LocalZ", local_pos[2])

        chart_gyro.unshift("GlobalX", global_pos[0])
        chart_gyro.unshift("GlobalY", global_pos[1])
        chart_gyro.unshift("GlobalZ", global_pos[2])
    }

    private fun plotLGDifferences(){
        chart_accl.unshift("LocalX", local_difference[0])
        chart_accl.unshift("LocalY", local_difference[1])
        chart_accl.unshift("LocalZ", local_difference[2])

        chart_gyro.unshift("GlobalX", global_difference[0])
        chart_gyro.unshift("GlobalY", global_difference[1])
        chart_gyro.unshift("GlobalZ", global_difference[2])
    }

    private fun plotLGAcc(){
        chart_accl.unshift("LocalX", local_highpassValue[0])
        chart_accl.unshift("LocalY", local_highpassValue[1])
        chart_accl.unshift("LocalZ", local_highpassValue[2])

        chart_gyro.unshift("GlobalX", global_highpassValue[0])
        chart_gyro.unshift("GlobalY", global_highpassValue[1])
        chart_gyro.unshift("GlobalZ", global_highpassValue[2])
    }

    private fun drawText(word : String, num : Float) {
        // テキストの設定
        val H = 1.1f
        textSize(20f)
        text(word, width / 2f + num, height / H)
    }

    // デコンストラクタ
    override fun dispose() {
        client.showIMUdata()
        client.close()
    }

    private fun setupChartAttr(chart: Chart, axis_name: String?, col: Int) {
        chart.addDataSet(axis_name)
        chart.setData(axis_name, *FloatArray(200))
        chart.setColors(axis_name, col, color(255, 255, 128))
    }
}
