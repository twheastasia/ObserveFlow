package com.twh.observeflow;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.net.TrafficStats;

public class MainActivity extends Activity {

	private Button startBtn,stopBtn;
	private Intent intent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		startBtn = (Button)findViewById(R.id.button1);
		stopBtn = (Button)findViewById(R.id.button2);
		intent = new Intent(MainActivity.this, MainActivityService.class);
		startBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startService(intent);
				showToast("start service successfully!");
			}
		});
		
		stopBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				stopService(intent);
				showToast("stop service successfully!");
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private void showToast(String info)
	{
		Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
	}

}
