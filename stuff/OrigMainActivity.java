package com.vag.politednd.activity;

import com.vag.politednd.fragment.TimePickerFragment;
import com.vag.politednd.service.PDNDService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

public class OrigMainActivity extends FragmentActivity {
	TextView status;
	CheckBox never, now;
	TimePicker endTime, iniTime;
	SharedPreferences settings;
	ToggleButton enable;
	Intent service;
	boolean manualEndTime;

	// TODO: tocar algum audio quando habilitar/desabilitar airplane mode

	public static final String PREFS_NAME = "PDND_PREFS";

	public PDNDService s;

	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder binder) {
			s = ((PDNDService.MyBinder) binder).getService();

			Toast.makeText(OrigMainActivity.this, "Connected", Toast.LENGTH_SHORT)
					.show();
		}

		public void onServiceDisconnected(ComponentName className) {
			Toast.makeText(OrigMainActivity.this, "Disconnected",
					Toast.LENGTH_SHORT).show();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setupApp();
	}

	public void showTimePickerDialog(View v) {
		TimePickerFragment newFragment = new TimePickerFragment();
		newFragment.show(getSupportFragmentManager(), "timePicker");
	}

	public void onCheckboxClicked(View view) {
		switch (view.getId()) {
		case R.id.never:
			if (never.isChecked()) {
				endTime.setVisibility(View.INVISIBLE);
			} else {
				endTime.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.now:
			if (now.isChecked()) {
				Log.i("PDNDActivity", "Enabling airplane mode");
				enable.setChecked(true);
				s.toggleAirplaneMode(true);
			} else {
				Log.i("PDNDActivity", "Disabling airplane mode");
				enable.setChecked(true);
				s.toggleAirplaneMode(false);
			}
		}
	}

	public void setupApp() {
		manualEndTime = false;
		settings = getSharedPreferences(PREFS_NAME, 0);

		iniTime = (TimePicker) findViewById(R.id.iniTime);
		iniTime.setIs24HourView(true);
		iniTime.setCurrentHour(settings.getInt("iniHour", 0));
		iniTime.setCurrentMinute(settings.getInt("iniMinutes", 0));

		endTime = (TimePicker) findViewById(R.id.endTime);
		endTime.setIs24HourView(true);
		endTime.setCurrentHour(settings.getInt("endHour", 0));
		endTime.setCurrentMinute(settings.getInt("endMinutes", 0));

		never = (CheckBox) findViewById(R.id.never);

		now = (CheckBox) findViewById(R.id.now);

		enable = (ToggleButton) findViewById(R.id.enable);
		if (settings.getInt("enabled", 0) == 1) {
			enable.setChecked(true);
			doBindService();
		} else {
			enable.setChecked(false);
		}

		enable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				SharedPreferences.Editor editor = settings.edit();

				if (isChecked) {
					editor.putInt("iniHour", iniTime.getCurrentHour());
					editor.putInt("iniMinutes", iniTime.getCurrentMinute());
					editor.putInt("endHour", endTime.getCurrentHour());
					editor.putInt("endMinutes", endTime.getCurrentMinute());
					editor.putInt("enabled", 1);
					editor.putInt("manual", manualEndTime ? 1 : 0);
					editor.commit();

					doBindService();
				} else {
					Settings.System.putInt(getContentResolver(),
							Settings.System.AIRPLANE_MODE_ON, 0);
					editor.putInt("enabled", 0);
					editor.commit();

					unbindService(mConnection);
					stopService(service);
					Log.i("PDNDActivity", "Service disabled");
				}
			}
		});
	}

	void doBindService() {
		service = new Intent(this, PDNDService.class);
		this.startService(service);
		bindService(new Intent(this, PDNDService.class), mConnection,
				Context.BIND_AUTO_CREATE);
		Log.i("PDNDActivity", "Activity binded to service");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
