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
    PApplet.main("IMUvalue_Plot")
}

//PROCESSING用クラス
class IMUvalue_Plot: PApplet() {
    lateinit var client: FREWebSocketClient
    lateinit var cp5: ControlP5
    lateinit var chart_accl: Chart
    lateinit var chart_gyro: Chart

    // 画面サイズ設定
    override fun settings()  {
        size(displayWidth / 2,displayHeight / 2)
    }

    // 初期処理関数
    override fun setup() {
        smooth()
        background(100)
        frameRate(10F)

        val uri = URI("ws://kumapi.local:8765/")
        client = FREWebSocketClient(uri)
        client.connectBlocking()

        cp5 = ControlP5(this)
        // 加速度のグラフ設定
        chart_accl = cp5.addChart("Accl sensor");
        chart_accl.setView(Chart.LINE)                             /* グラフの種類（折れ線グラフ） */
            .setRange(-2f, 2f)                         /* Y値の範囲（最小値、最大値） */
            .setSize(displayWidth / 2, 200)                               /* グラフの表示サイズ */
            .setPosition(0f, 0f)                            /* グラフの表示位置 */
            .setColorCaptionLabel(color(0,0,255))            /* キャプションラベルの色 */
            .setStrokeWeight(1.5f)                            /* グラフの線の太さを設定する */
            .color.background = color(224, 224, 224)  /* グラフの背景色を設定する */
        ;

        setupChartAttr(chart_accl, "AcclX", color(200, 0, 0))
        setupChartAttr(chart_accl, "AcclY", color(0, 200, 0))
        setupChartAttr(chart_accl, "AcclZ", color(0, 0, 200))

        // ジャイロのグラフ設定
        chart_gyro = cp5.addChart("Gyro sensor")
        chart_gyro.setView(Chart.LINE)
            .setRange(-300f, 300f)
            .setSize(displayWidth / 2, 200)                               /* グラフの表示サイズ */
            .setPosition(0f, 250f)                            /* グラフの表示位置 */
            .setColorCaptionLabel(color(0,0,255))
            .setStrokeWeight(1.5f)
            .color.background = color(224, 224, 224)

        setupChartAttr(chart_gyro, "GyroX", color(200, 0, 0))
        setupChartAttr(chart_gyro, "GyroY", color(0, 200, 0))
        setupChartAttr(chart_gyro, "GyroZ", color(0, 0, 200))

        // テキストの設定
        val word: String = "X:Red, Y:Green, Z:Blue"
        val H = 1.1f
        textSize(20f)
        text(word, width / 2f-330f, height/ H)
    }

    // 描画処理関数
    override fun draw()  {
        client.send("GetImuValue")
        // setPlotdata()

        chart_accl.unshift("AcclX", client.acc_data[0].toFloat());
        chart_accl.unshift("AcclY", client.acc_data[1].toFloat());
        chart_accl.unshift("AcclZ", client.acc_data[2].toFloat());

        chart_gyro.unshift("GyroX", client.gyro_data[0].toFloat());
        chart_gyro.unshift("GyroY", client.gyro_data[1].toFloat());
        chart_gyro.unshift("GyroZ", client.gyro_data[2].toFloat());
    }

    // デコンストラクタ
    override fun dispose() {
        client.showIMUdata()
        client.close()
    }

    fun setupChartAttr(chart: Chart, axis_name: String?, col: Int) {
        chart.addDataSet(axis_name)
        chart.setData(axis_name, *FloatArray(200))
        chart.setColors(axis_name, col, color(255, 255, 128))
    }
}
