#include "mbed.h"
#include "math.h"
#include <iostream>

Serial pc(USBTX, USBRX);
AdkTerm AdkTerm;
Ticker flipper;

class AdkTerm :public AndroidAccessory
{
public:
    AdkTerm():AndroidAccessory(INBL,OUTL,
                                   "ARM",
                                   "mbed",
                                   "mbed Terminal",
                                   "0.1",
                                   "http://www.mbed.org",
                                   "0000000012345678"),pc(USBTX,USBRX),Right(p19),Left(p20) {};
    virtual int callbackRead(u8 *buff, int len);
    virtual void setupDevice();
    virtual void resetDevice();
    virtual int callbackWrite();

private:
    void serialIRQ();
    void onTick();
    void AttachTick();
    char buffer[OUTL];
    int bcount;
    Serial pc;
    AnalogIn Right;
    AnalogIn Left;
    Ticker tick;
    float right,left,rl,ll;
    int tl,tr;
    Timeout n;
    bool settick;
};



void AdkTerm::setupDevice()
{
    pc.baud(115200);
    pc.printf("Welcome to adkTerm (MbedSketch)\n\r");
    settick = false;
    pc.attach(this, &AdkTerm::serialIRQ, Serial::RxIrq);
    for (int i = 0; i<OUTL; i++) {
        buffer[i] = 0;
    }
    bcount = 0;
    //n.attach(this,&AdkTerm::AttachTick,5);
    //tick.attach(this,&AdkTerm::onTick,0.1);
}

void AdkTerm::AttachTick()
{
    if(!settick)tick.attach(this,&AdkTerm::onTick,0.04);
    settick = true;
}

void AdkTerm::onTick()
{
    right = 1-Right;
    left = 1-Left;
    bool update = false;
    int templ, tempr;

    templ = int(left * 10000);
    tempr = int(right * 10000);

    if (abs(templ-tl)>170) {
        update = true;
    }
    if (abs(tempr-tr)>170) {
        update = true;
    }
    if (update) {
        u8* wbuf = _writebuff;

        wbuf[0] = 'P';
        wbuf[1] =  templ&0xFF;
        wbuf[2] = (templ>>8) & 0xFF;
        wbuf[3] =  tempr&0xFF;
        wbuf[4] = (tempr>>8) & 0xFF;
        wbuf[5] = 0;

        this->write(wbuf,5);
    }
}

void AdkTerm::resetDevice()
{
    pc.printf("adkTerm reset\n\r");
    for (int i = 0; i<OUTL; i++) {
        buffer[i] = 0;
    }
    bcount = 0;
}

int AdkTerm::callbackRead(u8 *buf, int len)
{
    pc.printf("%i  %s\n\r\n\n\n",len,buf);
    for (int i = 0; i<INBL; i++) {
        buf[i] = 0;
    }

    AttachTick();

    return 0;
}

int AdkTerm::callbackWrite()
{

    ind = false;
    return 0;
}


void AdkTerm::serialIRQ()
{
    buffer[bcount] = pc.getc();

    pc.putc(buffer[bcount]);

    if (buffer[bcount] == '\n' || buffer[bcount] == '\r') {
        u8* wbuf = _writebuff;
        for (int i = 0; i<OUTL; i++) {
            wbuf[i] = buffer[i];
            buffer[i] = 0;
        }
        pc.printf("Sending: %s\n\r",wbuf);
        ind = true;
        this->write(wbuf,bcount);
        bcount = 0;
    } else {
        if (buffer[bcount] != 0x08 && buffer[bcount] != 0x7F ) {
            bcount++;
            if (bcount == OUTL) {
                bcount = 0;
            }
        } else {
            bcount--;
        }
    }

}

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

void servoChanger(){
	margin = value / 20.0;
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
}

int main() {
	AnalogIn ain(p16);
	DigitalOut left(p15);
	DigitalOut right(p18);
	DigitalOut myled1(LED1);
	AdkTerm.setupDevice();
	printf("Android Development Kit: start\r\n");
	USBInit();

	float value = interpolate(5.0);
	float margin;
	flipper.attach(&servoChanger, 0.1);
	while(1) {
		USBLoop();
	}
}
