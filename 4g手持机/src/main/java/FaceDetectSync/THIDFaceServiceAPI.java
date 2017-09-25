package FaceDetectSync;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;

public class THIDFaceServiceAPI {

	//private final static String TAG="THIDFaceServiceAPI";

	public static class THIDFaceIDArg1vN
	{
		String sDBPath = "";				// 本地库路径，相对路径(Local library path, relative path)
		int nThrd = 600;					// 阈值 [0,1000]，默认为600(Threshold,default is 600)
		int nMaxCan = 5;				// 最大候选人数[1~20]，默认为5 (The max face can detect)

		THIDFaceIDArg1vN(){
		}

		THIDFaceIDArg1vN(String dbPath, int thrd, int maxcan){
			sDBPath = dbPath;
			nThrd = thrd;
			nMaxCan = maxcan;
		}
	}

	/**填写人脸比对参数 1vN
	 * @param sDBPath	本地库路径，相对路径
	 *                  (Local library path, relative path)
	 * @param nThrd		阈值 [0,1000]，默认为600
	 *                  (Threshold,default is 600)
	 * @param nMaxCan	最大候选人数[1~20]，默认为5
	 *                  (The max face can detect)
	 * @return	json格式字符串
	 *          return json result
	 */
	public static String SetTHIDFaceIDArg(String sDBPath, int nThrd, int nMaxCan)
	{
		String retjson = "";

		Gson gson = new Gson();
		THIDFaceIDArg1vN faceIDarg = new THIDFaceIDArg1vN(sDBPath, nThrd, nMaxCan);
		retjson = gson.toJson( faceIDarg );
		return retjson;
	}

	/**填写人脸比对参数 1v1
	 * @param ImgPath	图片路径（第二张）
	 *                  the second choice image path
	 * @param nThrd		比对阈值（0〜1000）,暂未使用
	 *                  the threshold (not used now)
	 * @return	json格式字符串
	 *          return json result
	 */
	public static String SetTHIDFaceIDArg(String ImgPath, int nThrd) {
		String retjson = "";
		try {
			JSONObject jsonPlateArg = new JSONObject();

			jsonPlateArg.put("ImgPath", ImgPath);
			jsonPlateArg.put("nThrd", nThrd);

			retjson = jsonPlateArg.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return retjson;
	}

	/**填写人脸入库参数
	 * @param sDBPath       准备存储的本地库路径，比如 localdb，如果不存在，则不会保存，只会内存中临时增加。
	 *                      For storage of local library path ,such as local library ,If not, will not save, will only be a temporary increase in the memory
	 * @param addPersonInfo 人员信息，有两个项：名称、描述
	 *                      the person name and description add into the library
	 * @return
	 */
	public static String SetTHIDFaceIDArg(String sDBPath, String[] addPersonInfo) {
		String retjson = "";

		try {
			JSONObject jsonArg = new JSONObject();
			jsonArg.put("nCmd", 4); //比对类型，0:DBScan, 1:WatchList, 3:Verfiy 4:AddtoLDB
			jsonArg.put("addPersonName", addPersonInfo[0]);
			jsonArg.put("addPersonDes", addPersonInfo[1]);
			if( sDBPath!=null )
				jsonArg.put("sDBPath", sDBPath);
			retjson = jsonArg.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return retjson;
	}

	/**填写人脸入库参数
	 * Add storage of information
	 * @param addPersonInfo 人员信息，有两个项：名称、描述
	 * the person name and description add into the library
	 * @return
	 */
	public static String SetTHIDFaceIDArg(String[] addPersonInfo) {
		return SetTHIDFaceIDArg( null, addPersonInfo);
	}

	/**调用人脸识别服务
	 * Call the face recognition service
	 */
	public static int CallFaceIDService(Context mContext, String taskID, String inPath, String jsonArg)
	{
		Log.i("aaaaaa", "CallFaceIDService: ");
		Intent msgIntent = THIDServiceAPI.GenTHIDServiceIntent(THIDServiceAPI.ALGTYPE_FACEID, taskID, inPath, jsonArg);
		msgIntent.putExtra(THIDServiceAPI.MESSAGE_PKGNAME, mContext.getPackageName());
		mContext.startService(msgIntent);
		Log.i("lllll", "CallFaceIDService: ");
		return 0;
	}


	//测试人脸1vN服务调用
	/***
	 * Test the face recognition service for 1vN
	 * @param mContext
	 * @param imagePath
	 * @return
	 */
	public static int TestFaceID1vNService (Context mContext, String imagePath){

		String jsonArg = SetTHIDFaceIDArg("localdb", 600, 10);
		CallFaceIDService(mContext, "faceidtest1vN", imagePath, jsonArg);

		return 0;
	}

	//测试人脸1v1服务调用
	/***
	 * Test the face recognition service for 1v1
	 * @param mContext
	 * @param lastChosenUri
	 * @param imagePath
	 * @return
	 */
	public static int TestFaceID1v1Service (Context mContext, Uri lastChosenUri, String imagePath){

		Log.i("llll", "TestFaceID1v1Service: ");
		String jsonArg = SetTHIDFaceIDArg(imagePath, 0);
		CallFaceIDService(mContext, "faceidtest1v1", lastChosenUri.toString(), jsonArg );
		return 0;
	}

	//测试人脸入库服务调用
	/**
	 * Test the face storage into the library service
	 * @param mContext
	 * @param addImagePath  the image path to add
	 * @param addPersonInfo  the person info to add
	 * @return
	 */
	public static int TestFaceIDAddService (Context mContext, String addImagePath, String[] addPersonInfo){

		String jsonArg = SetTHIDFaceIDArg("localdb", addPersonInfo);
		CallFaceIDService( mContext, "faceidtestAdd", addImagePath, jsonArg);

		return 0;
	}


	/**
	 * @param imagePath	待入库图片路径（文件绝对对路径 或 uri.toString）
	 */
	private static String addImagePath;
	public static void addFeatDialog(final Context mContext, String imagePath) {

//		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
//		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.adddialog, null);
//		dialog.setView(layout);
//		final EditText etPersonName = (EditText)layout.findViewById(R.id.AD_personName);
//		final EditText etPersonDes = (EditText)layout.findViewById(R.id.AD_personDes);
//		final ImageView imageView = (ImageView)layout.findViewById(R.id.imageView1);
//		addImagePath = imagePath;
//		imageView.setImageURI( Uri.parse(imagePath) );
//		dialog.setPositiveButton("保存", new DialogInterface.OnClickListener() {
//			public void onClick(DialogInterface dialog, int which) {
//
//				String[] addPersonInfo = new String[2];
//				addPersonInfo[0] = etPersonName.getText().toString();
//				addPersonInfo[1] = etPersonDes.getText().toString();
//				//Log.i(TAG,"PersonNames：("+addPersonInfo[0]+ ")  Details：("+addPersonInfo[1]+")");
//				TestFaceIDAddService(mContext, addImagePath, addPersonInfo);
//			}
//		});
//
//		dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//			public void onClick(DialogInterface dialog, int which) {
//
//			}
//		});
//		dialog.show();
	}

	/**
	 *	人脸1V1图片显示（文件绝对对路径 或 uri.toString）
	 */
	public static void face1V1Dialog(final Context mContext, String imagePath1, String imagePath2, String mScore) {

		SharedPreferences sp = mContext.getSharedPreferences("config", mContext.MODE_PRIVATE);
		String score = sp.getString("Score", "50");
		double s1 = Double.parseDouble(score);
		String s = mScore.substring(13, 17);
		Log.e("eee", mScore+"-"+s+"===");
		double s2 = Double.parseDouble(s);
		if (s2>s1) {
			Toast.makeText(mContext, "是本人", Toast.LENGTH_LONG).show();
		}else {
			Toast.makeText(mContext, "不是本人", Toast.LENGTH_LONG).show();
		}
//		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
//		LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.face1v1dialog, null);
//		dialog.setView(layout);
//		dialog.setCancelable(false);
//
//		final EditText etScore = (EditText)layout.findViewById(R.id.AD_Score);
//		final ImageView imageView1 = (ImageView)layout.findViewById(R.id.imageView3);
//		final ImageView imageView2 = (ImageView)layout.findViewById(R.id.imageView4);
//
//		//扩大或缩小到相同的比例
//		Bitmap facebmp1 = GetBitMapFromFilePath(mContext, imagePath1, 384, 540);
//		Bitmap facebmp2 = GetBitMapFromFilePath(mContext, imagePath2, 384, 540);
//		imageView1.setImageBitmap(facebmp1);
//		imageView2.setImageBitmap(facebmp2);
//
//		//setImageResource(R.drawable.ok);//setImageURI//setImageBitmap
//		imageView1.setImageURI( Uri.parse(imagePath1) );
//		imageView2.setImageURI( Uri.parse(imagePath2) );
//
//		etScore.setText( mScore );
//
//		dialog.setPositiveButton("Sure", new DialogInterface.OnClickListener() {
//			public void onClick(DialogInterface dialog, int which) {
//				dialog.dismiss();
//			}
//		});
//
//	    /*
//	    dialog.setNegativeButton(R.string.cfg_CancleValue, new DialogInterface.OnClickListener() {
//	        public void onClick(DialogInterface dialog, int which) {
//
//	        }
//	    });*/
//
//
//		dialog.show();
	}


	//指纹图片源显示//
	public static  Bitmap GetBitMapFromFilePath(Context ctxPt, String probe_path, int width, int height)
	{
		Uri probe_uri = Uri.fromFile(new File(probe_path));
		String ldbResultStatus;
		int imgSampleSize;
		Bitmap bitmap = null;
		try
		{
			ContentResolver cr = ctxPt.getContentResolver();
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;//只描边，不读取数据
			BitmapFactory.decodeStream(cr.openInputStream(probe_uri), null, options);

			if( options.outWidth > options.outHeight )// must < 1024*768*2
				imgSampleSize= (options.outWidth+360) / width;  // >1000 ->resample
			else
				imgSampleSize= (options.outHeight+360) / height;

			options.inSampleSize = imgSampleSize;
			options.inJustDecodeBounds = false;//读取数据
			//options.inPreferredConfig = Bitmap.Config.RGB_565;
			bitmap = BitmapFactory.decodeStream(cr.openInputStream(probe_uri), null, options);

		}
		catch (FileNotFoundException e)
		{
			ldbResultStatus = "图片解码错误！" ;
//			Toast.makeText(ctxPt, ldbResultStatus, Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		catch (OutOfMemoryError e)
		{
			ldbResultStatus = "内存不足错误！" ;
			Toast.makeText(ctxPt, ldbResultStatus, Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
		return bitmap;
	}

}