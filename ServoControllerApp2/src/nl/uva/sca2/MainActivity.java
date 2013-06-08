package nl.uva.sca2;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * @author René Aparicio Saez
 * @author Tom Peerdeman
 * 
 */
public class MainActivity extends Activity implements OnTouchListener,
		OnSeekBarChangeListener,
		AdkPort.MessageNotifier {
	private AdkPort mbedPort;
	
	private RadioButton radioButton1;
	private RadioButton radioButton2;
	private Button left;
	private Button right;
	private SeekBar seekBar;
	
	private boolean isLocal;
	private MbedNetwork netComponent;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("SCA2", "Starting ServoControllerApp 2");
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
		try {
			mbedPort = new AdkPort(this, this);
			new Thread(mbedPort).start();
		} catch(IOException e) {
			radioButton1.setChecked(false);
			radioButton2.setChecked(true);
			isLocal = false;
		}
		
		// Start the network component depending on if this app is server or
		// client
		if(isLocal) {
			netComponent = new MbedServoServer(this);
			radioButton2.setEnabled(false);
		} else {
			netComponent = new MbedServoClient(this);
			radioButton1.setEnabled(false);
			TextView systemval = (TextView) findViewById(R.id.systemval);
			TextView sys = (TextView) findViewById(R.id.textView2);
			systemval.setVisibility(View.INVISIBLE);
			sys.setVisibility(View.INVISIBLE);
		}
	}
	
	/**
	 * Notify the user an error has occurred.
	 * When the user closes the dialog the app is killed.
	 * 
	 * @param title
	 *            The title of the error dialog
	 * @param error
	 *            The text of the error dialog
	 */
	public void onError(String title, String error) {
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
		
		// set title
		alertBuilder.setTitle(title);
		
		// set dialog message
		alertBuilder.setMessage(error);
		alertBuilder.setCancelable(false);
		alertBuilder.setNegativeButton("Exit",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(which == AlertDialog.BUTTON_NEGATIVE) {
							Process.killProcess(Process.myPid());
						}
					}
				});
		
		// create alert dialog & show it
		alertBuilder.create().show();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	protected void onDestroy() {
		super.onDestroy();
		// Kill server or client on app stop
		if(netComponent != null) {
			netComponent.stop();
		}
		
		// Close the mbed port if open
		if(mbedPort != null) {
			mbedPort.onDestroy(this);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnTouchListener#onTouch(android.view.View,
	 * android.view.MotionEvent)
	 */
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
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.SeekBar.OnSeekBarChangeListener#onProgressChanged(android
	 * .widget.SeekBar, int, boolean)
	 */
	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		// Update the value of the TextView to the new value of the seekBar
		TextView value = (TextView) findViewById(R.id.value);
		value.setText("" + arg1 / 10.0);
		
		if(isLocal) {
			sendMbedChangeValue(arg1);
		}
		
		netComponent.newValue(arg1);
	}
	
	/**
	 * Called from the networking classes. Called when a new value is received.
	 * 
	 * @param value
	 *            The new value
	 */
	public void newValue(final int value) {
		final TextView valueText = (TextView) findViewById(R.id.value);
		valueText.post(new Runnable() {
			public void run() {
				valueText.setText("" + value / 10.0);
			}
		});
		
		seekBar.setProgress(value);
		if(isLocal) {
			sendMbedChangeValue(value);
		}
	}
	
	/**
	 * Send a value to the mbed via the usb connection.
	 * 
	 * @param value
	 *            The value to send.
	 */
	public void sendMbedChangeValue(int value) {
		byte[] buf = new byte[1];
		buf[0] = (byte) value;
		mbedPort.sendBytes(buf);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.SeekBar.OnSeekBarChangeListener#onStartTrackingTouch(android
	 * .widget.SeekBar)
	 */
	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.widget.SeekBar.OnSeekBarChangeListener#onStopTrackingTouch(android
	 * .widget.SeekBar)
	 */
	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i("SD2", "requestcode: " + requestCode + " resultcode: "
				+ resultCode);
		if(requestCode == MbedNetwork.REQUEST_ENABLE_BT) {
			if(resultCode == RESULT_OK) {
				netComponent.onStart();
			} else {
				onError("Bluetooth error", "Bluetooth disabled");
			}
		} else if(requestCode == MbedServoServer.REQUEST_DISCOVERABLE_BT
				&& netComponent instanceof MbedServoServer) {
			if(resultCode == RESULT_CANCELED) {
				onError("Bluetooth error", "Could not go into discovery mode");
			} else {
				((MbedServoServer) netComponent).onDiscoverable();
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.uva.servodingestweepuntnul.AdkPort.MessageNotifier#onNew()
	 */
	@Override
	public void onNew() {
		// Called when data is send from the mbed and ready to be read
		final TextView valueText = (TextView) findViewById(R.id.systemval);
		final byte a[] = mbedPort.readB();
		final int b = (int) a[0];
		final float c = b / 10.0f;
		valueText.post(new Runnable() {
			public void run() {
				valueText.setText("" + c);
			}
		});
	}
}
