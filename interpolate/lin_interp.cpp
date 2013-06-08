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

// This function interpolates a value between 0 and 10 to a value
// between 0.0 and 1.0
float interpolate(float x) {
	// known values between 0.0 and 1.0 for indices from 0 to 10
	const float values[] = {0.002, 0.0062, 0.018, 0.059, 0.105, 0.152, 0.2, 0.302, 0.504, 0.745, 0.996};

	// If a fiven value is not within the indices range, return an error code
	if(x < 0 || x > 10.0) {
		return -1.0;
	}
	// Find the correct indices for the incoming value
	float ind = floor(x);
	int indi = (int) ind;
	// If the value is exactly the value of an index, return the value for that index.
	if(ind == x) {
		return values[indi];
	}

	// Calculate the approcimate location for the given value
	float low = values[indi];
	float high = values[indi + 1];
	float shift = x - ind;
	return (high - low) * shift + low;
}

// This function interpolates a value between 0 and 1.0 to a value
// between 0 and 10
float rev_interpolate(float x) {
	// known values between 0.0 and 1.0 for indices from 0 to 10
	const float values[] = {0.002, 0.0062, 0.018, 0.059, 0.105, 0.152, 0.2, 0.302, 0.504, 0.745, 0.996};

	// find the correct indices for the given value
	int ind = 0;
	for(int i = 0; i < 10; i++){
		// set the index to the 
		if(x >= values[i] && x <= values[i+1]){
			ind = i;
		}
	}
	// If the value crosses the largest possibility, return the largest index
	if(x > values[10])
		return 10;
	// If the value is exactly the value from the given index, return the index
	if(x == values[ind])
		return ind;
	// If the value is exactly the value from the given index+1, return the index+1
	if(x == values[ind+1])
		return ind+1;
	
	// Calculate the approximate value that is visible to the user.
	float difference = fabs(values[ind+1] - values[ind]);
	return ind + (x-values[ind])/difference;
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
private:
	void writeToDevice();
	Ticker flipper2;
};

// This function returns the current measured value back to the Android device
void AdkTerm::writeToDevice(){
	/*
	 * The value that should be returned lays between 0 and 100.
	 * The value measured lays betweer 0.0 and 1.0. To find the correct value,
	 * reverse linear interpolation is used.
	 */
	float systemval = rev_interpolate(ain.read())*10;
	u8* wbuf = _writebuff;
	wbuf[0] = systemval;
	this->write(wbuf,1);
}

void AdkTerm::setupDevice() {
}


void AdkTerm::resetDevice() {
}

// This function reads incoming values from an Android device
int AdkTerm::callbackRead(u8 *buf, int len) {
	/* 
	 * The incoming buf[0] should replace the current wanted value.
	 * The received value will be between 0 and 100. This is changed to a
	 * value between 0 and 10 by dividing this value by 10. To find the correct
	 * value that the potentiometer should be measuring,
	 * this value has to be interpolated
	 */
	value = interpolate((buf[0] / 10.0));
	
	/* 
	 * When the value is set, return the current visible value back to
	 * the android device. This is done in a low frequency to prevent a
	 * flood of information back to the Android device
	 */
	flipper2.attach(this, &AdkTerm::writeToDevice, 0.5);
    return 0;
}

int AdkTerm::callbackWrite() {
    return 0;
}

AdkTerm AdkTerm;

// This function looks at the wanted value and
// changes the position of the electro motor to this value
void servoChanger(){
	// Set the margin around the wanted value.
	// The margin is within 5% of the value
	float margin = value / 20.0;

	// Stop moving if the potentiometer finds a value within
	// the margin of the wanted value
	if(ain.read() >= value - margin && ain.read() <= value + margin) {
		AdkTerm.right = 0;
		AdkTerm.left = 0;
		// Notify the user by lighting up a LED
		myled1 = 1;
	}
	// If the potentiometer is not reading a value within the margins,
	// shut down the LED.
	else {
		myled1 = 0;
		// If the wanted value is smaller then the measured value,
		// move to the left
	 	if(ain.read() > value) {
			AdkTerm.right = 0;
			AdkTerm.left = 1;
		}
		// If the wanted value is greater then the measured value,
		// move to the right
	 	if(ain.read() < value) {
			AdkTerm.right = 1;
			AdkTerm.left = 0;
		}
	}
}

int main() {
	// Setup and initialize the connection
	AdkTerm.setupDevice();
	USBInit();
	// Find the standard start value
	value = interpolate(5.0);
	// Begin the ticker that calls the servochanger
	flipper.attach(&servoChanger, 0.1);
	// continuously call the USB connection
	while(1) {
		USBLoop();
	}
}
