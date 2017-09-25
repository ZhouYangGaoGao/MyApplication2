package com.idcard.modernskyticketing1;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import FaceDetectSync.ExtractFaceRect;
import FaceDetectSync.THIDFaceServiceAPI;
import FaceDetectSync.THIDServiceAPI;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class FaceFragment extends Fragment {

	private static final String TAG = "FaceFragment";

	private ImageView iv_face1, iv_face2;
	private Button bt_facebd,bt_setting;
	private Uri fileUri;
	private String imagePath;
	private SharedPreferences sp;


	ExtractFaceRect faceTrack = null;// sync service 同步人脸服务

	// --------------注册服务接收器----------------------------------------------------------------
	private MessageReceiverFace receiverFace;// 人脸

	public void RegistMessage() {
		// 动态注册receiver，人脸服务
		// registerReceiver faceService
		IntentFilter filter = new IntentFilter(THIDServiceAPI.ACTION_RECV_MSG_FACE);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		receiverFace = new MessageReceiverFace();
		getActivity().registerReceiver(receiverFace, filter);
		Log.e(TAG, "注册服务");
	}

	// 广播接收 人脸、指纹 服务返回的消息
	/**
	 * revice the broadcast from face and finger service
	 *
	 * @author liulv
	 *
	 */
	public class MessageReceiverFace extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			String message = intent.getStringExtra(THIDServiceAPI.MESSAGE_OUT);
//				textView1.setText(message);
			Log.e(TAG, message);



			String taskID = intent.getStringExtra(THIDServiceAPI.MESSAGE_ID);// 任务ID
			Log.e(TAG, "接收服务taskID---" + taskID);

			try {
				JSONObject jsonmsg = new JSONObject(message);
				message = jsonmsg.toString(4); // 转换成易阅读的格式
				EditText resultET = new EditText(getActivity());
				// resultET.setScrollBarStyle(0);
				resultET.setText(message);

				if (0 == taskID.compareTo("faceidtest1v1")) // 人脸1:1
				{
					int nScore = jsonmsg.getInt("Score");
					String mScore = String.format("detect score：%2.1f%%", (nScore * 0.1));
					String path1 = jsonmsg.getString("Img2Uri");
					String path2 = jsonmsg.getString("Img1Uri");
					THIDFaceServiceAPI.face1V1Dialog(getActivity(), path1, path2, mScore);
				} else {
					// Log.e(TAG, "Finger 1vN Back" );
					// 对话框
					new AlertDialog.Builder(getActivity()).setTitle("Result")
							.setIcon(android.R.drawable.ic_dialog_info).setView(resultET)
							.setNegativeButton("sure", null).show();

				}
			} catch (JSONException e) {
				Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
				// TODO Auto-generated catch block
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.fragment_face, container, false);
		imagePath = Environment.getExternalStorageDirectory()+File.separator+"wltlib/zp.bmp";
		bt_facebd = (Button) view.findViewById(R.id.bt_bd);
		bt_setting = (Button) view.findViewById(R.id.bt_setting);
		iv_face1 = (ImageView) view.findViewById(R.id.imageView1);
		iv_face2 = (ImageView) view.findViewById(R.id.imageView2);

		iv_face1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				try {
					FileInputStream fis = new FileInputStream(imagePath);
					Bitmap bmp = BitmapFactory.decodeStream(fis);
					fis.close();
					iv_face1.setImageBitmap(bmp);
				} catch (Exception e) {
					Toast.makeText(getActivity(), "未读取身份证！", Toast.LENGTH_SHORT).show();
				}
//                Intent tmpIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                startActivityForResult(Intent.createChooser(tmpIntent, "choice pic"), 66);
			}
		});
		iv_face2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				fileUri = getOutputMediaFileUri(1);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
				intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(intent, 88);
			}
		});

		bt_facebd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int version = Integer.parseInt(Build.VERSION.SDK);
				if (version == 22){
					Toast.makeText(getActivity(),"有待开发中", Toast.LENGTH_LONG).show();
					return;
				}
				if (fileUri == null) {
					return;
				}
				THIDFaceServiceAPI.TestFaceID1v1Service(getActivity(), fileUri, imagePath);
			}
		});
		bt_setting.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				View view = View.inflate(getActivity(), R.layout.face_setting, null);
				final EditText et = (EditText) view.findViewById(R.id.et_setting);
				String mScore = sp.getString("Score", "50");
				et.setText(mScore);
				builder.setView(view);
				builder.setCancelable(false);
				builder.setNegativeButton("取消", null);
				builder.setPositiveButton("确定", new android.content.DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String score = et.getText().toString();
						sp.edit().putString("Score", score).commit();
						Toast.makeText(getActivity(), "设置成功", Toast.LENGTH_LONG).show();
					}
				});
				builder.show();
			}
		});
		return view;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 66){
			Uri uri = data.getData();
			try {
				Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
				iv_face1.setImageBitmap(bitmap);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else if (requestCode == 88){
			Log.i(TAG, "onActivityResult: 拍照");
			if (data != null)
			{
				// 没有指定特定存储路径的时候
				Log.d(TAG,
						"data is NOT null, file on default position.");

				// 指定了存储路径的时候（intent.putExtra(MediaStore.EXTRA_OUTPUT,fileUri);）
				// Image captured and saved to fileUri specified in the
				// Intent
//	                    Toast.makeText(this, "Image saved to:\n" + data.getData(),
//	                            Toast.LENGTH_LONG).show();

				if (data.hasExtra("data"))
				{

					Bitmap thumbnail = data.getParcelableExtra("data");
//	                        iv1.setImageBitmap(thumbnail);
				}
			}
			else
			{
				Log.d(TAG, "data IS null, file saved on target position."+fileUri.toString());

				// 人脸1v1第2张，提交比对
				try {
					Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), fileUri);
					iv_face2.setImageBitmap(bitmap);
				} catch (IOException e) {
					e.printStackTrace();
				}
//	                    String imagePath = Environment.getExternalStorageDirectory()+File.separator+"wltlib/zp.bmp";
//	                    THIDFaceServiceAPI.TestFaceID1v1Service(IDCardActivity.this, fileUri, imagePath);
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		RegistMessage();
		sp = getActivity().getSharedPreferences("config", getActivity().MODE_PRIVATE);
	}


	private static Uri getOutputMediaFileUri(int type)
	{
		return Uri.fromFile(getOutputMediaFile(type));
	}

	private static File getOutputMediaFile(int type)
	{
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = null;
		try
		{
			// This location works best if you want the created images to be
			// shared
			// between applications and persist after your app has been
			// uninstalled.
			mediaStorageDir = new File(
					Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
					"MyCameraApp");

			Log.d(TAG, "Successfully created mediaStorageDir: "
					+ mediaStorageDir);

		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.d(TAG, "Error in Creating mediaStorageDir: "
					+ mediaStorageDir);
		}

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists())
		{
			if (!mediaStorageDir.mkdirs())
			{
				// 在SD卡上创建文件夹需要权限：
				// <uses-permission
				// android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
				Log.d(TAG,
						"failed to create directory, check if you have the WRITE_EXTERNAL_STORAGE permission");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File mediaFile;
		if (type == 1)
		{
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".jpg");
		}
		else if (type == 2)
		{
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "VID_" + timeStamp + ".mp4");
		}
		else
		{
			return null;
		}

		return mediaFile;
	}
}
