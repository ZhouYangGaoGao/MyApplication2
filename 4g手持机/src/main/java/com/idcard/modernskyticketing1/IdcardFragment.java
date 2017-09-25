package com.idcard.modernskyticketing1;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;

import com.huashi.lua.db.utlis.BlackIDCardDao;
import com.huashi.lua.db.utlis.IDCardDao;
import com.huashi.serialport.sdk.HsSerialPortSDK;
import com.huashi.serialport.sdk.IDCardInfo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.aratek.fp.Bione;
import cn.com.aratek.fp.FingerprintImage;
import cn.com.aratek.fp.FingerprintScanner;
import cn.com.aratek.util.Result;

public class IdcardFragment extends Fragment {

	private static final String TAG = "IdcardFragment";
	private static final String FP_DB_PATH = "/sdcard/fp.db";
	private IPowerManager mPower;
	private SoundPool soundPool;
	private int mId;
	private FingerprintTask mTask;
	private IDCardInfo icFp;
	private ProgressDialog mProgressDialog;
	private TextView tv_sam, tv_info;

	int version = Integer.parseInt(Build.VERSION.SDK);

	private ImageView iv_photo;
	private Button bt_readidcard, bt_fingerbd, bt_hyblack;
	private IDCardTask task;
	private FingerprintScanner mScanner;
	private SharedPreferences sp;
	private HsSerialPortSDK sdk;
	static String filepath = "";
	private IDCardDao dao;
	private BlackIDCardDao blackDao;
	private IDCardInfo hyBlackInfo;

	private static final int MSG_SHOW_PROGRESS_DIALOG = 7;
	private static final int MSG_DISMISS_PROGRESS_DIALOG = 8;
	private static final int MSG_SHOW_ERROR = 9;
	private static final int MSG_SHOW_INFO = 6;
	private static final int SAM_SUCCEED = 5;
	private static final int READER_IDCARD_SUCCEED = 1;
	private static final int PHOTO_SUCCEED = 2;

	SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月dd日");// 设置日期格式


	private FragmentListener mFragListner;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mFragListner = (FragmentListener) activity;
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		// TODO Auto-generated method stub
		Log.e(TAG, "onHiddenChanged");
		super.onHiddenChanged(hidden);
		if (hidden) {
				//tv_sam.setText(null);
			cz();
			closeDevice();
		//	closeFpDevice();
		}else {
			openDevice();
		//	openFpDevice();
		}
	}




	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case MSG_SHOW_PROGRESS_DIALOG: {
					String[] info = (String[]) msg.obj;
					mProgressDialog.setTitle(info[1]);
					mProgressDialog.setMessage(info[1]);
					mProgressDialog.show();
					break;
				}
				case MSG_DISMISS_PROGRESS_DIALOG: {
					mProgressDialog.dismiss();
					break;
				}
				case MSG_SHOW_ERROR: {
					getActivity().showDialog(0, (Bundle) msg.obj);
					break;
				}
				case MSG_SHOW_INFO: {
					Toast.makeText(getActivity(), (String) msg.obj, Toast.LENGTH_SHORT).show();
					break;
				}
				case SAM_SUCCEED: {
					//tv_sam.setText("SAM号" + sdk.GetSAM());
					break;
				}
				case READER_IDCARD_SUCCEED: {

					IDCardInfo ic = (IDCardInfo) msg.obj;

					/*
					hyBlackInfo = (IDCardInfo) msg.obj;
//					if (!dao.Contains(ic)) {
//						int ret = dao.addInfo(ic);
//					}
					int ret = dao.addInfo(ic);
//					if (!ExcelUtlis.Contains(ic)) {
						int index = sp.getInt("xls_index", 1);
						boolean reslut = ExcelUtlis.addContent(getActivity(), index, ic);
						if (reslut) {
							Toast.makeText(getActivity(), "录入成功", Toast.LENGTH_LONG).show();
							index++;
							sp.edit().putInt("xls_index", index).commit();
						} else {
							Toast.makeText(getActivity(), "录入失败", Toast.LENGTH_LONG).show();
						}
//					}
*/

					/*
					byte[] fp = new byte[1024];
					fp = ic.getFpDate();
					String m_FristPFInfo = "";
					String m_SecondPFInfo = "";

					if (fp[4] == 1) {
						m_FristPFInfo = String.format("指位：%s。指纹质量：%d \n", GetFPcode(fp[5]), fp[6]);
						icFp = ic;
						bt_fingerbd.setEnabled(true);
					} else {

						m_FristPFInfo = "身份证无指纹 \n";
					}
					if (fp[512 + 4] == 1) {
						m_SecondPFInfo = String.format("指位：%s。指纹质量：%d \n", GetFPcode(fp[512 + 5]), fp[512 + 6]);

					} else {
						m_SecondPFInfo = "身份证无指纹 \n";
					}
					*/
					tv_info.setText("姓名：" + ic.getPeopleName() + "\n" + "性别：" + ic.getSex() + "\n"
							+ "身份证号：" + ic.getIDCard() + "\n"
							+ "有效期：" + ic.getStrartDate() + "-" + ic.getEndDate() );

					mFragListner.onGetID(ic.getIDCard());
					break;
				}
				case PHOTO_SUCCEED: {
					try {
						FileInputStream fis = new FileInputStream(filepath + "/zp.bmp");
						Bitmap bmp = BitmapFactory.decodeStream(fis);
						fis.close();
						iv_photo.setImageBitmap(bmp);
					} catch (FileNotFoundException e) {
						showInfoToast("头像不存在");
						// Toast.makeText(getApplicationContext(), "头像不存在！",
						// Toast.LENGTH_SHORT).show();
					} catch (IOException e) {
						// TODO 自动生成的 catch 块
						showInfoToast("头像读取失败");
						// Toast.makeText(getApplicationContext(), "头像读取错误",
						// Toast.LENGTH_SHORT).show();
					} catch (Exception e) {
						showInfoToast("头像解码失败");
						// Toast.makeText(getApplicationContext(), "头像解码失败",
						// Toast.LENGTH_SHORT).show();
					}
					break;
				}
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.e(TAG, "onCreateView");


		View view = inflater.inflate(R.layout.fragment_idcard, container, false);
		tv_sam = (TextView) view.findViewById(R.id.tv_sam);
		tv_info = (TextView) view.findViewById(R.id.tv_info);
		iv_photo = (ImageView) view.findViewById(R.id.iv_image);
		bt_readidcard = (Button) view.findViewById(R.id.bt_readidcard);
		bt_hyblack = (Button) view.findViewById(R.id.bt_hyblack);
		bt_hyblack.setVisibility(View.GONE);
		bt_fingerbd = (Button) view.findViewById(R.id.bt_fingerbd);
		bt_fingerbd.setVisibility(View.GONE);
		cz();
		bt_readidcard.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				task = new IDCardTask();
				task.execute();
			}
		});

		/*
		bt_hyblack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (hyBlackInfo == null) {
					return;
				}
				boolean ret = blackDao.Contains(hyBlackInfo.getIDCard());
				if (ret) {
					Toast.makeText(getActivity(), "存在黑名单", Toast.LENGTH_LONG).show();
					hyBlackInfo = null;
				}else {
					Toast.makeText(getActivity(), "不存在黑名单", Toast.LENGTH_LONG).show();
				}
			}
		});
		bt_fingerbd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.e(TAG, "onClick: 比对指纹" );
				mTask = new FingerprintTask();
				mTask.execute("icBd");
			}
		});
		*/
		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.e(TAG, "onCreate");
		mPower = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
		super.onCreate(savedInstanceState);
		sp = getActivity().getSharedPreferences("config", getActivity().MODE_PRIVATE);
		// 获取实例
		mScanner = FingerprintScanner.getInstance();
		dao = new IDCardDao(getActivity());
		blackDao = new BlackIDCardDao(getActivity());
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		Log.e(TAG, "onStart");
		super.onStart();
		if (isHidden()) {
			return;
		}
		// 进度条对话框
		mProgressDialog = new ProgressDialog(getActivity());
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setIcon(android.R.drawable.ic_dialog_info);
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setCancelable(false);
		soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
		soundPool.load(getActivity(), R.raw.bi, 1);
		if (sdk != null) {
			return;
		}
		openDevice();
		//openFpDevice();
		SystemClock.sleep(500);
		/*
		boolean flag = sp.getBoolean("isCreateXls", false);
		if (!flag) {
			boolean reslut = ExcelUtlis.createXls();
			sp.edit().putBoolean("isCreateXls", reslut).commit();
			Toast.makeText(getActivity(), "创建xls成功", Toast.LENGTH_LONG).show();
		}
		*/
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		Log.e(TAG, "onDestroyView");
		super.onDestroyView();
		tv_sam.setText(null);
		cz();
		closeDevice();
		//closeFpDevice();
	}


	private void bfraw(int count) {
		soundPool.play(1, 1, 1, 0, count, 1);
	}

	private void cz() {
		Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.face);
		// iv_photo.setImageResource(R.drawable.nophoto);
		iv_photo.setImageBitmap(bmp);
		tv_info.setText("姓名：" + "\n" + "性别：" + "\n"
				+ "身份证号：" + "\n" + "有效期：");
		bt_fingerbd.setEnabled(false);
	}

	private long startTime;

	private class IDCardTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			cz();
			bfraw(0);
		}

		@Override
		protected Void doInBackground(Void... params) {
			// showProgressDialog("请稍后⋯⋯","正在读卡");
			// cz();
			startTime = System.currentTimeMillis();
			int i = 8;
			while (i > 0) {
				i--;
				if (sdk == null) {
					break;
				}
				if (sdk.Authenticate(150) == 0) {
					IDCardInfo ic = new IDCardInfo();
					if (sdk.Read_Card(ic, 2300) == 0) {
						int bf = 2;
						if (version == 22){
							bf = 1;
						}
						bfraw(bf);
						long endTime = System.currentTimeMillis() - startTime;
						i = 0;
						mHandler.sendMessage(mHandler.obtainMessage(READER_IDCARD_SUCCEED, ic));
						int ret = sdk.Unpack(ic.getwltdata());
						if (ret == 0) {
							showInfoToast("读卡成功,用时：" + endTime + "ms");
							mHandler.sendMessage(mHandler.obtainMessage(PHOTO_SUCCEED));
						} else {
							showInfoToast("照片解码失败,用时：" + endTime + "ms");
						}
					}
				} else {
					if (i == 0) {
						showInfoToast("卡认证失败");
						break;
					}
					SystemClock.sleep(200);
					continue;
					// showInfoToast("卡认证失败");
					// Toast.makeText(IDCardActivity.this, "卡认证失败",
					// Toast.LENGTH_LONG).show();
				}
			}
			if (version == 22){
				try{
					mPower.SetCardPower(0);
					mPower.SetCardPower(1);
				}catch (Exception e){

				}
			}else {
				try {
					HsUtlis.IDCardPonoff1();
					HsUtlis.IDCardPonwer1();
				} catch (IOException e) {
				}
			}
			// dismissProgressDialog();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
		}

	}

	private class FingerprintTask extends AsyncTask<String, Integer, Void> {
		private boolean mIsDone = false;

		@Override
		protected void onPreExecute() {
			bt_fingerbd.setEnabled(true);
		}

		@Override
		protected Void doInBackground(String... params) {
			// long startTime, captureTime = -1, extractTime = -1,
			// generalizeTime = -1, verifyTime = -1;
			FingerprintImage fi = null;
			byte[] fpFeat = null, fpTemp = null;
			Result res;
			if (params[0].equals("icBd")) {

				do {

					showProgressDialog(getString(R.string.loading), getString(R.string.press_finger));
					mScanner.prepare();
					startTime = System.currentTimeMillis();
					do {
						// startTime = System.currentTimeMillis();
						res = mScanner.capture();
						// captureTime = System.currentTimeMillis() - startTime;
					} while (res.error == FingerprintScanner.NO_FINGER && !isCancelled()
							&& (System.currentTimeMillis() - startTime) < 8000);// 判断没有检测到指纹
					// 且没有取消任务
					mScanner.finish();// 采集指纹结束
					if (isCancelled()) {
						break;
					}
					if (res.error != FingerprintScanner.RESULT_OK) {
						showErrorDialog(getString(R.string.capture_image_failed), "");
						break;
					}
					fi = (FingerprintImage) res.data;
					Log.i(TAG, "Fingerprint image quality is " + Bione.getFingerprintQuality(fi));

					res = Bione.extractFeature(fi);
					if (res.error != Bione.RESULT_OK) {
						showErrorDialog(getString(R.string.enroll_failed_because_of_extract_feature), "");
						break;
					}
					fpFeat = (byte[]) res.data;
					if (icFp == null) {
						showInfoToast("身份证无指纹");
						break;
					}
					Result result = Bione.idcardVerify(icFp.getFpDate(), fpFeat);
					Log.e(TAG, result.error + "===" + result.data);
					if ((boolean) result.data) {
						showInfoToast("比对成功,是本人");
					} else {
						showInfoToast("比对失败,不是本人");
					}
				} while (false);

				dismissProgressDialog();
			}
			mIsDone = true;
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
		}

		@Override
		protected void onCancelled() {
		}

		public void waitForDone() {
			while (!mIsDone) {
			}
		}
	}

	private void showProgressDialog(String title, String message) {
		mHandler.sendMessage(mHandler.obtainMessage(MSG_SHOW_PROGRESS_DIALOG, new String[] { title, message }));
	}

	private void dismissProgressDialog() {
		mHandler.sendMessage(mHandler.obtainMessage(MSG_DISMISS_PROGRESS_DIALOG));
	}

	private void showErrorDialog(String operation, String errString) {
		Bundle bundle = new Bundle();
		bundle.putString("operation", operation);
		bundle.putString("errString", errString);
		mHandler.sendMessage(mHandler.obtainMessage(MSG_SHOW_ERROR, bundle));
	}

	private void showInfoToast(String info) {
		mHandler.sendMessage(mHandler.obtainMessage(MSG_SHOW_INFO, info));
	}

	/**
	 * 指纹 指位代码
	 *
	 * @param FPcode
	 * @return
	 */
	String GetFPcode(int FPcode) {
		switch (FPcode) {
			case 11:
				return "右手拇指";
			case 12:
				return "右手食指";
			case 13:
				return "右手中指";
			case 14:
				return "右手环指";
			case 15:
				return "右手小指";
			case 16:
				return "左手拇指";
			case 17:
				return "左手食指";
			case 18:
				return "左手中指";
			case 19:
				return "左手环指";
			case 20:
				return "左手小指";
			case 97:
				return "右手不确定指位";
			case 98:
				return "左手不确定指位";
			case 99:
				return "其他不确定指位";
			default:
				return "未知";
		}
	}

	private void openDevice() {
		new Thread() {
			@Override
			public void run() {
				super.run();

				// showInfoToast(version+"");
				if (version == 22){
					try {
						mPower.SetCardPower(1);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}else {
					try {
						HsUtlis.IDCardPonwer1();
					} catch (IOException e) {
						showErrorDialog("上电失败","");
						return;
					}
				}
//				try {
//					HsUtlis.IDCardPonwer1();
//				} catch (IOException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
				if (sdk == null) {
					Log.e("huashi", "初始化");
					try {
						sdk = new HsSerialPortSDK(getActivity());
						showProgressDialog("正在加载", "设备正在准备,请稍后⋯⋯");
					} catch (Exception e) {
						showErrorDialog("初始化失败", "");
						return;
					} finally {
						SystemClock.sleep(1000);
						filepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/wltlib";// 授权目录
						int ret = sdk.init("/dev/ttyMT1", 115200, 0);
						if (ret == 0) {
							mHandler.sendMessage(mHandler.obtainMessage(SAM_SUCCEED));
							showInfoToast("身份证模块准备成功");
							// tv_sam.setText("SAM号:"+SAM);
							// Toast.makeText(IDCardActivity.this,"身份证模块准备成功"+SAM,Toast.LENGTH_LONG).show();
						} else {
							showInfoToast("身份证模块准备失败");
							Toast.makeText(getActivity(), "身份证模块准备失败", Toast.LENGTH_LONG).show();
						}
						// openFpDevice();
						dismissProgressDialog();
					}
				} else {
					return;
				}

			}
		}.start();
	}

	private void closeDevice(){
		if (sdk == null) {
			return;
		}
		try {
			sdk.close();
			sdk = null;
			if (version == 22){
				mPower.SetCardPower(0);
			}else {
				try {
					HsUtlis.IDCardPonoff1();
				} catch (IOException e) {
					showErrorDialog("断电失败", "");
					return;
				}
			}
		} catch (Exception e) {
			return;
		}
	}


	public interface FragmentListener {

		void onGetID(String id_info);
	}

	/**
	 * 打开设备
	 */

	/*
	private void openFpDevice() {
		new Thread() {
			@Override
			public void run() {
//				showProgressDialog(getString(R.string.loading), getString(R.string.preparing_device));
				int error;
				//上电,由于是线程所以有点延迟所以会提示上电失败
				if (version == 22){
					try{
						mPower.SetFingerPower(1);
					}catch (Exception e){

					}
				}else {
					if ((error = HsUtlis.UsbPonwer1()) != 1) {
						showErrorDialog(getString(R.string.fingerprint_device_power_on_failed), "");
					}
				}
				SystemClock.sleep(1500);
				//打开设备
				if ((error = mScanner.open()) != FingerprintScanner.RESULT_OK) {
//					mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_SN, getString(R.string.fps_sn, "null")));
//					mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_FW_VERSION, "指纹传感器内部固件版本号:"));
					showErrorDialog(getString(R.string.fingerprint_device_open_failed), "");
				} else {
					Result res = mScanner.getSN();
//					mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_SN, getString(R.string.fps_sn, (String) res.data)));
					res = mScanner.getFirmwareVersion();
					String test = getResources().getString(R.string.fps_fw)+(String) res.data;
//					mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_FW_VERSION, test));//getString(R.string.fps_fw, (String) res.data))
					showInfoToast(getString(R.string.fingerprint_device_open_success));
//					enableControl(true);
				}
				//初始化算法
				if ((error = Bione.initialize(getActivity(), FP_DB_PATH)) != Bione.RESULT_OK) {
					showErrorDialog(getString(R.string.algorithm_initialization_failed), "");
				}
				Log.i(TAG, "Fingerprint algorithm version: " + Bione.getVersion());
//				dismissProgressDialog();
			}
		}.start();
	}
	*/

	/**
	 * 关闭设备
	 */
	/*
	private void closeFpDevice() {
		new Thread() {
			@Override
			public void run() {
//				showProgressDialog(getString(R.string.loading), getString(R.string.closing_device));
//				enableControl(false);
				int error;
				if ((error = mScanner.close()) != FingerprintScanner.RESULT_OK) {
//					showErrorDialog(getString(R.string.fingerprint_device_close_failed), "");
				} else {
//					showInfoToast(getString(R.string.fingerprint_device_close_success));
				}
				if (version == 22){
					try{
						mPower.SetFingerPower(0);
					}catch (Exception e){
					}
				}else {
					if ((error = HsUtlis.UsbPonoff1()) != 1) {
//						showErrorDialog(getString(R.string.fingerprint_device_power_off_failed), "");
					}
				}

				if ((error = Bione.exit()) != Bione.RESULT_OK) {
//					showErrorDialog(getString(R.string.algorithm_cleanup_failed), "");
				}
//				dismissProgressDialog();
			}
		}.start();
	}
	*/
}
