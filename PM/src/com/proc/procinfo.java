/**
 * ActivityManager.RunningAppProcessInfo {
 *     public int importance				// 进程在系统中的重要级别
 *     public int importanceReasonCode		// 进程的重要原因代码
 *     public ComponentName importanceReasonComponent	// 进程中组件的描述信息
 *     public int importanceReasonPid		// 当前进程的子进程Id
 *     public int lru						// 在同一个重要级别内的附加排序值
 *     public int pid						// 当前进程Id
 *     public String[] pkgList				// 被载入当前进程的所有包名
 *     public String processName			// 当前进程的名称
 *     public int uid						// 当前进程的用户Id
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

	// 获取包管理器和活动管理器的实例
	private static PackageManager packageManager = null;
	private static ActivityManager activityManager = null;

	// 正在运行的进程列表
	private static List<RunningAppProcessInfo> runningProcessList = null;
	private static List<basic> infoList = null;
	private static wpackage packageUtil = null;
	procwaste processMemoryUtil = null;

	// 被选中的进程名称
	private static RunningAppProcessInfo processSelected = null;

	private static final String TAG_SYSTEMASSIST = "abc";

	private static Button btnRefresh = null;
	private static Button btnCloseAll = null;

	// 用于后台刷新列表，显示更新提示
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

		// 获取包管理器，主要通过包管理器获取程序的图标和程序名
		packageManager = this.getPackageManager();
		activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		packageUtil = new wpackage(this);

		// 获取正在运行的进程列表
		runningProcessList = new ArrayList<RunningAppProcessInfo>();
		infoList = new ArrayList<basic>();

		updateProcessList();
	}

	private void updateProcessList() {
		// 新建一个进度对话框，在更新列表时，覆盖在父视图之上
		pd = new ProgressDialog(procinfo.this);
		pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pd.setTitle(getString(R.string.progress_tips_title));
		pd.setMessage(getString(R.string.progress_tips_content));

		// 启动新线程，执行更新列表操作
		handler = new RefreshHandler();
		RefreshThread thread = new RefreshThread();
		thread.start();

		// 显示进度对话框
		pd.show();
	}

	private class RefreshHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO : Update your UI
			getListView().setAdapter(procListAdapter);
			// 取消进度框
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
				// 为选中的进程获取安装包的详细信息
				detail programUtil = buildProgramUtilComplexInfo(processSelected.processName);
				if (programUtil == null) {
					break;
				}
				// 使用Serializable在Activity之间传递对象
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
			Toast.makeText(procinfo.this, "不能自杀!", Toast.LENGTH_LONG).show();
		}
		ApplicationInfo tempAppInfo = packageUtil
				.getApplicationInfo(processName);
		if (tempAppInfo == null) {
			return;
		}
		activityManager.killBackgroundProcesses(tempAppInfo.packageName);
		Toast.makeText(procinfo.this, "进程已杀掉!", Toast.LENGTH_LONG).show();
	}

	/*
	 * 构建一个ListAdapter
	 */
	public proclist buildProcListAdapter() {

		if (!runningProcessList.isEmpty()) {
			runningProcessList.clear();
		}
		if (!infoList.isEmpty()) {
			infoList.clear();
		}
		// 获取正在运行的进程列表
		runningProcessList = activityManager.getRunningAppProcesses();
		// 获取当前进程的内存和CPU信息
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
	 * 为进程获取基本的信息
	 */
	public basic buildProgramUtilSimpleInfo(int procId, String procNameString) {

		basic programUtil = new basic();
		programUtil.setProcessName(procNameString);

		// 根据进程名，获取应用程序的ApplicationInfo对象
		ApplicationInfo tempAppInfo = packageUtil
				.getApplicationInfo(procNameString);

		if (tempAppInfo != null) {
			// 为进程加载图标和程序名称
			programUtil.setIcon(tempAppInfo.loadIcon(packageManager));
			programUtil.setProgramName(tempAppInfo.loadLabel(packageManager)
					.toString());
		} else {
			// 如果获取失败，则使用默认的图标和程序名
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
	 * 为进程获取安装包的详情
	 */
	public detail buildProgramUtilComplexInfo(String procNameString) {

		detail complexProgramUtil = new detail();
		// 根据进程名，获取应用程序的ApplicationInfo对象
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

		// 获取以下三个信息，需要为PackageManager进行授权(packageManager.getPackageInfo()方法)
		complexProgramUtil.setUserPermissions(tempPkgInfo.requestedPermissions);
		complexProgramUtil.setServices(tempPkgInfo.services);
		complexProgramUtil.setActivities(tempPkgInfo.activities);

		return complexProgramUtil;
	}

}