# -*- coding: UTF-8 -*-
"""
WebSocketのクライエントプログラム
"""

import asyncio
import websockets
from BMX055Class import BMX055
import numpy as np
import time

async def Send(acc_data, gyro_data):
    uri = "ws://kumapi.local:8765/"
    message = f'IMU,{acc_data[0]},{acc_data[1]},{acc_data[2]},{gyro_data[0]},{gyro_data[1]},{gyro_data[2]}'
    print(message)
    async with websockets.connect(uri) as websocket:
        await websocket.send(message)
        print(await websocket.recv())

def main():
    sensor = BMX055()
    sensor.setup()
    while True:
        # センサ値の取得
        acc = sensor.getAccel()
        gyro = sensor.getGyro()

        acc_data = np.array(acc)
        gyro_data = np.array(gyro)

        asyncio.run(Send(acc_data, gyro_data))
        time.sleep(0.01)


if __name__ == "__main__":
    main()
