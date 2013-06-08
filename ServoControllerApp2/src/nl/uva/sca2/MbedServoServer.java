package nl.uva.sca2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

public class MbedServoServer extends MbedNetwork {
	// Request code of MbedNetwork.REQUEST_ENABLE_BT + 1
	public static final int REQUEST_DISCOVERABLE_BT = 5379;
	
	private BluetoothServerSocket serverSock;
	private boolean isRunning;
	private InputStream in;
	private OutputStream out;
	
	/**
	 * @param main
	 *            The main activity to call with new values & start activity's
	 *            on
	 */
	public MbedServoServer(MainActivity main) {
		super(main);
		isRunning = true;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.uva.sca2.MbedNetwork#onStart()
	 */
	@Override
	public void onStart() {
		Intent discoverableIntent =
			new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(
				BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
		main.startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE_BT);
	}
	
	/**
	 * Called when this device is bluetooth discoverable
	 */
	public void onDiscoverable() {
		new Thread(this).start();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		BluetoothSocket sock;
		int nread;
		byte[] buf = new byte[1];
		
		while(isRunning) {
			try {
				serverSock =
					adapter.listenUsingInsecureRfcommWithServiceRecord(
							MbedNetwork.SERVICE_NAME, MbedNetwork.SD2_UUID);
			} catch(IOException e) {
				Log.e("SCA2", "Could not open server socket", e);
				main.onError("Network error", "Could not open the server.");
				return;
			}
			
			Log.i("SCA2", "Opened bluetooth server socket with adress "
					+ adapter.getAddress());
			
			try {
				sock = serverSock.accept();
				in = sock.getInputStream();
				out = sock.getOutputStream();
			} catch(IOException e) {
				Log.e("SCA2", "Could not open accept conn", e);
				continue;
			}
			Log.i("SCA2", "Client connected");
			while(isRunning) {
				try {
					Log.i("SCA2", "trying to receive something");
					nread = in.read(buf);
					if(nread < 0) {
						out.close();
						in.close();
						sock.close();
						in = null;
						out = null;
					}
					if(nread > 0) {
						// Update Mbed & progressbar
						main.newValue(buf[0] & 0xFF);
					}
				} catch(IOException e) {
					Log.e("SCA2", "Could not read from conn", e);
				}
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.uva.sca2.MbedNetwork#newValue(int)
	 */
	@Override
	public void newValue(int value) {
		if(out != null) {
			try {
				out.write(value);
				out.flush();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see nl.uva.sca2.MbedNetwork#stop()
	 */
	@Override
	public void stop() {
		isRunning = false;
		if(serverSock != null) {
			try {
				serverSock.close();
			} catch(IOException idontcare) {
			}
		}
		
		if(in != null && out != null) {
			try {
				in.close();
				out.close();
			} catch(IOException idontcare) {
			}
		}
	}
}
