from DeviceClass import Servo
import time

def tanpatu():
    servo = Servo(pin=12)

    def run_servo(degree, sleep):
        servo_ang, spw = servo.move(degree=degree, log=True)
        print("ang={}, apw={}".format(servo_ang, spw))
        time.sleep(sleep)

    run_servo(0, 2)
    text = ''
    while True:
        text = input('入力 (-1 = 終了) ->')
        if int(text) == -1:
            break
        run_servo(int(text), 0.2)
    run_servo(0, 2)
    servo.stop()


def main():
    servo = Servo(pin=12)

    def run_servo(degree, sleep):
        servo_ang, spw = servo.move(degree=degree, log=True)
        print("ang={}, apw={}".format(servo_ang, spw))
        time.sleep(sleep)

    run_servo(0, 2)
    start = time.perf_counter()
    while True:
        check = time.perf_counter() - start
        if check > 10.0:
            break
        run_servo(90, 0.3)
        run_servo(0, 0.5)
    print('A')
    run_servo(0, 2)
    servo.stop()


if __name__ == '__main__':
    main()
    # tanpatu()
    print('Fin.')
