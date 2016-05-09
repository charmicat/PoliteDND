package com.vag.politednd.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.vag.politednd.dao.SQLiteAdapter;
import com.vag.politednd.shared.PDNDConstants;
import com.vag.politednd.shared.PDNDConstants.WeekdaysEnum;
import com.vag.politednd.view.CustomTimePicker;
import com.vag.politednd.view.WeekdayPicker;

import android.os.Bundle;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class ConfigurationActivity extends FragmentActivity {

	private SQLiteAdapter sqlAdapter;

	LinkedHashMap<WeekdaysEnum, Integer> selectDays;
	boolean isEdit = false;
	boolean isEnabled, isNonStop;
	int id, startHour, startMinute, endHour, endMinute;
	String daysStr;
	ListView timeRange;

	String TAG = "ConfigurationActivity";

	private CustomTimePicker.OnTimeSetListener startTimeListener = new CustomTimePicker.OnTimeSetListener() {
		@Override
		public void onTimeSet(int hourOfDay, int minute, boolean never) {
			Log.d("OnTimeSetListener",
					"OnTimeSetListener activated, startTimeListener");
			startHour = hourOfDay;
			startMinute = minute;
			isNonStop = never;
			TextView t = (TextView) timeRange.getChildAt(0);
			String time = hourOfDay + ":" + minute;
			t.setText(getResources().getString(R.string.start) + "\n" + time);
			Log.d(TAG, "timeRange " + timeRange.getChildCount());
		}
	};

	private CustomTimePicker.OnTimeSetListener endTimeListener = new CustomTimePicker.OnTimeSetListener() {
		@Override
		public void onTimeSet(int hourOfDay, int minute, boolean never) {
			Log.d("OnTimeSetListener",
					"OnTimeSetListener activated, endTimeListener");
			endHour = hourOfDay;
			endMinute = minute;
			isNonStop = never;
			TextView t = (TextView) timeRange.getChildAt(1);
			String time = never ? getResources().getString(
					R.string.neverCaption) : hourOfDay + ":" + minute;
			t.setText(getResources().getString(R.string.end) + "\n" + time);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_configuration);

		Bundle extras = getIntent().getExtras();
		id = -1;
		startHour = -1;

		isNonStop = false;

		timeRange = (ListView) findViewById(R.id.timeRangeList);
		timeRange.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {

				showTimePicker(position);
			}
		});
		ArrayList<String> listItems = new ArrayList<String>();
		listItems.add(getResources().getString(R.string.start));
		listItems.add(getResources().getString(R.string.end));

		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, listItems);
		timeRange.setAdapter(arrayAdapter);

		timeRange.post(new Runnable() { // ugly hack to get an workable ListView
					@Override
					public void run() {
						if (id != -1 && startHour != -1) { // setup was called
							TextView t = (TextView) timeRange.getChildAt(0);
							String time = startHour + ":" + startMinute;
							t.setText(getResources().getString(R.string.start)
									+ "\n" + time);

							t = (TextView) timeRange.getChildAt(1);
							time = isNonStop ? getResources().getString(
									R.string.neverCaption) : endHour + ":"
									+ endMinute;
							t.setText(getResources().getString(R.string.end)
									+ "\n" + time);
						}
					}
				});

		selectDays = new LinkedHashMap<WeekdaysEnum, Integer>(7);
		selectDays.put(PDNDConstants.WeekdaysEnum.SUNDAY, 0);
		selectDays.put(PDNDConstants.WeekdaysEnum.MONDAY, 0);
		selectDays.put(PDNDConstants.WeekdaysEnum.TUESDAY, 0);
		selectDays.put(PDNDConstants.WeekdaysEnum.WEDNESDAY, 0);
		selectDays.put(PDNDConstants.WeekdaysEnum.THURSDAY, 0);
		selectDays.put(PDNDConstants.WeekdaysEnum.FRIDAY, 0);
		selectDays.put(PDNDConstants.WeekdaysEnum.SATURDAY, 0);

		if (savedInstanceState == null) {
			extras = getIntent().getExtras();
			if (extras != null) {
				id = extras.getInt("id");
			}
		} else {
			id = (Integer) savedInstanceState.getSerializable("id");
		}

		if (id != -1) {
			isEdit = true;
			setup(id);
		} else {
			Calendar now = Calendar.getInstance();
			startHour = now.get(Calendar.HOUR);
			startMinute = now.get(Calendar.MINUTE);

			endHour = now.get(Calendar.HOUR);
			endMinute = now.get(Calendar.MINUTE);

			isEdit = false;
		}
	}

	public void onButtonClicked(View view) {
		switch (view.getId()) {
		case R.id.ok:
			if (sqlAdapter == null)
				sqlAdapter = new SQLiteAdapter(this);
			sqlAdapter.openToWrite();

			String iniStr = (startHour < 10 ? "0" : "") + startHour + ":"
					+ (startMinute < 10 ? "0" : "") + startMinute;

			String endStr;
			if (!isNonStop) {
				endStr = (endHour < 10 ? "0" : "") + endHour + ":"
						+ (endMinute < 10 ? "0" : "") + endMinute;
			} else {
				endStr = "-";
			}

			String weekdays = getSelectedDays();

			ContentValues cv = new ContentValues();
			cv.put(SQLiteAdapter.INI_COL, iniStr);
			cv.put(SQLiteAdapter.END_COL, endStr);
			cv.put(SQLiteAdapter.WEEKDAYS_COL, weekdays);

			if (!isEdit) {
				cv.put(SQLiteAdapter.ENABLED_COL, 1);
				try {
					sqlAdapter.insert(cv);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				cv.put(SQLiteAdapter.ENABLED_COL, isEnabled ? 1 : 0);
				try {
					sqlAdapter.update(id, cv);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			Log.d(TAG, " Configuration: " + iniStr + " " + endStr + " "
					+ weekdays);

			break;
		case R.id.cancel:
		}

		Intent intent = new Intent(this, MainActivity.class);
		sqlAdapter.close();
		startActivityForResult(intent, 1);
		finish();
	}

	public void onToggleClicked(View view) {
		ToggleButton tb = (ToggleButton) view;

		if (tb.isChecked()) {
			selectDays.put(PDNDConstants.WeekdaysEnum.getWeekday((String) tb
					.getTextOn()), 1);
		} else {
			selectDays.put(PDNDConstants.WeekdaysEnum.getWeekday((String) tb
					.getTextOn()), 0);
		}
	}

	public String getSelectedDays() {
		daysStr = "";
		Iterator<WeekdaysEnum> iterator = selectDays.keySet().iterator();
		boolean foundSelection = false;

		while (iterator.hasNext()) {
			WeekdaysEnum key = iterator.next();
			if (selectDays.get(key) == 1) {
				foundSelection = true;
				daysStr += key.getCode() + ",";
			}
		}

		daysStr = daysStr.replaceFirst(",$", ""); // remover ultima virgula

		if (!foundSelection) {
			daysStr = "-";
		}

		Log.d(TAG, "Selected days: " + daysStr);

		return daysStr;
	}

	public void setup(int id) {
		Log.d(TAG, "Updating id " + id);
		sqlAdapter = new SQLiteAdapter(this);
		sqlAdapter.openToRead();
		Cursor c = sqlAdapter.query(null, "id=" + id);

		for (c.moveToFirst(); !(c.isAfterLast()); c.moveToNext()) {

			isEnabled = c.getInt(c.getColumnIndex("enabled")) == 1 ? true
					: false;

			String[] toks = c.getString(c.getColumnIndex("ini")).split(":");
			startHour = Integer.parseInt(toks[0]);
			startMinute = Integer.parseInt(toks[1]);

			toks = c.getString(c.getColumnIndex("end")).split(":");
			if (toks[0].equals("-")) {
				isNonStop = true;
			} else {
				endHour = Integer.parseInt(toks[0]);
				endMinute = Integer.parseInt(toks[1]);
			}

			WeekdayPicker wp = (WeekdayPicker) findViewById(R.id.weekdayPicker);
			toks = c.getString(c.getColumnIndex("weekdays")).split(",");

			if (toks[0].charAt(0) != '-') {
				for (int i = 0; i < toks.length; i++) {
					selectDays.put(
							WeekdaysEnum.getWeekday(Integer.parseInt(toks[i])),
							1);
					wp.toggleDay(WeekdaysEnum.getWeekday(
							Integer.parseInt(toks[i])).toString());
				}
			}
		}
		c.close();
		sqlAdapter.close();
	}

	public void showTimePicker(int id) {
		FragmentManager fm = getSupportFragmentManager();
		CustomTimePicker tp;
		switch (id) {
		case 0: // start
			if (isEdit) {
				tp = CustomTimePicker.newInstance(0, startHour, startMinute,
						false);
			} else {
				tp = CustomTimePicker.newInstance(0);
			}
			tp.setOnTimeSetListener(startTimeListener);
			tp.show(fm, "fragment_time_picker");
			break;
		case 1:// end
			if (isEdit) {
				tp = CustomTimePicker.newInstance(1, endHour, endMinute,
						isNonStop);
			} else {
				tp = CustomTimePicker.newInstance(1);
			}
			tp.setOnTimeSetListener(endTimeListener);
			tp.show(fm, "fragment_time_picker");
		}
	}
}
