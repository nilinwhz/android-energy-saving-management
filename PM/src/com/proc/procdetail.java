package com.proc;

import com.proc.wpackage;

import com.power.R;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class procdetail extends Activity {
	int time0 = 0;
	private detail processInfo = null;
	private static TextView textProcessName = null;
	private static TextView textProcessVersion = null;
	private static TextView textInstallDir = null;
	private static TextView textDataDir = null;
	private static TextView textPkgSize = null;
	private static TextView textPermission = null;
	private static TextView textService = null;
	private static TextView textActivity = null;
	private static Button btnKillProcess = null;
	private static EditText text = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.proc_detail);
		textProcessName = (TextView) findViewById(R.id.detail_process_name);
		textProcessVersion = (TextView) findViewById(R.id.detail_process_copyright);
		textInstallDir = (TextView) findViewById(R.id.detail_process_install_dir);
		textDataDir = (TextView) findViewById(R.id.detail_process_data_dir);
		textPkgSize = (TextView) findViewById(R.id.detail_process_package_size);
		textPermission = (TextView) findViewById(R.id.detail_process_permission);
		textService = (TextView) findViewById(R.id.detail_process_service);
		textActivity = (TextView) findViewById(R.id.detail_process_activity);
		btnKillProcess = (Button) findViewById(R.id.btn_kill_process);
		btnKillProcess.setOnClickListener(new KillButtonListener());

		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		processInfo = (detail) bundle.getSerializable("process_info");

		showAppInfo();
	}

	public void showAppInfo() {

		textProcessName.setText(processInfo.getProcessName());
		textInstallDir.setText(processInfo.getSourceDir());
		textProcessVersion.setText(getString(R.string.detail_process_company)
				+ processInfo.getCompanyName() + "  "
				+ getString(R.string.detail_process_version)
				+ processInfo.getVersionName() + "("
				+ processInfo.getVersionCode() + ")");
		textDataDir.setText(processInfo.getDataDir());
		textPkgSize.setText(processInfo.getPackageSize());
		textPermission.setText(processInfo.getUserPermissions());
		textService.setText(processInfo.getServices());
		textActivity.setText(processInfo.getActivities());
	}

	private class KillButtonListener implements OnClickListener {

		@Override
		public void onClick(View v) {

			if (processInfo.getProcessName().equals("com.power")) {
				Toast.makeText(getApplicationContext(),
						"不能自杀!", Toast.LENGTH_LONG).show();
				return;
			}

			text = new EditText(v.getContext());

			new AlertDialog.Builder(v.getContext())
					.setTitle("请输入时间")
					.setIcon(android.R.drawable.ic_dialog_info)
					.setView(text)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub

									time0 = Integer.valueOf(text.getText()
											.toString());
									new Thread(new Runnable() {
										@Override
										public void run() {
											// TODO Auto-generated method stub
											final ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
											wpackage packageUtil = new wpackage(
													procdetail.this);
											final ApplicationInfo tempAppInfo = packageUtil
													.getApplicationInfo(processInfo
															.getProcessName());
											try {
												Thread.sleep(time0 * 1000);

												activityManager
														.killBackgroundProcesses(tempAppInfo.packageName);

												Looper.prepare();
												Toast.makeText(
														getApplicationContext(),
														"进程已杀掉!",
														Toast.LENGTH_LONG)
														.show();
												Looper.loop();

											} catch (Exception e) {
												e.printStackTrace();
											}
										}
									}).start();
								}
							}).setNegativeButton("取消", null).show();

		}
	}
}
