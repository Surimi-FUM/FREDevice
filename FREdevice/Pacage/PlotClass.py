# -*- coding: UTF-8 -*-
"""
Raspberry Piプログラミング
・センサー値を受け取り、リアルタイムでプロットしていく
引数：センサー値(y軸)、時間(x軸)
"""
import matplotlib.pyplot as plt
import numpy as np

class PlotManager:
    def __init__(self):
        self.fig, self.ax = plt.subplots(1, 1)
        self.y = np.zeros(10)
        self.x = np.zeros(10)

        # 初期化的に一度plotしなければならない.そのときplotしたオブジェクトを受け取る受け取る必要がある．listが返ってくるので，注意
        self.lines, = self.ax.plot(self.x, self.y)

        plt.title('MPU6050 Value')  # タイトル
        plt.xlabel('t')  # 横軸のラベル名
        plt.ylabel('accel')  # 縦軸のラベル名
        plt.grid()  # グリッド表示
        print(self.x, '\n', self.y)

    def plot(self, time, sensor_value):
        self.x = np.append(self.x, time)
        self.y = np.append(self.y, sensor_value)
        # 描画データを更新するときにplot関数を使うと
        # lineオブジェクトが都度増えてしまうので，注意．
        # 一番楽なのは上記で受け取ったlinesに対して
        # set_data()メソッドで描画データを更新する方法．
        self.lines.set_data(self.x, self.y)

        # set_data()を使うと軸とかは自動設定されないっぽいので，
        # 今回の例だとあっという間にsinカーブが描画範囲からいなくなる．
        # そのためx軸の範囲は適宜修正してやる必要がある．
        # self.ax.set_xlim((self.x.min(), self.x.max()))

        # 一番のポイント
        # - plt.show() ブロッキングされてリアルタイムに描写できない
        # - plt.ion() + plt.draw() グラフウインドウが固まってプログラムが止まるから使えない
        # ----> plt.pause(interval) これを使う!!! 引数はsleep時間
        plt.pause(.01)


if __name__ == '__main__':
    ploter = PlotManager()
    for i in range(100):
        y = np.sin(i)
        ploter.plot(i, y)
    print('{}\n{}'.format(ploter.x, ploter.y))
