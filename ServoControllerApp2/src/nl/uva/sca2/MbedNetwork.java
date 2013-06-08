package nl.uva.sca2;

import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;

/**
 * @author René Aparicio Saez
 * @author Tom Peerdeman
 * 
 */
public abstract class MbedNetwork implements Runnable {
	// Random generated request code
	public static final int REQUEST_ENABLE_BT = 5378;
	// The name of our service
	public static final String SERVICE_NAME = "SCA2-BT";
	// Random generated UUID
	public static final UUID SD2_UUID =
		UUID.fromString("4fdabc30-cf4e-11e2-8b8b-0800200c9a66");
	
	protected MainActivity main;
	protected BluetoothAdapter adapter;
	
	/**
	 * @param main
	 *            The main activity to call with new values & start activity's
	 *            on
	 */
	public MbedNetwork(MainActivity main) {
		this.main = main;
		
		adapter = BluetoothAdapter.getDefaultAdapter();
		if(adapter == null) {
			main.onError("Bluetooth error",
					"Device has no bluetooth support or it's disabled");
			return;
		}
		
		if(!adapter.isEnabled()) {
			// BT is off, request it to be enabled
			Intent enableBtIntent =
				new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			main.startActivityForResult(enableBtIntent,
					MbedNetwork.REQUEST_ENABLE_BT);
		} else {
			// BT is already enabled, start the networking
			onStart();
		}
	}
	
	/**
	 * Called when bluetooth is enabled
	 */
	public abstract void onStart();
	
	/**
	 * Called when the user sets a new value using the slider or buttons
	 * 
	 * @param value
	 *            The new value
	 */
	public abstract void newValue(int value);
	
	/**
	 * Called when the app is destroyed, should kill it's threads
	 */
	public abstract void stop();
}
