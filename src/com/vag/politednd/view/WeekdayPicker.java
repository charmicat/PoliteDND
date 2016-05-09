package com.vag.politednd.view;

import java.util.Iterator;
import java.util.LinkedHashMap;

import com.vag.politednd.activity.R;
import com.vag.politednd.shared.PDNDConstants.WeekdaysEnum;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import android.view.View.OnClickListener;

public class WeekdayPicker extends LinearLayout implements OnClickListener {

	LinkedHashMap<String, Integer> days;

	String TAG = "WeekdayPicker";

	public WeekdayPicker(Context context, AttributeSet attrs) {
		super(context, attrs);

		LayoutInflater layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.weekday_picker, this);

		days = new LinkedHashMap<String, Integer>(7);

		days.put(getResources().getString(R.string.mo), R.id.mo);
		days.put(getResources().getString(R.string.tu), R.id.tu);
		days.put(getResources().getString(R.string.we), R.id.we);
		days.put(getResources().getString(R.string.th), R.id.th);
		days.put(getResources().getString(R.string.fr), R.id.fr);
		days.put(getResources().getString(R.string.sa), R.id.sa);
		days.put(getResources().getString(R.string.su), R.id.su);

		for (String s : days.keySet()) {
			findViewById(days.get(s)).setOnClickListener(this);
		}
	}

	public void toggleDay(String day) {
		Log.d(TAG, "Toggling day " + day + " " + Math.abs(days.get(day)));
		ToggleButton b = (ToggleButton) findViewById(Math.abs(days.get(day)));

		days.put(day, days.get(day) * -1); // toggled buttons have negative ids

		b.setChecked(b.isChecked() ? false : true);
	}

	@Override
	public void onClick(View v) {
		ToggleButton b = (ToggleButton) v;

		days.put((String) b.getTextOn(), days.get((String) b.getTextOn()) * -1);
	}

	public String getSelectedDays() {
		String daysStr = "";

		Iterator<String> iterator = days.keySet().iterator();
		boolean foundSelection = false;

		while (iterator.hasNext()) {
			String key = iterator.next();
			if (days.get(key) < 0) {
				foundSelection = true;
				daysStr += WeekdaysEnum.getWeekday(key).getCode() + ",";
			}
		}

		daysStr = daysStr.replaceFirst(",$", ""); // remover ultima virgula

		if (!foundSelection)
			daysStr = "-";

		Log.d(TAG, "Selected days: " + daysStr);

		return daysStr;
	}

}
