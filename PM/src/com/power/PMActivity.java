package com.power;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class PMActivity extends Activity implements OnClickListener {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		View process_button = this.findViewById(R.id.process_button);
		process_button.setOnClickListener(this);
		View exit_button = this.findViewById(R.id.exit_button);
		exit_button.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.process_button:
			Intent i_process = new Intent(this, Process.class);
			startActivity(i_process);
			break;
		case R.id.exit_button:
			finish();
			break;
		}
	}
}