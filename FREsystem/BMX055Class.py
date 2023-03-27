"""
BMX055のクラス
・ジャイロ設定：スケールレンジ=±125, 出力レート=100Hz, フィルタ帯域幅=32Hzが精度が良い
・最小設定が一番バランスが良いと思われる
"""
import numpy as np
import pigpio
import time


class BMX055:
    acc_addr = 0x19
    accel = None
    acc_register_addr = 0x02
    gyro_addr = 0x69
    gyro = None
    gyro_register_addr = 0x02
    mag_addr = 0x13
    mag = None
    mag_register_addr = 0x42

    pi = pigpio.pi()
    i2c_bus = 1

    def __del__(self):
        self.stop()

    def setup(self):
        # 加速度センサーの設定
        self.accel = self.open(self.acc_addr)
        # Select PMU_Range register, 0x0F(15)
        #       0x03(03)    加速度計の重力範囲(Range) = +/- 2g
        self.pi.i2c_write_byte_data(self.accel, 0x0F, 0x03)
        time.sleep(0.1)
        # Select PMU_BW register, 0x10(16)
        #       0x08(08)    加速データフィルタの帯域幅(Bandwidth) = 7.81 Hz
        self.pi.i2c_write_byte_data(self.accel, 0x10, 0x08)
        time.sleep(0.1)
        # Select PMU_LPW register, 0x11(17)
        #       0x00(00)    メイン電力モードと低電力スリープ期間(Normal mode, Sleep duration) = 0.5ms
        self.pi.i2c_write_byte_data(self.accel, 0x11, 0x00)
        time.sleep(0.1)

        # ジャイロセンサーの設定
        self.gyro = self.open(self.gyro_addr)
        # Select Range register, 0x0F(15)
        #       0x04(100)    Full scale = +/- 125 degree/s
        self.pi.i2c_write_byte_data(self.gyro, 0x0F, 0x04)
        time.sleep(0.1)
        # Select Bandwidth register, 0x10(16)
        #       0x07(0111)    出力レート(ODR) = 100 Hz フィルタ帯域幅 = 32 Hz
        self.pi.i2c_write_byte_data(self.gyro, 0x10, 0x07)
        time.sleep(0.1)
        # Select LPM1 register, 0x11(17)
        #       0x00(00)    Normal mode, Sleep duration = 2ms
        self.pi.i2c_write_byte_data(self.gyro, 0x11, 0x00)
        time.sleep(0.1)

        # mag_data_setup : 地磁気値をセットアップ
        self.mag = self.open(self.mag_addr)
        data = self.pi.i2c_read_byte_data(self.mag, 0x4B)
        if data == 0:
            # Soft reset
            self.pi.i2c_write_byte_data(self.mag, 0x4B, 0x83)
            time.sleep(0.1)
        # Soft reset
        self.pi.i2c_write_byte_data(self.mag, 0x4B, 0x01)
        time.sleep(0.1)
        # Normal Mode, ODR = 10 Hz
        self.pi.i2c_write_byte_data(self.mag, 0x4C, 0x00)
        time.sleep(0.1)
        # X, Y, Z-Axis enabled
        self.pi.i2c_write_byte_data(self.mag, 0x4E, 0x84)
        time.sleep(0.1)
        # No. of Repetitions for X-Y Axis = 9
        self.pi.i2c_write_byte_data(self.mag, 0x51, 0x04)
        time.sleep(0.1)
        # No. of Repetitions for Z-Axis = 15
        self.pi.i2c_write_byte_data(self.mag, 0x52, 0x16)
        time.sleep(0.1)

    def open(self, i2c_addr):
        return self.pi.i2c_open(self.i2c_bus, i2c_addr)

    def close(self, handle):
        self.pi.i2c_close(handle)

    def stop(self):
        self.close(self.accel)
        self.close(self.mag)
        self.close(self.gyro)
        self.pi.stop()

    def getAccel(self):
        accData = [0, 0, 0, 0, 0, 0]
        value = [0.0, 0.0, 0.0]
        for i in range(6):
            accData[i] = self.pi.i2c_read_byte_data(self.accel, self.acc_register_addr + i)

        for i in range(3):
            value[i] = ((accData[2 * i + 1] * 256) + int(accData[2 * i] & 0xF0)) / 16
            if value[i] > 2047:
                value[i] -= 4096
            value[i] *= 0.00098  # range = +/-2g
        return value

    def getGyro(self):
        gyrData = [0, 0, 0, 0, 0, 0]
        value = [0.0, 0.0, 0.0]
        for i in range(6):
            gyrData[i] = self.pi.i2c_read_byte_data(self.gyro, self.gyro_register_addr + i)

        for i in range(3):
            value[i] = (gyrData[2 * i + 1] * 256) + gyrData[2 * i]
            if value[i] > 32767:
                value[i] = value[i] - 65536
            value[i] *= 0.0038  # 角速度範囲 ±125 deg/s => 角速度分解能 3.8 mdeg/s
        return value

    def getMag(self):
        magData = [0, 0, 0, 0, 0, 0, 0, 0]
        value = [0.0, 0.0, 0.0]
        for i in range(8):
            magData[i] = self.pi.i2c_read_byte_data(self.mag, self.mag_register_addr + i)

        for i in range(3):
            if i != 2:
                value[i] = ((magData[2 * i + 1] * 256) + (magData[2 * i] & 0xF8)) / 8
                if value[i] > 4095:
                    value[i] = value[i] - 8192
            else:
                value[i] = ((magData[2 * i + 1] * 256) | (magData[2 * i] & 0xF8)) / 2
                if value[i] > 16383:
                    value[i] = value[i] - 32768
        return value


if __name__ == "__main__":
    sensor = BMX055()
    sensor.setup()

    angle = [0.0, 0.0, 0.0]
    # 時間計測
    elapsedTime = 0.0  # 経過時間
    startTime = time.perf_counter()  # 計測開始

    rooptime = 0.0
    roopstart = time.perf_counter()
    while True:
        try:
            # 100秒経ったら修了する
            rooptime = time.perf_counter() - roopstart
            if rooptime >= 100.00:
                break

            # センサ値の取得
            gyro = sensor.getGyro()

            # 角度算出 (elapsedTimeで計算すると、おかしな結果になる)
            gyro = np.array(gyro)
            elapsedTime = time.perf_counter() - startTime

            angle[0] += gyro[0] * elapsedTime
            angle[1] += gyro[1] * elapsedTime
            angle[2] += gyro[2] * elapsedTime

            startTime = time.perf_counter()

            time.sleep(0.01)
            print("\r{}: R={}, P={}, Y={}, etime={}".format(round(rooptime, 2),
                                                            round(angle[0], 2),
                                                            round(angle[1], 2),
                                                            round(angle[2], 2),
                                                            round(elapsedTime, 2)), end="")  # プログラム監視

        except KeyboardInterrupt:
            exit()
