#include "mbed.h"
#include "math.h"
#include "AndroidAccessory.h"
#include <iostream>

#define OUTL 100
#define INBL 100

AnalogIn ain(p16);
DigitalOut myled1(LED1);
Ticker flipper;
float value;


float interpolate(float x) {
	const float values[] = {0.002, 0.0062, 0.018, 0.059, 0.105, 0.152, 0.2, 0.302, 0.504, 0.745, 0.996};

	if(x < 0 || x > 10.0) {
		return -1.0;
	}
	cout << x << endl;
	float ind = floor(x);
	cout << ind << endl;
	int indi = (int) ind;
	cout << indi << endl;
	if(ind == x) {
		return values[indi];
	}

	float low = values[indi];
	float high = values[indi + 1];
	float shift = x - ind;
	return (high - low) * shift + low;
}

class AdkTerm :public AndroidAccessory
{
public:
	DigitalOut left;
	DigitalOut right;
    AdkTerm():AndroidAccessory(INBL,OUTL,
                                   "ARM",
                                   "mbed",
                                   "mbed Terminal",
                                   "0.1",
                                   "http://www.mbed.org",
                                   "0000000012345678"), left(p15), right(p18){};

    virtual int callbackRead(u8 *buff, int len);
    virtual void setupDevice();
    virtual void resetDevice();
    virtual int callbackWrite();
};

void AdkTerm::setupDevice() {
}


void AdkTerm::resetDevice() {
}

int AdkTerm::callbackRead(u8 *buf, int len) {
	value = interpolate((buf[0] / 10.0));
    return 0;
}

int AdkTerm::callbackWrite() {
    return 0;
}

AdkTerm AdkTerm;

void servoChanger(){
	cout << "Value" << value << endl;
	cout << "ain" << ain.read() << endl;
	float margin = value / 20.0;
	if(ain.read() >= value - margin && ain.read() <= value + margin) {
		AdkTerm.right = 0;
		AdkTerm.left = 0;
		myled1 = 1;
	} else {
		myled1 = 0;
	 	if(ain.read() > value) {
			AdkTerm.right = 0;
			AdkTerm.left = 1;
		}

	 	if(ain.read() < value) {
			AdkTerm.right = 1;
			AdkTerm.left = 0;
		}
	}
}

int main() {
	cout << "Program start" << endl;
	AdkTerm.setupDevice();
	cout << "Adk setup done" << endl;
	USBInit();
	cout << "USB init done" << endl;
	value = interpolate(5.0);
	cout << "Value set " << value << endl;
	flipper.attach(&servoChanger, 0.1);
	while(1) {
		USBLoop();
	}
}
