package com.vag.politednd.view;

import com.vag.politednd.activity.R;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class TextPopupFragment extends DialogFragment {

	String text, title;

	String TAG = "TextPopupFragment";

	public static TextPopupFragment newInstance(String title, String content) {
		TextPopupFragment frag = new TextPopupFragment();
		Bundle args = new Bundle();
		args.putString("text", content);
		args.putString("title", title);
		frag.setArguments(args);

		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		text = getArguments().getString("text");
		title = getArguments().getString("title");

		Dialog d = new Dialog(getActivity());
		d.setContentView(R.layout.text_popup);
		d.setTitle(title);

		TextView t = (TextView) d.findViewById(R.id.teste);
		t.setText(Html.fromHtml(text));

		ImageButton btnDismiss = (ImageButton) d.findViewById(R.id.close);
		btnDismiss.setOnClickListener(new ImageButton.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		return d;
	}

}
