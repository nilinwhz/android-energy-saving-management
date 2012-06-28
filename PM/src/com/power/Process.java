package com.power;

import android.app.Activity;
import android.os.Bundle;
import com.proc.procinfo;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;

public class Process extends TabActivity {
	private static final int MENU_ITEM_SETTING = 0;
	private static final int MENU_ITEM_QUIT = MENU_ITEM_SETTING + 1;

	// 得到TabHost对象，针对TabActivity的操作通常都由这个对象完成
	private static TabHost tabHost = null;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main2);

		tabHost = getTabHost();
		// 生成一个Intent对象，该对象指向一个Activity
		Intent procIntent = new Intent();
		procIntent.setClass(this, com.proc.procinfo.class);
		// 生成一个TabSpec对象，这个对象代表了一个页
		TabHost.TabSpec procSpec = tabHost
				.newTabSpec(getString(R.string.tab_title_process));
		Resources res = getResources();
		// 设置该页的Indicator
		procSpec.setIndicator(getString(R.string.tab_title_process),
				res.getDrawable(R.drawable.process));
		// 设置该页的内容
		procSpec.setContent(procIntent);
		// 将设置好的TabSpec对象添加到TabHost当中
		tabHost.addTab(procSpec);

		Intent taskIntent = new Intent();
		taskIntent.setClass(this, Battery.class);
		TabHost.TabSpec taskSpec = tabHost
				.newTabSpec(getString(R.string.tab_title_b));
		taskSpec.setIndicator(getString(R.string.tab_title_b),
				res.getDrawable(R.drawable.task));
		taskSpec.setContent(taskIntent);
		tabHost.addTab(taskSpec);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_ITEM_SETTING, MENU_ITEM_SETTING, R.string.setting);
		menu.add(0, MENU_ITEM_QUIT, MENU_ITEM_QUIT, R.string.quit);
		menu.getItem(MENU_ITEM_SETTING).setIcon(R.drawable.setting);
		menu.getItem(MENU_ITEM_QUIT).setIcon(R.drawable.quit);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ITEM_SETTING:
			break;
		case MENU_ITEM_QUIT:
			returnToHome();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void returnToHome() {
		Intent home = new Intent(Intent.ACTION_MAIN);
		home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		home.addCategory(Intent.CATEGORY_HOME);
		startActivity(home);
	}
}