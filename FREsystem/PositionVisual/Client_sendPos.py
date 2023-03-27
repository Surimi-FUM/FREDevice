# -*- coding: UTF-8 -*-
"""
WebSocketのクライエントプログラム
"""

import asyncio
import websockets
from BMX055Class import BMX055
import numpy as np
import time

async def SendData(dataType, data):
    uri = "ws://kumapi.local:8765/"
    message = f'SET:{dataType}:{data[0]},{data[1]},{data[2]}'
    async with websockets.connect(uri) as websocket:
        await websocket.send(message)
        print(await websocket.recv())

def main():
    sensor = BMX055()
    sensor.setup()

    # 計算用
    start_dt = time.perf_counter()
    lowpass_acc = [0, 0, 1]
    highpass_acc = [0, 0, 0]
    v = [0, 0, 0]
    x = [0, 0, 0]

    """
    ローパスの初期化
    ・初期化することで，ハイパスフィルタ値の初期値がきれいに調整される
    """
    acc = sensor.getAccel()
    acc_data = np.array(acc)
    for i in range(3):
        lowpass_acc[i] = acc_data[i]

    while True:
        # センサ値の取得
        acc = sensor.getAccel()
        gyro = sensor.getGyro()

        acc_data = np.array(acc)
        gyro_data = np.array(gyro)

        dt = time.perf_counter() - start_dt
        start_dt = time.perf_counter()

        """
        ローパス＆ハイパスフィルタ
        ・ローパスフィルタで重力成分を取り出し，それを元データから差し引く
        """
        fc = 0.25  # カットオフ周波数
        c = 2 * np.pi * fc
        K = (1 / c) / ((1 / c) + dt)
        for i, data in enumerate(acc_data):
            lowpass_acc[i] = K * lowpass_acc[i] + (1 - K) * data
            highpass_acc[i] = data - lowpass_acc[i]

        """
        合成加速度
        ・軸成分を排除した加速度データ。手振れなどの微小な揺れによるノイズを除去する
        """
        m = highpass_acc[0] ** 2 + highpass_acc[1] ** 2 + highpass_acc[2] ** 2
        resultant_acc = np.sqrt(m)

        if resultant_acc < 0.039:
            for i in range(3):
                highpass_acc[i] = 0

        """移動距離算出"""
        for i, acc in enumerate(highpass_acc):
            acc *= 9.81
            v[i] += acc * dt
            x[i] += v[i] * dt

        # asyncio.run(SendData('POS', x))
        asyncio.run(SendData('ACC', highpass_acc))
        # asyncio.run(SendData('GYR', gyro_data))


if __name__ == "__main__":
    main()
