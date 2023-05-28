# FRE Project
博士研究テーマ  
釣り竿効果(Fishing Rod Effect)と呼ぶ錯覚を提唱し、それを実証すると共にVR向けインタフェースへの応用を目指す研究での成果物  



https://user-images.githubusercontent.com/116449282/229969163-7e8f4642-daf0-4f51-bad5-556606012812.mp4  



https://user-images.githubusercontent.com/116449282/229968886-4ff7c06e-71f4-44c8-870a-cb9983bfc11a.mp4



## FREdevice
作成したFRE装置に搭載されているサーボモータを制御する。  

## FREsystem
FRE装置をVRインタフェースとして活用するためのシステム。  
IMU(慣性計測ユニット)で推定した姿勢や位置を、VRアプリに送信することでFRE装置をコントローラとして扱えるようにする。  
Unityアプリでは、Arduinoを使用したシリアル通信システムを構築した。  
Processingでは、Raspberry Pi上に建てたWebSocketサーバを介してデータ通信を行うシステムを構築した。  

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
