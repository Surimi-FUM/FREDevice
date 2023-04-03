import time
import pigpio


class Servo:
    """
    # 可動角: 180度
    # スピード: 0.1s / 60度
    # 電圧: 4.8 ～ 5V
    # パルス周期: 10,000 - 20,000μs
    # パルス幅: 500 - 2,500μs -> 個体差考慮： 550 ~ 2450
    # 入力値：-90 ~ 90 度
    """

    def __init__(self, pin=None):
        self.range_of_motion = 180
        self.min_pulse_width = 550
        self.max_pulse_width = 2450

        self.pig = pigpio.pi()
        self.pin = pin

    def move(self, degree, log=False):
        deg = degree
        if deg > 180:
            deg = 180
        elif deg < 0:
            deg = 0

        if self.pin is None:
            print("Pinを指定してください")
        else:
            spw = (deg / self.range_of_motion) * (self.max_pulse_width - self.min_pulse_width) + self.min_pulse_width
            self.pig.set_servo_pulsewidth(self.pin, spw)
            if log:
                return deg, spw

    def stop(self):
        self.pig.set_servo_pulsewidth(self.pin, 0)


if __name__ == '__main__':
    servo = Servo(pin=12)

    def run_servo(degree, sleep):
        servo_ang, spw = servo.move(degree=degree, log=True)
        print("ang={}, apw={}".format(servo_ang, spw))
        time.sleep(sleep)

    for i in range(10):
        run_servo(0, 0.3)
        run_servo(180, 1)

    for i in range(20):
        run_servo(180, 0.3)
        run_servo(0, 0.1)
    run_servo(0, 2)
    servo.stop()