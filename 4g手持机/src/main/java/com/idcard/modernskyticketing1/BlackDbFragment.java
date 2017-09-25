package com.idcard.modernskyticketing1;

import java.util.List;

import com.huashi.lua.bean.BlackInfo;
import com.huashi.lua.db.utlis.BlackIDCardDao;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BlackDbFragment extends Fragment {

	private ListView lv;
	private List<BlackInfo> ics;
	private BlackIDCardDao dao;
	private Button bt_lr;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_black_db, container, false);
		lv = (ListView) view.findViewById(R.id.lv);
		bt_lr = (Button) view.findViewById(R.id.bt_lr);
		initView();
		initData();
		return view;
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		dao = new BlackIDCardDao(getActivity());
	}

	private void initView(){
		ics = dao.getAllInfos();
		lv.setAdapter(new MyAdapter());
	}

	private void initData() {
		// TODO Auto-generated method stub
		bt_lr.setOnClickListener(new OnClickListener() {
			private EditText et_name, et_idcard;
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				View view = View.inflate(getActivity(), R.layout.dialog_black, null);
				et_name = (EditText) view.findViewById(R.id.et_black_name);
				et_idcard = (EditText) view.findViewById(R.id.et_black_idcard);
				builder.setView(view);
				builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						BlackInfo info = new BlackInfo();
						String name = et_name.getText().toString().trim();
						String idcard = et_idcard.getText().toString().trim();
						if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(idcard)) {
							info.setName(name);
							info.setIdcard(idcard);
							int ret = dao.addInfo(info);
							if (ret != -1) {
								initView();
								Toast.makeText(getActivity(), "添加成功", Toast.LENGTH_LONG).show();
							}
						}
					}
				});
				builder.setNegativeButton("取消", null);
				builder.setCancelable(false);
				builder.create().show();

			}
		});
	}

	class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return ics.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v;
			ViewHolder hodler;
			if (convertView == null) {
				v = View.inflate(getActivity(), R.layout.item3, null);
				hodler = new ViewHolder();
				hodler.tv_name = (TextView) v.findViewById(R.id.tv_name);
				hodler.tv_idNo = (TextView) v.findViewById(R.id.tv_idNo);
				convertView = v;
				convertView.setTag(hodler);
			} else {
				v = convertView;
				hodler = (ViewHolder) convertView.getTag();
			}
			BlackInfo ic = ics.get(position);
			if (ic == null) {
				return v;
			}
			hodler.tv_name.setText(ic.getName());
			hodler.tv_idNo.setText(ic.getIdcard());
			return v;
		}
	}

	class ViewHolder {
		private TextView tv_name, tv_sex, tv_idNo, tv_finger;
	}


}
