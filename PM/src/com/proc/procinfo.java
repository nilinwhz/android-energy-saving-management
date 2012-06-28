/**
 * ActivityManager.RunningAppProcessInfo {
 *     public int importance				// ������ϵͳ�е���Ҫ����
 *     public int importanceReasonCode		// ���̵���Ҫԭ�����
 *     public ComponentName importanceReasonComponent	// �����������������Ϣ
 *     public int importanceReasonPid		// ��ǰ���̵��ӽ���Id
 *     public int lru						// ��ͬһ����Ҫ�����ڵĸ�������ֵ
 *     public int pid						// ��ǰ����Id
 *     public String[] pkgList				// �����뵱ǰ���̵����а���
 *     public String processName			// ��ǰ���̵�����
 *     public int uid						// ��ǰ���̵��û�Id
 * }
 */
package com.proc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import com.power.R;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class procinfo extends ListActivity {

	// ��ȡ���������ͻ��������ʵ��
	private static PackageManager packageManager = null;
	private static ActivityManager activityManager = null;

	// �������еĽ����б�
	private static List<RunningAppProcessInfo> runningProcessList = null;
	private static List<basic> infoList = null;
	private static wpackage packageUtil = null;
	procwaste processMemoryUtil = null;

	// ��ѡ�еĽ�������
	private static RunningAppProcessInfo processSelected = null;

	private static final String TAG_SYSTEMASSIST = "abc";

	private static Button btnRefresh = null;
	private static Button btnCloseAll = null;

	// ���ں�̨ˢ���б���ʾ������ʾ
	private static RefreshHandler handler = null;
	private static ProgressDialog pd = null;
	private static proclist procListAdapter = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.proc_list);

		btnRefresh = (Button) findViewById(R.id.btn_refresh_process);
		btnRefresh.setOnClickListener(new RefreshButtonListener());
		btnCloseAll = (Button) findViewById(R.id.btn_closeall_process);
		btnCloseAll.setOnClickListener(new CloseAllButtonListener());

		// ��ȡ������������Ҫͨ������������ȡ�����ͼ��ͳ�����
		packageManager = this.getPackageManager();
		activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		packageUtil = new wpackage(this);

		// ��ȡ�������еĽ����б�
		runningProcessList = new ArrayList<RunningAppProcessInfo>();
		infoList = new ArrayList<basic>();

		updateProcessList();
	}

	private void updateProcessList() {
		// �½�һ�����ȶԻ����ڸ����б�ʱ�������ڸ���ͼ֮��
		pd = new ProgressDialog(procinfo.this);
		pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pd.setTitle(getString(R.string.progress_tips_title));
		pd.setMessage(getString(R.string.progress_tips_content));

		// �������̣߳�ִ�и����б����
		handler = new RefreshHandler();
		RefreshThread thread = new RefreshThread();
		thread.start();

		// ��ʾ���ȶԻ���
		pd.show();
	}

	private class RefreshHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO : Update your UI
			getListView().setAdapter(procListAdapter);
			// ȡ�����ȿ�
			pd.dismiss();
		}
	}

	class RefreshThread extends Thread {
		@Override
		public void run() {
			// TODO : Do your Stuff here.
			procListAdapter = buildProcListAdapter();
			Message msg = handler.obtainMessage();
			handler.sendMessage(msg);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		processSelected = runningProcessList.get(position);

		AlertButtonListener listener = new AlertButtonListener();
		Dialog alertDialog = new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle(R.string.please_select)
				.setNegativeButton(R.string.kill_process, listener)
				.setNeutralButton(R.string.info_detail, listener).create();
		alertDialog.show();

		super.onListItemClick(l, v, position, id);
	}

	public class AlertButtonListener implements
			android.content.DialogInterface.OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case Dialog.BUTTON_NEUTRAL:
				Intent intent = new Intent();
				intent.setClass(procinfo.this, procdetail.class);
				// Ϊѡ�еĽ��̻�ȡ��װ������ϸ��Ϣ
				detail programUtil = buildProgramUtilComplexInfo(processSelected.processName);
				if (programUtil == null) {
					break;
				}
				// ʹ��Serializable��Activity֮�䴫�ݶ���
				Bundle bundle = new Bundle();
				bundle.putSerializable("process_info",
						(Serializable) programUtil);
				intent.putExtras(bundle);
				startActivity(intent);

				break;
			case Dialog.BUTTON_NEGATIVE:
				closeOneProcess(processSelected.processName);
				updateProcessList();
				break;
			default:
				break;
			}
		}
	}

	private class RefreshButtonListener implements
			android.view.View.OnClickListener {

		@Override
		public void onClick(View v) {
			updateProcessList();
		}
	}

	private class CloseAllButtonListener implements
			android.view.View.OnClickListener {

		@Override
		public void onClick(View v) {
			int count = infoList.size();
			basic bpu = null;
			for (int i = 0; i < count; i++) {
				bpu = infoList.get(i);
				closeOneProcess(bpu.getProcessName());
			}

			updateProcessList();
		}
	}

	private void closeOneProcess(String processName) {
		if (processName.equals("com.power")) {
			Toast.makeText(procinfo.this, "������ɱ!", Toast.LENGTH_LONG).show();
		}
		ApplicationInfo tempAppInfo = packageUtil
				.getApplicationInfo(processName);
		if (tempAppInfo == null) {
			return;
		}
		activityManager.killBackgroundProcesses(tempAppInfo.packageName);
		Toast.makeText(procinfo.this, "������ɱ��!", Toast.LENGTH_LONG).show();
	}

	/*
	 * ����һ��ListAdapter
	 */
	public proclist buildProcListAdapter() {

		if (!runningProcessList.isEmpty()) {
			runningProcessList.clear();
		}
		if (!infoList.isEmpty()) {
			infoList.clear();
		}
		// ��ȡ�������еĽ����б�
		runningProcessList = activityManager.getRunningAppProcesses();
		// ��ȡ��ǰ���̵��ڴ��CPU��Ϣ
		processMemoryUtil = new procwaste();
		processMemoryUtil.initPMUtil();

		RunningAppProcessInfo procInfo = null;
		for (Iterator<RunningAppProcessInfo> iterator = runningProcessList
				.iterator(); iterator.hasNext();) {
			procInfo = iterator.next();
			basic programUtil = buildProgramUtilSimpleInfo(procInfo.pid,
					procInfo.processName);
			infoList.add(programUtil);
		}

		proclist adapter = new proclist(infoList, this);
		return adapter;
	}

	/*
	 * Ϊ���̻�ȡ��������Ϣ
	 */
	public basic buildProgramUtilSimpleInfo(int procId, String procNameString) {

		basic programUtil = new basic();
		programUtil.setProcessName(procNameString);

		// ���ݽ���������ȡӦ�ó����ApplicationInfo����
		ApplicationInfo tempAppInfo = packageUtil
				.getApplicationInfo(procNameString);

		if (tempAppInfo != null) {
			// Ϊ���̼���ͼ��ͳ�������
			programUtil.setIcon(tempAppInfo.loadIcon(packageManager));
			programUtil.setProgramName(tempAppInfo.loadLabel(packageManager)
					.toString());
		} else {
			// �����ȡʧ�ܣ���ʹ��Ĭ�ϵ�ͼ��ͳ�����
			programUtil.setIcon(getApplicationContext().getResources()
					.getDrawable(R.drawable.unknown));
			programUtil.setProgramName(procNameString);
			Log.v(procinfo.TAG_SYSTEMASSIST, procNameString);
		}

		String str = processMemoryUtil.getMemInfoByName(procNameString);
		Log.v(TAG_SYSTEMASSIST, "Time --- > "
				+ Calendar.getInstance().getTimeInMillis());
		programUtil.setCpuMemString(str);
		return programUtil;
	}

	/*
	 * Ϊ���̻�ȡ��װ��������
	 */
	public detail buildProgramUtilComplexInfo(String procNameString) {

		detail complexProgramUtil = new detail();
		// ���ݽ���������ȡӦ�ó����ApplicationInfo����
		ApplicationInfo tempAppInfo = packageUtil
				.getApplicationInfo(procNameString);
		if (tempAppInfo == null) {
			return null;
		}

		PackageInfo tempPkgInfo = null;
		try {
			tempPkgInfo = packageManager.getPackageInfo(
					tempAppInfo.packageName,
					PackageManager.GET_UNINSTALLED_PACKAGES
							| PackageManager.GET_ACTIVITIES
							| PackageManager.GET_SERVICES
							| PackageManager.GET_PERMISSIONS);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (tempPkgInfo == null) {
			return null;
		}

		complexProgramUtil.setProcessName(procNameString);
		complexProgramUtil.setCompanyName(getString(R.string.no_use));
		complexProgramUtil.setVersionName(tempPkgInfo.versionName);
		complexProgramUtil.setVersionCode(tempPkgInfo.versionCode);
		complexProgramUtil.setDataDir(tempAppInfo.dataDir);
		complexProgramUtil.setSourceDir(tempAppInfo.sourceDir);

		// ��ȡ����������Ϣ����ҪΪPackageManager������Ȩ(packageManager.getPackageInfo()����)
		complexProgramUtil.setUserPermissions(tempPkgInfo.requestedPermissions);
		complexProgramUtil.setServices(tempPkgInfo.services);
		complexProgramUtil.setActivities(tempPkgInfo.activities);

		return complexProgramUtil;
	}

}