# -*- coding: UTF-8 -*-
"""
WebSocketのサーバプログラム
"""

import asyncio
import websockets
import numpy as np

POSITION = [0.0, 0.0, 0.0]
ANGLE = [0.0, 0.0, 0.0]
ACC_DATA = [0.0, 0.0, 0.0]
GYRO_DATA = [0.0, 0.0, 0.0]


# クライエントから送られてきたデータを格納する
async def setData(websocket, data_type, data):
    print('Run setData() : {}'.format(data))
    greeting = 'Data Set, OK'
    data = [float(x.strip()) for x in data.split(',')]
    try:
        if data_type == 'POS':
            for i in range(3):
                POSITION[i] = data[i]
        elif data_type == 'ANG':
            for i in range(3):
                ANGLE[i] = data[i]
        elif data_type == 'ACC':
            for i in range(3):
                ACC_DATA[i] = data[i]
        elif data_type == 'GYR':
            for i in range(3):
                GYRO_DATA[i] = data[i]
        else:
            print('setData Error')
            greeting = 'Data Set, Miss'
        await websocket.send(greeting)
    except:
        print('Error setData()')
        await websocket.send('Error setData()')

        
# 格納されているデータをクライエントに送る
async def getData(websocket, data_type):
    print('Run getData')
    try:
        if data_type == 'POS':
            greeting = f'{data_type}:{POSITION[0]},{POSITION[1]},{POSITION[2]}'
            await websocket.send(greeting)
        elif data_type == 'ANG':
            greeting = f'{data_type}:{ANGLE[0]},{ANGLE[1]},{ANGLE[2]}'
            await websocket.send(greeting)
        elif data_type == 'ACC':
            greeting = f'{data_type}:{ACC_DATA[0]},{ACC_DATA[1]},{ACC_DATA[2]}'
            await websocket.send(greeting)
        elif data_type == 'GYR':
            greeting = f'{data_type}:{GYRO_DATA[0]},{GYRO_DATA[1]},{GYRO_DATA[2]}'
            await websocket.send(greeting)
        else:
            print('getData Error')
            greeting = 'Data Get, Miss'
            await websocket.send(greeting)
    except:
        print('Error getData()')
        await websocket.send('Error getData()')

        
# クライエントと接続した時
async def connect(websocket):
    try:
        print('New Client Connect')
        greeting = 'You Connect'
        await websocket.send(greeting)
    except:
        print('Error connect()')

        
# デバック用。エコー関数
async def echo(websocket, message):
    try:
        print('Run echo()')
        greeting = message
        await websocket.send(greeting)
    except:
        print('Error echo()')

        
# メイン処理。サーバの振る舞いを記述
async def handler(websocket):
    while True:
        try:
            message = await websocket.recv()  # ここでクライアントメッセージを受信したので、これ以降でwebsocket.recv()するとsend待ちになる
            order, data_type, data = message.split(':')
            if order == 'SET':
                await setData(websocket, data_type, data)
            elif order == 'GET':
                await getData(websocket, data_type)
            elif order == 'CONNECT':
                await connect(websocket)
            else:
                await echo(websocket, message)

        except websockets.ConnectionClosedOK:
            break

# 以下変更不可
async def main():
    async with websockets.serve(handler, "kumapi.local", 8765):
        await asyncio.Future()  # run forever


if __name__ == "__main__":
    print("Server On!!")
    asyncio.run(main())
