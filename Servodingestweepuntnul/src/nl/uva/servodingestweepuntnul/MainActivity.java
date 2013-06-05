package nl.uva.servodingestweepuntnul;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity implements OnTouchListener,
		OnSeekBarChangeListener, OnClickListener {
	
	// init all buttons background : GRAY
	public void initButtons() {
		Button button1 = (Button) findViewById(R.id.left);
		Button button2 = (Button) findViewById(R.id.right);
		button1.setBackgroundColor(Color.WHITE);
		button2.setBackgroundColor(Color.WHITE);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button button1 = (Button) findViewById(R.id.left);
		Button button2 = (Button) findViewById(R.id.right);
		SeekBar seekbar1 = (SeekBar) findViewById(R.id.seekBar1);
		button1.setOnTouchListener(this);
		button2.setOnTouchListener(this);
		button1.setOnClickListener(this);
		button2.setOnClickListener(this);
		seekbar1.setOnSeekBarChangeListener(this);
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
		else
			((Button) v).setBackgroundColor(Color.WHITE);
		return true;
	}
	
	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		// Update the value of the TextView to the new value of the seekBar
		TextView value = (TextView) findViewById(R.id.value);
		value.setText("" + arg1 / 10.0);
	}
	
	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onClick(View arg0) {
		SeekBar progressBar = (SeekBar) findViewById(R.id.seekBar1);
		int progress = progressBar.getProgress();
		// left button pressed
		if(findViewById(R.id.left).getId() == arg0.getId()) {
			progress--;
		}
		// right button pressed
		else if(findViewById(R.id.right).getId() == arg0.getId()) {
			progress++;
		}
		// Set the progress of the seekBar
		progressBar.setProgress(progress);
		
		// Set the TextView to the new value
		TextView value = (TextView) findViewById(R.id.value);
		value.setText("" + progress / 10.0);
	}
	
}
