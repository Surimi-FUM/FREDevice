# -*- coding: UTF-8 -*-
"""
WebSocketのサーバプログラム
"""

import asyncio
import websockets
import numpy as np

async def echo(websocket, message):
    try:
        print('Run echo()')
        greeting = f'Received invalid message: {message}\nThe following keywords are valid: 「IMU」, 「GetImuValue」'
        await websocket.send(greeting)
    except:
        print('Error echo()')

async def handler(websocket):
    while True:
        try:
            message = await websocket.recv()  # ここでクライアントメッセージを受信したので、これ以降でwebsocket.recv()するとsend待ちになる
            await echo(websocket, message)

        except websockets.ConnectionClosedOK:
            break

async def main():
    async with websockets.serve(handler, "kumapi.local", 8765):
        await asyncio.Future()  # run forever


if __name__ == "__main__":
    print("Server On!!")
    asyncio.run(main())