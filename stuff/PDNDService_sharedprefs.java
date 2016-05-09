package com.vag.politednd.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Map;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

public class PDNDService_sharedprefs extends Service {

	private final IBinder mBinder = (IBinder) new MyBinder();
	public static final String PREFS_NAME = "PDND_PREFS";

	SharedPreferences settings;
	Calendar start, end;
	boolean isEnabled;
	int howMany;
	ArrayList<Schedule> sch;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("PDNDService", "Service started");

		setupAppService();

		Calendar now = Calendar.getInstance();
		if (now.get(Calendar.HOUR) == start.get(Calendar.HOUR)
				&& now.get(Calendar.MINUTE) == start.get(Calendar.MINUTE)) { // start
			Log.i("PDNDService",
					"Scheduled start time reached. Enabling airplane mode");
			toggleAirplaneMode(true);
		} else if (now.get(Calendar.HOUR) == end.get(Calendar.HOUR)
				&& now.get(Calendar.MINUTE) == end.get(Calendar.MINUTE)) { // stop
			Log.i("PDNDService",
					"Scheduled end time reached. Disabling airplane mode");
			toggleAirplaneMode(false);
		}

		CountDownTimer minutesTimer = new CountDownTimer(60000, 60000) {

			@Override
			public void onTick(long millisUntilFinished) {
			}

			@Override
			public void onFinish() {
				Calendar now = Calendar.getInstance();
				if (now.get(Calendar.HOUR) == start.get(Calendar.HOUR)
						&& now.get(Calendar.MINUTE) == start
								.get(Calendar.MINUTE)) { // start
					Log.i("PDNDService",
							"Scheduled start time reached. Enabling airplane mode");
					toggleAirplaneMode(true);
				} else if (now.get(Calendar.HOUR) == end.get(Calendar.HOUR)
						&& now.get(Calendar.MINUTE) == end.get(Calendar.MINUTE)) { // stop
					Log.i("PDNDService",
							"Scheduled end time reached. Disabling airplane mode");
					toggleAirplaneMode(false);
				}

				start();
			}
		};

		minutesTimer.start();

		return Service.START_STICKY;
	}

	public void setupAppService() {
		settings = getSharedPreferences(PREFS_NAME, 0);
		sch = new ArrayList<Schedule>();

		Map<String, ?> values = settings.getAll();

		Iterator<String> iterator = values.keySet().iterator();

		while (iterator.hasNext()) {
			String key = iterator.next().toString();
			if (key.matches("schedule.*")) {
				Log.d("PDNDService", key + " = " + values.get(key));

				String tks[] = ((String) values.get(key)).split("\\|");
				Schedule s = new Schedule(tks[1], tks[2], tks[3]);

				s.setEnabled(((String) tks[4]).equals("1") ? true : false);
				sch.add(s);
			}
		}
	}

	public void checkSchedule() {
		Calendar now = Calendar.getInstance();

		for (int i = 0; i < sch.size(); i++) {
			if (sch.get(i).isEnabled()) {

			}
		}
		if (now.get(Calendar.HOUR) == start.get(Calendar.HOUR)
				&& now.get(Calendar.MINUTE) == start.get(Calendar.MINUTE)) { // start
			Log.i("PDNDService",
					"Scheduled start time reached. Enabling airplane mode");
			toggleAirplaneMode(true);
		} else if (now.get(Calendar.HOUR) == end.get(Calendar.HOUR)
				&& now.get(Calendar.MINUTE) == end.get(Calendar.MINUTE)) { // stop
			Log.i("PDNDService",
					"Scheduled end time reached. Disabling airplane mode");
			toggleAirplaneMode(false);
		}
	}

	public void old_setupAppService() {
		settings = getSharedPreferences(PREFS_NAME, 0);

		start = new GregorianCalendar();
		end = new GregorianCalendar();
		start.set(0, 0, 0, settings.getInt("iniHour", -1),
				settings.getInt("iniMinutes", -1), 0);

		end.set(0, 0, 0, settings.getInt("endHour", -1),
				settings.getInt("endMinutes", -1), 0);
	}

	public boolean toggleAirplaneMode(boolean enable) {
		boolean status = true;

		status = Settings.System.putInt(getContentResolver(),
				Settings.System.AIRPLANE_MODE_ON, enable ? 1 : 0);
		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		intent.putExtra("state", 0);
		sendBroadcast(intent);

		return status;
	}

	public class MyBinder extends Binder {
		public PDNDService_sharedprefs getService() {
			return PDNDService_sharedprefs.this;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		Log.i("PDNDService", "Service received bind request");
		return mBinder;
	}

}
