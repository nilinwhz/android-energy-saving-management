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
			 * 如果捕捉到的action是ACTION_BATTERY_CHANGED， 就运行onBatteryInfoReceiver()
			 */
			if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {

				sb.append("\n当前电量:").append(intent.getIntExtra("level", 0));
				sb.append("\n电池电量:").append(intent.getIntExtra("scale", 100));
				// 电池伏数
				sb.append("\n当前电压：").append(intent.getIntExtra("voltage", 0));
				// 电池温度
				sb.append("\n当前温度：").append(
						intent.getIntExtra("temperature", 0));
				String BatteryStatus = null;
				switch (intent.getIntExtra("status",
						BatteryManager.BATTERY_STATUS_UNKNOWN)) {
				case BatteryManager.BATTERY_STATUS_CHARGING:
					BatteryStatus = "充电状态";
					break;
				case BatteryManager.BATTERY_STATUS_DISCHARGING:
					BatteryStatus = "放电状态";
					break;
				case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
					BatteryStatus = "未充电";
					break;
				case BatteryManager.BATTERY_STATUS_FULL:
					BatteryStatus = "充满电";
					break;
				case BatteryManager.BATTERY_STATUS_UNKNOWN:
					BatteryStatus = "未知道状态";
					break;
				}
				sb.append("\n当前状态：").append(BatteryStatus);
				String BatteryStatus2 = null;
				switch (intent.getIntExtra("plugged",
						BatteryManager.BATTERY_PLUGGED_AC)) {
				case BatteryManager.BATTERY_PLUGGED_AC:
					BatteryStatus2 = "AC充电";
					break;
				case BatteryManager.BATTERY_PLUGGED_USB:
					BatteryStatus2 = "USB充电";
					break;
				}
				sb.append("\n充电方式：").append(BatteryStatus2);
				String BatteryTemp = null;
				switch (intent.getIntExtra("health",
						BatteryManager.BATTERY_HEALTH_UNKNOWN)) {
				case BatteryManager.BATTERY_HEALTH_UNKNOWN:
					BatteryTemp = "未知错误";
					break;
				case BatteryManager.BATTERY_HEALTH_GOOD:
					BatteryTemp = "状态良好";
					break;
				case BatteryManager.BATTERY_HEALTH_DEAD:
					BatteryTemp = "电池没有电";
					break;
				case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
					BatteryTemp = "电池电压过高";
					break;
				case BatteryManager.BATTERY_HEALTH_OVERHEAT:
					BatteryTemp = "电池过热";
					break;
				}
				sb.append("\n电池状态：").append(BatteryTemp);
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