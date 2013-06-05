package nl.uva.servodingestweepuntnul;

import java.io.IOException;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity implements OnTouchListener,
		OnSeekBarChangeListener, OnCheckedChangeListener {
	private AdkPort mbedPort;
	private RadioButton radioButton1;
	private RadioButton radioButton2;
	private Button left;
	private Button right;
	private SeekBar seekBar;
	
	private boolean isLocal;
	private IMbedNetwork netComponent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("SD2","ik begin");
		isLocal = true;
		
		setContentView(R.layout.activity_main);
		left = (Button) findViewById(R.id.left);
		right = (Button) findViewById(R.id.right);
		left.setOnTouchListener(this);
		right.setOnTouchListener(this);
		
		seekBar = (SeekBar) findViewById(R.id.seekBar1);
		seekBar.setOnSeekBarChangeListener(this);
		
		radioButton1 = (RadioButton) findViewById(R.id.radio0);
		radioButton2 = (RadioButton) findViewById(R.id.radio1);
		radioButton1.setOnCheckedChangeListener(this);
		radioButton2.setOnCheckedChangeListener(this);
		Log.i("SD2", "mbed trycatch ");
		try {
			mbedPort = new AdkPort(getBaseContext());
		} catch(Exception e) {
			radioButton1.setChecked(false);
			radioButton2.setChecked(true);
			isLocal = false;
			Log.i("SD2", "No mbed? ", e);
		}
		Log.i("SD2", "After try catch");
		
		if(isLocal) {
			netComponent = new MbedServoServer(this);
			radioButton2.setEnabled(false);
		} else {
			netComponent = new MbedServoClient(this);
			radioButton1.setEnabled(false);

		}
		Log.i("SD2", "After if else");
	}
	
	protected void onDestroy() {
		// Kill server or client on app stop
		if(netComponent != null) {
			netComponent.stop();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent arg1) {
		if(arg1.getAction() == MotionEvent.ACTION_DOWN)
			((Button) v).setBackgroundColor(Color.GRAY);
		else {
			((Button) v).setBackgroundColor(Color.WHITE);
			int progress = seekBar.getProgress();
			
			if(left.getId() == v.getId()) {
				// left button pressed
				if(progress > 0)
					progress--;
			} else if(right.getId() == v.getId()) {
				// right button pressed
				if(progress < 100)
					progress++;
			}
			// Set the progress of the seekBar
			seekBar.setProgress(progress);
			
			// Set the TextView to the new value
			TextView value = (TextView) findViewById(R.id.value);
			value.setText("" + progress / 10.0);
		}
		return true;
	}
	
	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		// Update the value of the TextView to the new value of the seekBar
		TextView value = (TextView) findViewById(R.id.value);
		value.setText("" + arg1 / 10.0);
		Log.i("SD2", "before islocal");
		if(isLocal) {
			sendMbedChangeValue(arg1);
		}
		Log.i("SD2", "After islocal" + netComponent);
		netComponent.newValue(arg1);
		Log.i("SD2", "After newvalue");
	}
	
	public void newValue(int value) {
		TextView valueText = (TextView) findViewById(R.id.value);
		valueText.setText("" + value / 10.0);
		seekBar.setProgress(value);
		if(isLocal) {
			sendMbedChangeValue(value);
		}
	}
	
	public void sendMbedChangeValue(int value) {
		byte[] buf = new byte[1];
		buf[0] = (byte) value;
		mbedPort.sendBytes(buf);
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
	}
	
	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
	}
	
	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		if(radioButton1.getId() == arg0.getId()) {
			radioButton2.setChecked(!arg1);
		} else if(radioButton2.getId() == arg0.getId()) {
			radioButton1.setChecked(!arg1);
		}
	}
}
