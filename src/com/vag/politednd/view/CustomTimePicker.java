package com.vag.politednd.view;

import com.vag.politednd.activity.R;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TimePicker;

public class CustomTimePicker extends DialogFragment {
	// http://tseng-blog.nge-web.net/blog/2009/02/17/how-implement-your-own-listener-android-java/

	OnTimeSetListener onTimeSetListener = null;
	TimePicker t;
	CheckBox never;

	public static CustomTimePicker newInstance(int type) {
		CustomTimePicker frag = new CustomTimePicker();
		Bundle args = new Bundle();
		args.putInt("type", type);
		args.putInt("hour", -1);
		args.putInt("minute", -1);
		args.putBoolean("never", false);
		frag.setArguments(args);

		return frag;
	}

	public static CustomTimePicker newInstance(int type, int hour, int minute,
			boolean never) {
		CustomTimePicker frag = new CustomTimePicker();
		Bundle args = new Bundle();
		args.putInt("type", type);
		args.putInt("hour", hour);
		args.putInt("minute", minute);
		args.putBoolean("never", never);
		frag.setArguments(args);

		return frag;
	}

	// Allows the user to set an Listener and react to the event
	public void setOnTimeSetListener(OnTimeSetListener listener) {
		onTimeSetListener = listener;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		int type = getArguments().getInt("type");
		int hour = getArguments().getInt("hour");
		int minute = getArguments().getInt("minute");
		boolean isNever = getArguments().getBoolean("never");

		Dialog d = new Dialog(getActivity());
		d.setContentView(R.layout.time_picker);
		d.setTitle(type == 1 ? getResources().getString(R.string.end)
				: getResources().getString(R.string.start));

		never = (CheckBox) d.findViewById(R.id.never);

		t = (TimePicker) d.findViewById(R.id.tp);
		t.setIs24HourView(true);
		if (hour != -1) {
			t.setCurrentHour(hour);
			t.setCurrentMinute(minute);
		}

		Button set = (Button) d.findViewById(R.id.set_tp);

		never.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				t.setEnabled(!isChecked);
			}
		});
		never.setChecked(isNever);

		if (type == 0) {
			never.setVisibility(View.INVISIBLE);
		}

		set.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (onTimeSetListener != null) {
					onTimeSetListener.onTimeSet(t.getCurrentHour(),
							t.getCurrentMinute(), never.isChecked());
				}

				dismiss();
			}
		});

		Button cancel = (Button) d.findViewById(R.id.cancel_tp);

		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		return d;
	}

	// Define our custom Listener interface
	public interface OnTimeSetListener {
		public abstract void onTimeSet(int hourOfDay, int minute, boolean never);
	}
}
