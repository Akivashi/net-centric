#include "mbed.h"

AnalogIn ain(p16);
AnalogIn change(p20);
DigitalOut left(p15);
DigitalOut right(p18);

int main() {
	DigitalOut leds[] = {(LED1), (LED2), (LED3), (LED4)};

	float value = 0.3;
	float margin;
	printf("Hello World!\n");
    while(1) {
		printf("Value is: %lf \n",ain.read());
		/*
		margin = value / 1000.0;
		if(ain.read() >= value - margin && ain.read() <= value + margin) {
			right = 0;
			left = 0;
			for(int i = 0; i < 4; i++) {
				for(int j = 0; j < (4-i); j++) {
					leds[j] = 1;
					wait(0.2);
					leds[j] = 0;
				}
				leds[(3-i)] = 1;
			}

			wait(0.9);

			for(int i = 0; i < 4; i++) {
				leds[i] = 0;
			}

			wait(0.3);

			for(int i = 0; i < 4; i++) {
				for(int j = 0; j < (4-i); j++) {
					leds[(3-j)] = 1;
					wait(0.2);
					leds[(3-j)] = 0;
				}
				leds[i] = 1;
			}

			wait(0.9);

			for(int i = 0; i < 4; i++) {
				leds[i] = 0;
			}

			for(int j = 0; j < 3; j++) {
				wait(0.4);

				for(int i = 0; i < 4; i++) {
					leds[i] = 1;
				}

				wait(0.5);

				for(int i = 0; i < 4; i++) {
					leds[i] = 0;
				}
			}

			wait(0.5);
		} else {
		 	if(ain.read() > value) {
				right = 0;
				left = 1;
			}

		 	if(ain.read() < value) {
				right = 1;
				left = 0;
			}
		}
*/
		/*if(change == 1) {
			if((increase || value < 0.1) && value <= 0.8) {
				leds[0] = 1;
				leds[1] = 0;
				increase = 1;
				value += 0.1;
			} else if((!increase || value > 0.8) && value >= 0.1) {
				leds[0] = 0;
				leds[1] = 1;
				increase = 0;
				value -= 0.1;
			}
			wait(0.5);
		}*/
	//	value = change;
		wait(0.5);
    }
}
