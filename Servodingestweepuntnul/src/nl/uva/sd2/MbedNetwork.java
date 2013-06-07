package nl.uva.sd2;

import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;

public abstract class MbedNetwork implements Runnable {
	public static final int REQUEST_ENABLE_BT = 5378;
	public static final String SERVICE_NAME = "SD2-BT";
	public static final UUID SD2_UUID =
		UUID.fromString("4fdabc30-cf4e-11e2-8b8b-0800200c9a66");
	
	protected MainActivity main;
	protected BluetoothAdapter adapter;
	
	public MbedNetwork(MainActivity main) {
		this.main = main;
		
		adapter = BluetoothAdapter.getDefaultAdapter();
		if(adapter == null) {
			main.onError("Bluetooth error",
					"Device has no bluetooth support or it's disabled");
			return;
		}
		
		if(!adapter.isEnabled()) {
			Intent enableBtIntent =
				new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			main.startActivityForResult(enableBtIntent,
					MbedNetwork.REQUEST_ENABLE_BT);
		}
	}
	
	public abstract void onStart();
	
	public abstract void newValue(int value);
	
	public abstract void stop();
}
