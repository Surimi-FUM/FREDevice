# FRE Project
博士研究テーマ  
釣り竿効果(Fishing Rod Effect)と呼ぶ錯覚を提唱し、それを実証すると共にVR向けインタフェースへの応用を目指す研究での成果物  

2022年開発  
https://user-images.githubusercontent.com/116449282/229968886-4ff7c06e-71f4-44c8-870a-cb9983bfc11a.mp4

2020年開発   
https://user-images.githubusercontent.com/116449282/229969163-7e8f4642-daf0-4f51-bad5-556606012812.mp4  

## FREdevice
作成したFRE装置に搭載されているサーボモータを制御する。  

## FREsystem
FRE装置をVRインタフェースとして活用するためのシステム。  
IMU(慣性計測ユニット)を使用して、FRE装置をコントローラとして扱えるようにする。  

### 2023年  
Unityで開発。  
実験装置4号機に適したVR体験の実装を目的に、5/25に開発開始。  
今後の予定：  
1.魚の実装、2.IMUセンサとのデータ通信、3.4号機による操作、4.ブラッシュアップ  

### 2022年  
Processingで開発。
マイコンをRaspberry Piに変更し、WebSocketサーバを介してデータ通信を行うシステムを構築した。  
研究室の仲間と協力して、ノイズフィルタを実装し、モーションキャプチャを実装した。  
軸ずれは完璧に抑えられなかったため、ボールの動く範囲を制限し、合成加速度が閾値を超えない動きは反映しないようにすることで、ずれを抑制する工夫をした。  

### 2020年  
Unityで開発。  
Arduinoとシリアル通信をして、IMUのセンサ値をアプリへ送信する。  
加速度センサの値が閾値を超えたとき、ユーザがパンチをしたと判定して、モデルのアニメーションを開始する。  
アニメーションが終わり切るまで次の動作を受け付けないようにして、誤作動を減らす工夫をした。  

## 開発環境  
PC：Windows 10 Education、Intel Core i7-7700K  
メモリ：32.0GB  
GPU：NVIDIA GeForce GTX 1080  
IDE：PyCharm Professional、Processing4、Unity  
言語：Python3、C#  
フレームワークなど：WebSocket  

### テスト環境
PC：Windows 10 Pro、Intel Core i7-6700K  
メモリ：16.0GB  
GPU：NVIDIA GeForce GTX 1660 Ti  

## 開発期間・人数
令和2年(2019)/09 ~ 現在、1人

## 今後の開発
・IMUによるモーションキャプチャの精度を向上させる  
・WebSocket規格の良さを活かした通信システムに再構築する  
・装置の活用方法を考え、それに合わせたアプリを開発する  
