#include "mbed.h"
#include "math.h"

const float values[] = {0.002, 0.0062, 0.018, 0.059, 0.105, 0.152, 0.2, 0.302, 0.504, 0.745, 0.996};

AnalogIn ain(p16);
DigitalOut left(p15);
DigitalOut right(p18);
DigitalOut myled1(LED1);

float interpolate(float x) {
	if(x < 0 || x > 10.0) {
		return -1.0;
	}

	float ind = floor(x);
	int indi = (int) ind;
	if(ind == x) {
		return values[indi];
	}

	float low = values[indi];
	float high = values[indi + 1];
	float shift = x - ind;
	return (high - low) * shift + low;
}

int main() {
	float value = -1.0;
	float margin;
    while(1) {
		if(value == -1.0) {
			scanf("%f", &value);
			value = interpolate(value);
			printf("New value: %f\n", value);
			continue;
		}

		margin = value / 1000.0;
		if(ain.read() >= value - margin && ain.read() <= value + margin) {
			right = 0;
			left = 0;
			myled1 = 1;
			value = -1.0;
		} else {
			myled1 = 0;
		 	if(ain.read() > value) {
				right = 0;
				left = 1;
			}

		 	if(ain.read() < value) {
				right = 1;
				left = 0;
			}
		}
		wait(0.1);
    }
}

