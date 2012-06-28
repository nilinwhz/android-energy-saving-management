/**
 * 获取某进程的CPU和内存使用情况
 */
package com.proc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.util.Log;
import com.cmd.CMDExecute;

public class procwaste {

	private static final int INDEX_FIRST = -1;
	private static final int INDEX_CPU = INDEX_FIRST + 3;
	private static final int INDEX_RSS = INDEX_FIRST + 7;
	private static final int INDEX_NAME = INDEX_FIRST + 10;
	private static final int Length_ProcStat = 10;

	private List<String[]> PMUList = null;

	public procwaste() {

	}

	private String getProcessRunningInfo() {
		Log.i("fetch_process_info", "start. . . . ");
		String result = null;
		CMDExecute cmdexe = new CMDExecute();
		try {
			String[] args = { "/system/bin/top", "-n", "1" };
			result = cmdexe.run(args, "/system/bin/");
		} catch (IOException ex) {
			Log.i("fetch_process_info", "ex=" + ex.toString());
		}
		return result;
	}

	private int parseProcessRunningInfo(String infoString) {
		String tempString = "";
		boolean bIsProcInfo = false;

		String[] rows = null;
		String[] columns = null;
		rows = infoString.split("[\n]+"); // 使用正则表达式分割字符串

		for (int i = 0; i < rows.length; i++) {
			tempString = rows[i];
			if (tempString.indexOf("PID") == -1) {
				if (bIsProcInfo == true) {
					tempString = tempString.trim();
					columns = tempString.split("[ ]+");
					if (columns.length == Length_ProcStat) {
						PMUList.add(columns);
					}
				}
			} else {
				bIsProcInfo = true;
			}
		}

		return PMUList.size();
	}

	// 初始化所有进程的CPU和内存列表，用于检索每个进程的信息
	public void initPMUtil() {
		PMUList = new ArrayList<String[]>();
		String resultString = getProcessRunningInfo();
		parseProcessRunningInfo(resultString);
	}

	// 根据进程名获取CPU和内存信息
	public String getMemInfoByName(String procName) {
		String result = "";
		String item1 = "";
		String item2 = "";
		String item3 = "";
		String item4 = "";
		double tmem = 0;
		double mem = 0;
		double res = 0.00;
		String tempString = "";
		for (Iterator<String[]> iterator0 = PMUList.iterator(); iterator0
				.hasNext();) {
			String[] item0 = (String[]) iterator0.next();
			item1 = item0[INDEX_RSS]
					.substring(0, item0[INDEX_RSS].length() - 1);
			tmem = Integer.valueOf(item1).intValue() + tmem;
		}
		for (Iterator<String[]> iterator = PMUList.iterator(); iterator
				.hasNext();) {
			String[] item = (String[]) iterator.next();
			tempString = item[INDEX_NAME];
			if (tempString != null && tempString.equals(procName)) {
				// result = "CPU:" + item[INDEX_CPU] + "  Mem:" +
				// item[INDEX_RSS];
				item2 = item[INDEX_RSS].substring(0,
						item[INDEX_RSS].length() - 1);
				mem = Integer.valueOf(item2).intValue();
				res = mem / tmem;
				item3 = String.valueOf(res).substring(3, 4);
				item4 = String.valueOf(res).substring(4, 6);
			    result = "耗电： " + item3 + "." + item4 + "% ";	
				break;
			}
		}
		return result;
	}
}
