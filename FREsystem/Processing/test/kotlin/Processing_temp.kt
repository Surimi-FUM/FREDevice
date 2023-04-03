import processing.core.*

//メイン関数
fun main() {
    //SketchSampleクラスを呼び出す
    PApplet.main("SketchSample")
}

//PROCESSING用クラス
class SketchSample : PApplet() {
    //初期処理関数
    override fun settings()  {
        size(300,300)
    }
    override fun setup() {
    }
    //描画処理関数
    override fun draw()  {
        background(200)
    }
}
