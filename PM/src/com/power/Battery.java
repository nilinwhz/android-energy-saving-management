package com.power;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

public class Battery extends Activity {
	TextView mLog;
	DateFormat mDateFormat;
	IntentFilter mFilter;
	PowerManager.WakeLock mWakeLock;
	SpinThread mThread;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.test);

		findViewById(R.id.checkbox).setOnClickListener(mClickListener);
		mLog = (TextView) findViewById(R.id.log);

		mDateFormat = DateFormat.getInstance();

		mFilter = new IntentFilter();
		mFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
		mFilter.addAction(Intent.ACTION_BATTERY_LOW);
		mFilter.addAction(Intent.ACTION_BATTERY_OKAY);
		mFilter.addAction(Intent.ACTION_POWER_CONNECTED);

		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		mWakeLock = pm
				.newWakeLock(PowerManager.FULL_WAKE_LOCK, "BatteryWaster");
		mWakeLock.setReferenceCounted(false);
	}

	@Override
	public void onPause() {
		// super.stopRunning();
		super.onPause();
	}

	View.OnClickListener mClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			CheckBox checkbox = (CheckBox) v;
			if (checkbox.isChecked()) {
				startRunning();
			} else {
				stopRunning();
				// log("stopRunning()");
			}
		}
	};

	void startRunning() {
		log("Start");
		registerReceiver(mReceiver, mFilter);
		mWakeLock.acquire();
		if (mThread == null) {
			mThread = new SpinThread();
			mThread.start();
		}
	}

	void stopRunning() {
		log("Stop");
		unregisterReceiver(mReceiver);
		mWakeLock.release();
		if (mThread != null) {
			mThread.quit();
			mThread = null;
		}
	}

	void log(String s) {
		mLog.setText(mLog.getText() + "\n" + mDateFormat.format(new Date())
				+ ": " + s);
	}

	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			StringBuffer sb = new StringBuffer();
			String action = intent.getAction();
			/*
			 * �����׽����action��ACTION_BATTERY_CHANGED�� ������onBatteryInfoReceiver()
			 */
			if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {

				sb.append("\n��ǰ����:").append(intent.getIntExtra("level", 0));
				sb.append("\n��ص���:").append(intent.getIntExtra("scale", 100));
				// ��ط���
				sb.append("\n��ǰ��ѹ��").append(intent.getIntExtra("voltage", 0));
				// ����¶�
				sb.append("\n��ǰ�¶ȣ�").append(
						intent.getIntExtra("temperature", 0));
				String BatteryStatus = null;
				switch (intent.getIntExtra("status",
						BatteryManager.BATTERY_STATUS_UNKNOWN)) {
				case BatteryManager.BATTERY_STATUS_CHARGING:
					BatteryStatus = "���״̬";
					break;
				case BatteryManager.BATTERY_STATUS_DISCHARGING:
					BatteryStatus = "�ŵ�״̬";
					break;
				case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
					BatteryStatus = "δ���";
					break;
				case BatteryManager.BATTERY_STATUS_FULL:
					BatteryStatus = "������";
					break;
				case BatteryManager.BATTERY_STATUS_UNKNOWN:
					BatteryStatus = "δ֪��״̬";
					break;
				}
				sb.append("\n��ǰ״̬��").append(BatteryStatus);
				String BatteryStatus2 = null;
				switch (intent.getIntExtra("plugged",
						BatteryManager.BATTERY_PLUGGED_AC)) {
				case BatteryManager.BATTERY_PLUGGED_AC:
					BatteryStatus2 = "AC���";
					break;
				case BatteryManager.BATTERY_PLUGGED_USB:
					BatteryStatus2 = "USB���";
					break;
				}
				sb.append("\n��緽ʽ��").append(BatteryStatus2);
				String BatteryTemp = null;
				switch (intent.getIntExtra("health",
						BatteryManager.BATTERY_HEALTH_UNKNOWN)) {
				case BatteryManager.BATTERY_HEALTH_UNKNOWN:
					BatteryTemp = "δ֪����";
					break;
				case BatteryManager.BATTERY_HEALTH_GOOD:
					BatteryTemp = "״̬����";
					break;
				case BatteryManager.BATTERY_HEALTH_DEAD:
					BatteryTemp = "���û�е�";
					break;
				case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
					BatteryTemp = "��ص�ѹ����";
					break;
				case BatteryManager.BATTERY_HEALTH_OVERHEAT:
					BatteryTemp = "��ع���";
					break;
				}
				sb.append("\n���״̬��").append(BatteryTemp);
				log(sb.toString());
			}
		}
	};

	class SpinThread extends Thread {
		private boolean mStop;

		public void quit() {
			synchronized (this) {
				mStop = true;
			}
		}

		public void run() {
			while (true) {
				synchronized (this) {
					if (mStop) {
						return;
					}
				}
			}
		}
	}
}