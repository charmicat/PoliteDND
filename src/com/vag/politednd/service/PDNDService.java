package com.vag.politednd.service;

import java.util.Calendar;
import java.util.GregorianCalendar;

import com.vag.politednd.dao.SQLiteAdapter;
import com.vag.politednd.shared.App;
import com.vag.politednd.shared.PDNDConstants.WeekdaysEnum;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;

public class PDNDService extends Service {

	private final IBinder mBinder = (IBinder) new MyBinder();

	boolean isEnabled, isAplModeOn, isBound;
	SparseArray<Schedule> sch;

	private SQLiteAdapter sqlAdapter;
	CountDownTimer minutesTimer;

	String defaultRadios;

	String TAG = "PDNDService";

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "Service started");

		setupAppService();

		checkSchedule(); // primeira checagem, proxima daqui 30 segundos

		minutesTimer = new CountDownTimer(30000, 30000) {

			@Override
			public void onTick(long millisUntilFinished) {
			}

			@Override
			public void onFinish() {
				checkSchedule();
				start();
			}
		};

		minutesTimer.start();

		return Service.START_STICKY;
	}

	public void setupAppService() {
		sch = new SparseArray<Schedule>();

		sqlAdapter = new SQLiteAdapter(this);
		sqlAdapter.openToRead();

		Cursor c = sqlAdapter.queueAll();
		for (c.moveToFirst(); !(c.isAfterLast()); c.moveToNext()) {
			int id = c.getInt(c.getColumnIndex("id"));
			String ini = c.getString(c.getColumnIndex("ini"));
			String end = c.getString(c.getColumnIndex("end"));
			String weekdays = c.getString(c.getColumnIndex("weekdays"));
			Schedule s = new Schedule(id, ini, end, weekdays);

			s.setEnabled(c.getInt(c.getColumnIndex("enabled")) == 1 ? true
					: false);
			sch.put(id, s);
		}

		c.close();
		sqlAdapter.close();

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(Settings.System.RADIO_BLUETOOTH);
		stringBuilder.append(",");
		stringBuilder.append(Settings.System.RADIO_CELL);
		stringBuilder.append(",");
		stringBuilder.append("nfc"); // Settings.System.RADIO_NFC
		stringBuilder.append(",");
		stringBuilder.append(Settings.System.RADIO_WIFI);
		defaultRadios = stringBuilder.toString();

		Log.d(TAG, "List of radios: " + defaultRadios);
	}

	public void checkSchedule() {
		Calendar now = Calendar.getInstance();
		WeekdaysEnum weekday = WeekdaysEnum.getWeekday(now
				.get(Calendar.DAY_OF_WEEK));
		Log.i(TAG, now.get(Calendar.HOUR) + ":" + now.get(Calendar.MINUTE)
				+ ", " + weekday + "(" + now.get(Calendar.DAY_OF_WEEK)
				+ "): Checking schedule");

		isAplModeOn = Settings.System.getInt(getContentResolver(),
				Settings.System.AIRPLANE_MODE_ON, 0) != 0;

		int k = 0;
		for (int i = 0; i < sch.size(); i++) {
			k = sch.keyAt(i);
			Schedule s = sch.get(k);
			if (s.isNoRepeat()) {
				weekday = WeekdaysEnum.NONE;
			} else {
				weekday = WeekdaysEnum
						.getWeekday(now.get(Calendar.DAY_OF_WEEK));
			}

			if (s.isEnabled() == true && s.getIni().containsKey(weekday)) {
				Calendar start = s.getIni().get(weekday);
				Calendar end = s.getEnd().get(weekday);
				Log.d(TAG,
						"Found enabled schedule for today: i="
								+ start.get(Calendar.HOUR) + ":"
								+ start.get(Calendar.MINUTE));

				if (!isAplModeOn) { // start
					checkTimeRange(start, end);
				} else if (!s.isNonStop()
						&& now.get(Calendar.HOUR) == end.get(Calendar.HOUR)
						&& now.get(Calendar.MINUTE) == end.get(Calendar.MINUTE)) { // stop
					Log.i(TAG,
							"Scheduled end time reached. Disabling airplane mode");
					App.toggleAirplaneMode(false, true);

					if (s.isNoRepeat()) {
						toggleSchedule(s.getId(), false, true);
					}
				}
			}
		}
	}

	public boolean checkTimeRange(Calendar s, Calendar e) {
		Calendar now = new GregorianCalendar();

		// 0 if the times of the two Calendars are equal
		// -1 if the time of now is before the other one
		// 1 if the time of now is after the other one

		if (now.compareTo(s) == 0 || now.compareTo(s) == 1) {
			if (now.compareTo(e) == 0 || now.compareTo(e) == -1) {
				Log.i(TAG,
						"Scheduled start time reached. Enabling airplane mode");
				App.toggleAirplaneMode(true, true);
			}
		}

		return false;
	}

	public void removeSchedule(int id) {
		sch.remove(id);
	}

	public void toggleSchedule(int id, boolean enabled, boolean updateDB) {
		Schedule s = sch.get(id);
		s.setEnabled(enabled);
		sch.put(id, s);

		if (updateDB) {
			sqlAdapter.close();
			sqlAdapter.openToWrite();
			ContentValues cv = new ContentValues();
			cv.put(SQLiteAdapter.INI_COL, s.getIniString());
			cv.put(SQLiteAdapter.END_COL, s.getEndString());
			cv.put(SQLiteAdapter.WEEKDAYS_COL, s.getWeekdaysString());
			cv.put(SQLiteAdapter.ENABLED_COL, enabled ? 1 : 0);
			sqlAdapter.update(id, cv);
			sqlAdapter.close();
		}
	}

	public class MyBinder extends Binder {
		public PDNDService getService() {
			return PDNDService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i(TAG, "Service received bind request");
		isBound = true;
		return mBinder;
	}

	public boolean onUnbind(Intent intent) {
		Log.i(TAG, "Service received unbind request");
		isBound = false;
		return isBound;
	}

	public void onDestroy() {
		minutesTimer.cancel();
		App.toggleAirplaneMode(false, false);
	}

	public void setSqlAdapter(SQLiteAdapter sqlAdapter) {
		this.sqlAdapter = sqlAdapter;
	}
}
