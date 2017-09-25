package com.idcard.modernskyticketing1;



import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.Message;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.aratek.fp.Bione;
import cn.com.aratek.fp.FingerprintImage;
import cn.com.aratek.fp.FingerprintScanner;
import cn.com.aratek.util.Result;

public class FpFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = "FingerprintActivity";
    private static final String FP_DB_PATH = "/sdcard/fp.db";
    private static final int MSG_SHOW_ERROR = 0;
    private static final int MSG_SHOW_INFO = 1;
    private static final int MSG_UPDATE_IMAGE = 2;
    private static final int MSG_UPDATE_TEXT = 3;
    private static final int MSG_UPDATE_BUTTON = 4;
    private static final int MSG_UPDATE_SN = 5;
    private static final int MSG_UPDATE_FW_VERSION = 6;
    private static final int MSG_SHOW_PROGRESS_DIALOG = 7;
    private static final int MSG_DISMISS_PROGRESS_DIALOG = 8;

    int version = Integer.parseInt(Build.VERSION.SDK);
    private IPowerManager mPower;

    private TextView mSN;
    private TextView mFwVersion;
    private Button mBtnEnroll;
    private Button mBtnVerify;
    private Button mBtnIdentify;
    private Button mBtnClear;
    private Button mBtnShow;
    private EditText mCaptureTime;
    private EditText mExtractTime;
    private EditText mGeneralizeTime;
    private EditText mVerifyTime;
    private ImageView mFingerprintImage;
    private ProgressDialog mProgressDialog;
    private FingerprintScanner mScanner;
    private FingerprintTask mTask;
    private int mId;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SHOW_ERROR: {
                    getActivity().showDialog(0, (Bundle) msg.obj);
                    break;
                }
                case MSG_SHOW_INFO: {
                    Toast.makeText(getActivity(), (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                }
                case MSG_UPDATE_IMAGE: {
                    mFingerprintImage.setImageBitmap((Bitmap) msg.obj);
                    break;
                }
                case MSG_UPDATE_TEXT: {
                    String[] texts = (String[]) msg.obj;
                    mCaptureTime.setText(texts[0]);
                    mExtractTime.setText(texts[1]);
                    mGeneralizeTime.setText(texts[2]);
                    mVerifyTime.setText(texts[3]);
                    break;
                }
                case MSG_UPDATE_BUTTON: {
                    Boolean enable = (Boolean) msg.obj;
                    mBtnEnroll.setEnabled(enable);
                    mBtnVerify.setEnabled(enable);
                    mBtnIdentify.setEnabled(enable);
                    mBtnClear.setEnabled(enable);
                    mBtnShow.setEnabled(enable);
                    break;
                }
                case MSG_UPDATE_SN: {
                    mSN.setText((String) msg.obj);
                    break;
                }
                case MSG_UPDATE_FW_VERSION: {
                    Log.e(TAG, "handleMessage: 哈哈哈"+(String) msg.obj);
                    mFwVersion.setText((String) msg.obj);
                    break;
                }
                case MSG_SHOW_PROGRESS_DIALOG: {
                    String[] info = (String[]) msg.obj;
                    mProgressDialog.setTitle(info[0]);
                    mProgressDialog.setMessage(info[1]);
                    mProgressDialog.show();
                    break;
                }
                case MSG_DISMISS_PROGRESS_DIALOG: {
                    mProgressDialog.dismiss();
                    break;
                }
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View view = inflater.inflate(R.layout.fragment_fp, container, false);
        mSN = (TextView) view.findViewById(R.id.tv_fps_sn);
        mFwVersion = (TextView) view.findViewById(R.id.tv_fps_fw);
        mCaptureTime = (EditText) view.findViewById(R.id.captureTime);
        mExtractTime = (EditText) view.findViewById(R.id.extractTime);
        mGeneralizeTime = (EditText) view.findViewById(R.id.generalizeTime);
        mVerifyTime = (EditText) view.findViewById(R.id.verifyTime);
        mFingerprintImage = (ImageView) view.findViewById(R.id.fingerimage);

        mBtnEnroll = (Button) view.findViewById(R.id.bt_enroll);
        mBtnVerify = (Button) view.findViewById(R.id.bt_verify);
        mBtnIdentify = (Button) view.findViewById(R.id.bt_identify);
        mBtnClear = (Button) view.findViewById(R.id.bt_clear);
        mBtnShow = (Button) view.findViewById(R.id.bt_show);
        enableControl(false);
        mBtnEnroll.setOnClickListener(this);
        mBtnVerify.setOnClickListener(this);
        mBtnIdentify.setOnClickListener(this);
        mBtnClear.setOnClickListener(this);
        mBtnShow.setOnClickListener(this);

        updateSingerTestText(-1, -1, -1, -1);
        return view;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mScanner = FingerprintScanner.getInstance();
        //进度条对话框
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setIcon(android.R.drawable.ic_dialog_info);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setCancelable(false);
        mPower = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        // TODO Auto-generated method stub
        super.onHiddenChanged(hidden);
        if (hidden) {
            if (mTask != null && mTask.getStatus() != AsyncTask.Status.FINISHED) {
                mTask.cancel(false);
                mTask.waitForDone();
            }
            closeDevice();
        }else {
            SystemClock.sleep(1000);
            openDevice();
        }
    }

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        SystemClock.sleep(1000);
        openDevice();
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (mTask != null && mTask.getStatus() != AsyncTask.Status.FINISHED) {
            mTask.cancel(false);
            mTask.waitForDone();
        }
        closeDevice();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_enroll:
                enroll();
                break;
            case R.id.bt_verify:
                verify();
                break;
            case R.id.bt_identify:
                identify();
                break;
            case R.id.bt_clear:
                clearFingerprintDatabase();
                break;
            case R.id.bt_show:
                showFingerprintImage();
                break;
        }
    }

    private void updateFingerprintImage(FingerprintImage fi) {
        byte[] fpBmp = null;
        Bitmap bitmap;
        if (fi == null || (fpBmp = fi.convert2Bmp()) == null || (bitmap = BitmapFactory.decodeByteArray(fpBmp, 0, fpBmp.length)) == null) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.nofinger);
        }
        mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_IMAGE, bitmap));
    }

    private void updateSingerTestText(long captureTime, long extractTime, long generalizeTime, long verifyTime) {
        String[] texts = new String[4];
        if (captureTime < 0) {
            texts[0] = getString(R.string.not_done);
        } else if (captureTime < 1) {
            texts[0] = "< 1ms";
        } else {
            texts[0] = captureTime + "ms";
        }

        if (extractTime < 0) {
            texts[1] = getString(R.string.not_done);
        } else if (extractTime < 1) {
            texts[1] = "< 1ms";
        } else {
            texts[1] = extractTime + "ms";
        }

        if (generalizeTime < 0) {
            texts[2] = getString(R.string.not_done);
        } else if (generalizeTime < 1) {
            texts[2] = "< 1ms";
        } else {
            texts[2] = generalizeTime + "ms";
        }

        if (verifyTime < 0) {
            texts[3] = getString(R.string.not_done);
        } else if (verifyTime < 1) {
            texts[3] = "< 1ms";
        } else {
            texts[3] = verifyTime + "ms";
        }
        mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_TEXT, texts));
    }

    /**
     * 根据enable来设置按钮是否可以点击
     * @param enable
     */
    private void enableControl(boolean enable) {
        mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_BUTTON, enable));
    }

    /**
     * 打开设备
     */
    private void openDevice() {
        new Thread() {
            @Override
            public void run() {
                showProgressDialog(getString(R.string.loading), getString(R.string.preparing_device));
                int error;
                //上电,由于是线程所以有点延迟所以会提示上电失败
                if (version == 22){
                    try{
                        mPower.SetFingerPower(1);
                    }catch (Exception e){

                    }
                }else {
                    if ((error = HsUtlis.UsbPonwer1()) != 1) {
                        showErrorDialog(getString(R.string.fingerprint_device_power_on_failed), getFingerprintErrorString(error));
                    }
                }
                SystemClock.sleep(1500);
                //打开设备
                if ((error = mScanner.open()) != FingerprintScanner.RESULT_OK) {
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_SN, getString(R.string.fps_sn, "null")));
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_FW_VERSION, "指纹传感器内部固件版本号:"));
                    showErrorDialog(getString(R.string.fingerprint_device_open_failed), getFingerprintErrorString(error));
                } else {
                    Result res = mScanner.getSN();
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_SN, getString(R.string.fps_sn, (String) res.data)));
                    res = mScanner.getFirmwareVersion();
                    String test = getResources().getString(R.string.fps_fw)+(String) res.data;
                    mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_FW_VERSION, test));//getString(R.string.fps_fw, (String) res.data))
                    showInfoToast(getString(R.string.fingerprint_device_open_success));
                    enableControl(true);
                }
                //初始化算法
                if ((error = Bione.initialize(getActivity(), FP_DB_PATH)) != Bione.RESULT_OK) {
                    showErrorDialog(getString(R.string.algorithm_initialization_failed), getFingerprintErrorString(error));
                }
                Log.i(TAG, "Fingerprint algorithm version: " + Bione.getVersion());
                dismissProgressDialog();
            }
        }.start();
    }

    /**
     * 关闭设备
     */
    private void closeDevice() {
        new Thread() {
            @Override
            public void run() {
                showProgressDialog(getString(R.string.loading), getString(R.string.closing_device));
                enableControl(false);
                int error;
                if ((error = mScanner.close()) != FingerprintScanner.RESULT_OK) {
                    showErrorDialog(getString(R.string.fingerprint_device_close_failed), getFingerprintErrorString(error));
                } else {
                    showInfoToast(getString(R.string.fingerprint_device_close_success));
                }
                if (version == 22){
                    try{
                        mPower.SetFingerPower(0);
                    }catch (Exception e){
                    }
                }else {
                    if ((error = HsUtlis.UsbPonoff1()) != 1) {
                        showErrorDialog(getString(R.string.fingerprint_device_power_off_failed), getFingerprintErrorString(error));
                    }
                }
                if ((error = Bione.exit()) != Bione.RESULT_OK) {
                    showErrorDialog(getString(R.string.algorithm_cleanup_failed), getFingerprintErrorString(error));
                }
                dismissProgressDialog();
            }
        }.start();
    }

    private void enroll() {
        mTask = new FingerprintTask();
        mTask.execute("enroll");
    }

    private void verify() {
        mTask = new FingerprintTask();
        mTask.execute("verify");
    }

    private void identify() {
        mTask = new FingerprintTask();
        mTask.execute("identify");
    }

    private void clearFingerprintDatabase() {
        int error = Bione.clear();
        if (error == Bione.RESULT_OK) {
            showInfoToast(getString(R.string.clear_fingerprint_database_success));
        } else {
            showErrorDialog(getString(R.string.clear_fingerprint_database_failed), getFingerprintErrorString(error));
        }
    }

    private void showFingerprintImage() {
        mTask = new FingerprintTask();
        mTask.execute("show");
    }

    protected Dialog onCreateDialog(int id, Bundle args) {
        String operation = args.getString("operation");
        String errString = args.getString("errString");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle(R.string.info_error);
        if (errString != null && !errString.equals("")) {
            builder.setMessage(operation + "\n" + errString);
        } else {
            builder.setMessage(operation);
        }
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return builder.create();
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

    /**
     * 根据传入info弹出toast
     * @param info
     */
    private void showInfoToast(String info) {
        mHandler.sendMessage(mHandler.obtainMessage(MSG_SHOW_INFO, info));
    }

    private String getFingerprintErrorString(int error) {
        int strid;
        switch (error) {
            case FingerprintScanner.RESULT_OK:
                strid = R.string.operation_successful;
                break;
            case FingerprintScanner.RESULT_FAIL:
                strid = R.string.error_operation_failed;
                break;
            case FingerprintScanner.WRONG_CONNECTION:
                strid = R.string.error_wrong_connection;
                break;
            case FingerprintScanner.DEVICE_BUSY:
                strid = R.string.error_device_busy;
                break;
            case FingerprintScanner.DEVICE_NOT_OPEN:
                strid = R.string.error_device_not_open;
                break;
            case FingerprintScanner.TIMEOUT:
                strid = R.string.error_timeout;
                break;
            case FingerprintScanner.NO_PERMISSION:
                strid = R.string.error_no_permission;
                break;
            case FingerprintScanner.WRONG_PARAMETER:
                strid = R.string.error_wrong_parameter;
                break;
            case FingerprintScanner.DECODE_ERROR:
                strid = R.string.error_decode;
                break;
            case FingerprintScanner.INIT_FAIL:
                strid = R.string.error_initialization_failed;
                break;
            case FingerprintScanner.UNKNOWN_ERROR:
                strid = R.string.error_unknown;
                break;
            case FingerprintScanner.NOT_SUPPORT:
                strid = R.string.error_not_support;
                break;
            case FingerprintScanner.NOT_ENOUGH_MEMORY:
                strid = R.string.error_not_enough_memory;
                break;
            case FingerprintScanner.DEVICE_NOT_FOUND:
                strid = R.string.error_device_not_found;
                break;
            case FingerprintScanner.DEVICE_REOPEN:
                strid = R.string.error_device_reopen;
                break;
            case FingerprintScanner.NO_FINGER:
                strid = R.string.error_no_finger;
                break;
            case Bione.INITIALIZE_ERROR:
                strid = R.string.error_algorithm_initialization_failed;
                break;
            case Bione.INVALID_FEATURE_DATA:
                strid = R.string.error_invalid_feature_data;
                break;
            case Bione.BAD_IMAGE:
                strid = R.string.error_bad_image;
                break;
            case Bione.NOT_MATCH:
                strid = R.string.error_not_match;
                break;
            case Bione.LOW_POINT:
                strid = R.string.error_low_point;
                break;
            case Bione.NO_RESULT:
                strid = R.string.error_no_result;
                break;
            case Bione.OUT_OF_BOUND:
                strid = R.string.error_out_of_bound;
                break;
            case Bione.DATABASE_FULL:
                strid = R.string.error_database_full;
                break;
            case Bione.LIBRARY_MISSING:
                strid = R.string.error_library_missing;
                break;
            case Bione.UNINITIALIZE:
                strid = R.string.error_algorithm_uninitialize;
                break;
            case Bione.REINITIALIZE:
                strid = R.string.error_algorithm_reinitialize;
                break;
            case Bione.REPEATED_ENROLL:
                strid = R.string.error_repeated_enroll;
                break;
            case Bione.NOT_ENROLLED:
                strid = R.string.error_not_enrolled;
                break;
            default:
                strid = R.string.error_other;
                break;
        }
        return getString(strid);
    }

    /**
     * 启动任务执行的输入参数”、“后台任务执行的进度”、“后台计算结果的类型”
     */
    private class FingerprintTask extends AsyncTask<String, Integer, Void> {
        private boolean mIsDone = false;

        @Override
        protected void onPreExecute() {
            enableControl(false);
        }

        @Override
        protected Void doInBackground(String... params) {
            long startTime, captureTime = -1, extractTime = -1, generalizeTime = -1, verifyTime = -1;
            FingerprintImage fi = null;
            byte[] fpFeat = null, fpTemp = null;
            Result res;

            do {
                if (params[0].equals("show") || params[0].equals("enroll") || params[0].equals("verify") || params[0].equals("identify")) {
                    showProgressDialog(getString(R.string.loading), getString(R.string.press_finger));
                    mScanner.prepare();
                    startTime = System.currentTimeMillis();
                    do {
//                        startTime = System.currentTimeMillis();
                        res = mScanner.capture();
                        captureTime = System.currentTimeMillis() - startTime;
                    } while (res.error == FingerprintScanner.NO_FINGER && !isCancelled() && (System.currentTimeMillis() - startTime)<8000);//判断没有检测到指纹 且没有取消任务
                    mScanner.finish();//采集指纹结束
                    if (isCancelled()) {
                        break;
                    }
                    if (res.error != FingerprintScanner.RESULT_OK) {
                        showErrorDialog(getString(R.string.capture_image_failed), getFingerprintErrorString(res.error));
                        break;
                    }
                    fi = (FingerprintImage) res.data;
                    Log.i(TAG, "Fingerprint image quality is " + Bione.getFingerprintQuality(fi));
                }

                if (params[0].equals("enroll")) {
                    showProgressDialog(getString(R.string.loading), getString(R.string.enrolling));
                } else if (params[0].equals("verify")) {
                    showProgressDialog(getString(R.string.loading), getString(R.string.verifying));
                } else if (params[0].equals("identify")) {
                    showProgressDialog(getString(R.string.loading), getString(R.string.identifying));
                }

                if (params[0].equals("enroll") || params[0].equals("verify") || params[0].equals("identify")) {
                    startTime = System.currentTimeMillis();
                    res = Bione.extractFeature(fi);
                    extractTime = System.currentTimeMillis() - startTime;
                    if (res.error != Bione.RESULT_OK) {
                        showErrorDialog(getString(R.string.enroll_failed_because_of_extract_feature), getFingerprintErrorString(res.error));
                        break;
                    }
                    fpFeat = (byte[]) res.data;
                }

                if (params[0].equals("enroll")) {
                    startTime = System.currentTimeMillis();
                    res = Bione.makeTemplate(fpFeat, fpFeat, fpFeat);
                    generalizeTime = System.currentTimeMillis() - startTime;
                    if (res.error != Bione.RESULT_OK) {
                        showErrorDialog(getString(R.string.enroll_failed_because_of_make_template), getFingerprintErrorString(res.error));
                        break;
                    }
                    fpTemp = (byte[]) res.data;

                    int id = Bione.getFreeID();
                    if (id < 0) {
                        showErrorDialog(getString(R.string.enroll_failed_because_of_get_id), getFingerprintErrorString(id));
                        break;
                    }
                    int ret = Bione.enroll(id, fpTemp);
                    if (ret != Bione.RESULT_OK) {
                        showErrorDialog(getString(R.string.enroll_failed_because_of_error), getFingerprintErrorString(ret));
                        break;
                    }
                    mId = id;
                    showInfoToast(getString(R.string.enroll_success) + id);
                } else if (params[0].equals("verify")) {
                    startTime = System.currentTimeMillis();
                    int id = Bione.identify(fpFeat);
                    verifyTime = System.currentTimeMillis() - startTime;
                    if (id < 0) {
                        showErrorDialog(getString(R.string.verify_failed_because_of_error), getFingerprintErrorString(id));
                        break;
                    }
                    showInfoToast(getString(R.string.identify_match) + id);
//                    res = Bione.verify(mId, fpFeat);
//                    verifyTime = System.currentTimeMillis() - startTime;
//                    if (res.error != Bione.RESULT_OK) {
//                        showErrorDialog(getString(R.string.verify_failed_because_of_error), getFingerprintErrorString(res.error));
//                        break;
//                    }
//                    if ((Boolean) res.data) {
//                        showInfoToast(getString(R.string.fingerprint_match));
//                    } else {
//                        showInfoToast(getString(R.string.fingerprint_not_match));
//                    }
                } else if (params[0].equals("identify")) {
                    startTime = System.currentTimeMillis();
                    int id = Bione.identify(fpFeat);
                    verifyTime = System.currentTimeMillis() - startTime;
                    if (id < 0) {
                        showErrorDialog(getString(R.string.identify_failed_because_of_error), getFingerprintErrorString(id));
                        break;
                    }
                    showInfoToast(getString(R.string.identify_match) + id);
                }

                if (params[0].equals("show") || params[0].equals("enroll") || params[0].equals("verify") || params[0].equals("identify")) {
                    updateFingerprintImage(fi);
                }
            } while (false);

            updateSingerTestText(captureTime, extractTime, generalizeTime, verifyTime);
            enableControl(true);
            dismissProgressDialog();
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

}
