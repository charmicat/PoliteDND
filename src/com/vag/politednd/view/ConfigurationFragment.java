package com.vag.politednd.view;

import java.util.ArrayList;
import java.util.Calendar;

import com.vag.politednd.activity.R;
import com.vag.politednd.dao.SQLiteAdapter;
import com.vag.politednd.shared.PDNDConstants.WeekdaysEnum;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ConfigurationFragment extends DialogFragment {

	boolean isEdit = false;
	boolean isEnabled, isNonStop;
	int id, startHour, startMinute, endHour, endMinute;
	String daysStr;
	ListView timeRange;
	Dialog d;
	static Context c;
	static SQLiteAdapter sqlAdpt;

	OnCfgSetListener onCfgSetListener = null;

	WeekdayPicker wp;
	String TAG = "ConfigurationFragment";

	private CustomTimePicker.OnTimeSetListener startTimeListener = new CustomTimePicker.OnTimeSetListener() {
		@Override
		public void onTimeSet(int hourOfDay, int minute, boolean never) {
			Log.d("OnTimeSetListener",
					"OnTimeSetListener activated, startTimeListener");
			startHour = hourOfDay;
			startMinute = minute;
			isNonStop = never;
			TextView t = (TextView) timeRange.getChildAt(0);
			String time = (startHour < 10 ? "0" : "") + startHour + ":"
					+ (startMinute < 10 ? "0" : "") + startMinute;
			t.setText(getResources().getString(R.string.start) + "\n" + time);
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
			String time = "";
			if (!isNonStop) {
				time = (endHour < 10 ? "0" : "") + endHour + ":"
						+ (endMinute < 10 ? "0" : "") + endMinute;
			} else {
				time = getResources().getString(R.string.neverCaption);
			}
			t.setText(getResources().getString(R.string.end) + "\n" + time);
		}
	};

	public static ConfigurationFragment newInstance(int id, Context context,
			SQLiteAdapter sadp) {
		ConfigurationFragment frag = new ConfigurationFragment();
		Bundle args = new Bundle();
		args.putInt("id", id);
		frag.setArguments(args);

		sqlAdpt = sadp;
		c = context;

		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		id = getArguments().getInt("id");

		d = new Dialog(getActivity());
		d.setContentView(R.layout.activity_configuration);

		startHour = -1;

		isNonStop = false;

		timeRange = (ListView) d.findViewById(R.id.timeRangeList);
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

		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				d.getContext(), android.R.layout.simple_list_item_1, listItems);
		timeRange.setAdapter(arrayAdapter);

		timeRange.post(new Runnable() { // ugly hack to get a workable ListView
					@Override
					public void run() {
						TextView t0 = (TextView) timeRange.getChildAt(0);
						t0.setTextColor(getResources().getColor(R.color.black));

						TextView t1 = (TextView) timeRange.getChildAt(1);
						t1.setTextColor(getResources().getColor(R.color.black));
						if (id != -1 && startHour != -1) { // setup was called
							String time = (startHour < 10 ? "0" : "")
									+ startHour + ":"
									+ (startMinute < 10 ? "0" : "")
									+ startMinute;

							t0.setText(getResources().getString(R.string.start)
									+ "\n" + time);

							if (!isNonStop) {
								time = (endHour < 10 ? "0" : "") + endHour
										+ ":" + (endMinute < 10 ? "0" : "")
										+ endMinute;
							} else {
								time = getResources().getString(
										R.string.neverCaption);
							}
							t1.setText(getResources().getString(R.string.end)
									+ "\n" + time);
						}
					}
				});

		if (id != -1) {
			d.setTitle(R.string.edit_schedule);
			isEdit = true;
			setup(id);
		} else {
			d.setTitle(R.string.new_schedule);
			Calendar now = Calendar.getInstance();
			startHour = now.get(Calendar.HOUR);
			startMinute = now.get(Calendar.MINUTE);

			endHour = now.get(Calendar.HOUR);
			endMinute = now.get(Calendar.MINUTE);

			isEdit = false;
		}

		Button cancel = (Button) d.findViewById(R.id.cancel);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		Button ok = (Button) d.findViewById(R.id.ok);
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (onCfgSetListener != null) {
					onCfgSetListener.onCfgSet(id);
				}
				onButtonClicked();
				dismiss();
			}
		});

		wp = (WeekdayPicker) d.findViewById(R.id.weekdayPicker);

		return d;
	}

	public void onButtonClicked() {
		sqlAdpt.openToWrite();

		String iniStr = (startHour < 10 ? "0" : "") + startHour + ":"
				+ (startMinute < 10 ? "0" : "") + startMinute;

		String endStr;
		if (!isNonStop) {
			endStr = (endHour < 10 ? "0" : "") + endHour + ":"
					+ (endMinute < 10 ? "0" : "") + endMinute;
		} else {
			endStr = "-";
		}

		String weekdays = wp.getSelectedDays();

		ContentValues cv = new ContentValues();
		cv.put(SQLiteAdapter.INI_COL, iniStr);
		cv.put(SQLiteAdapter.END_COL, endStr);
		cv.put(SQLiteAdapter.WEEKDAYS_COL, weekdays);

		if (!isEdit) {
			cv.put(SQLiteAdapter.ENABLED_COL, 1);
			try {
				sqlAdpt.insert(cv);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			cv.put(SQLiteAdapter.ENABLED_COL, isEnabled ? 1 : 0);
			try {
				sqlAdpt.update(id, cv);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		Log.d(TAG, "Configuration: " + iniStr + " " + endStr + " " + weekdays);

		sqlAdpt.close();
		dismiss();
	}

	public void setup(int id) {
		Log.d(TAG, "Updating id " + id);
		sqlAdpt.openToRead();
		Cursor c = sqlAdpt.query(null, "id=" + id);

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

			WeekdayPicker wp = (WeekdayPicker) d
					.findViewById(R.id.weekdayPicker);
			toks = c.getString(c.getColumnIndex("weekdays")).split(",");

			if (toks[0].charAt(0) != '-') {
				for (int i = 0; i < toks.length; i++) {
					wp.toggleDay(WeekdaysEnum.getWeekday(
							Integer.parseInt(toks[i])).toString());
				}
			}
		}
		c.close();
		sqlAdpt.close();
	}

	public void showTimePicker(int id) {
		FragmentManager fm = getChildFragmentManager();
		CustomTimePicker tp;
		switch (id) {
		case 0: // start
			tp = CustomTimePicker.newInstance(0, startHour, startMinute, false);
			tp.setOnTimeSetListener(startTimeListener);
			tp.show(fm, "fragment_time_picker");
			break;
		case 1:// end
			tp = CustomTimePicker.newInstance(1, endHour, endMinute, isNonStop);
			tp.setOnTimeSetListener(endTimeListener);
			tp.show(fm, "fragment_time_picker");
		}
	}

	public void setOnCfgSetListener(OnCfgSetListener listener) {
		onCfgSetListener = listener;
	}

	public interface OnCfgSetListener {
		public abstract void onCfgSet(int id);
	}

}
