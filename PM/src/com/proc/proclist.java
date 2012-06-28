/**
 * �Զ�����б�������
 */
package com.proc;

import java.util.ArrayList;
import java.util.List;

import com.power.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class proclist extends BaseAdapter {

	List<basic> list = new ArrayList<basic>();
	// ��LayoutInflater���ڽ�һ��XML�����ļ�ʵ����Ϊһ��View����һ�㲻ֱ��ʹ�ã���ֹ������
	LayoutInflater layoutInflater;
	Context context;

	// ���캯��������Ϊ�б���� �� Context
	public proclist(List<basic> list, Context context) {
		this.list = list;
		this.context = context;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	// ��ȡ��ʾ���ݵ��б��View����
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder = null;

		if (convertView == null) {
			// �Ӹ�����context�л�ȡLayoutInflater����
			layoutInflater = LayoutInflater.from(context);
			// ����inflate() - ���ض���XML�����ļ�inflate���ͳ�һ���µ���ͼ
			convertView = layoutInflater.inflate(R.layout.proc_list_item, null);

			holder = new ViewHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.image_app);
			holder.nameText = (TextView) convertView
					.findViewById(R.id.name_app);
			holder.cpumemInfo = (TextView) convertView
					.findViewById(R.id.cpumem_app);

			// Ϊһ��View��ӱ�ǩ����ǩ������ViewHolder�ж��壬���Կ�����һ���󶨲���
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final basic pUtils = list.get(position);
		// ����ͼ��
		holder.image.setImageDrawable(pUtils.getIcon());
		// ���ó�����
		holder.nameText.setText(pUtils.getProgramName());
		// ����Cpu���ڴ���Ϣ
		holder.cpumemInfo.setText(pUtils.getCpuMemString());

		return convertView;
	}
}

/**
 * ��ViewHolder�ĳ�Ա������Ϊһ��View��Tag��
 * 
 */
class ViewHolder {
	TextView nameText;
	TextView cpumemInfo;
	ImageView image;
}
