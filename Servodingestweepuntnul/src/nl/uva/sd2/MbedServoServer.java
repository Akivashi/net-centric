package nl.uva.sd2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;

public class MbedServoServer implements IMbedNetwork {
	private MainActivity main;
	private ServerSocket serverSock;
	private boolean isRunning;
	private InputStream in;
	private OutputStream out;
	
	public MbedServoServer(MainActivity main) {
		isRunning = true;
		this.main = main;
		new Thread(this).start();
	}
	
	@Override
	public void run() {
		Log.i("SD2", "Starting server...");
		try {
			serverSock = new ServerSocket(23568, 0);
		} catch(IOException e) {
			Log.e("SD2", "Could not open server socket", e);
			main.onError("Network error", "Could not open the server.");
			return;
		}
		Log.i("SD2", "Server listening on port " + serverSock.getLocalPort());
		Socket sock;
		int nread;
		byte[] buf = new byte[1];
		while(isRunning) {
			try {
				sock = serverSock.accept();
				in = sock.getInputStream();
				out = sock.getOutputStream();
			} catch(IOException e) {
				Log.e("SD2", "Could not open accept conn", e);
				continue;
			}
			
			Log.i("SD2", "Client connected");
			
			try {
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
				Log.e("SD2", "Could not read from conn", e);
			}
		}
	}
	
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