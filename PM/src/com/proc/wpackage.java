/**
 * ����ĳ���̵�ApplicationInfo����Ŀ��Ϊ�˻�ȡͼ���Ӧ�ó�������
 */
package com.proc;

import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

public class wpackage {
	// ApplicationInfo �࣬��������ͨӦ�ó������Ϣ����Ҫ��ָManifest.xml��application��ǩ�е���Ϣ
	private List<ApplicationInfo> allAppList;

	public wpackage(Context context) {
		// ͨ�������������������е�Ӧ�ó��򣨰���ж�أ�������Ŀ¼
		PackageManager pm = context.getApplicationContext().getPackageManager();
		allAppList = pm
				.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
		pm.getInstalledPackages(0);
	}

	/**
	 * ͨ��һ�����������ظó����һ��ApplicationInfo����
	 * 
	 * @param name
	 *            ������
	 * @return ApplicationInfo
	 */
	public ApplicationInfo getApplicationInfo(String appName) {
		if (appName == null) {
			return null;
		}

		for (ApplicationInfo appinfo : allAppList) {
			if (appName.equals(appinfo.processName)) {
				return appinfo;
			}
		}
		return null;
	}
}
