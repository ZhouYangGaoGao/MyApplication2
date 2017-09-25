package FaceDetectSync;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

/**
 * 该类主要提供人脸框提取同步服务接口（即接口是阻塞式的，便于用户进行实时的人脸框提取）
 * 使用该接口需要的资源：该工程目录src/下的FaceDetectSync文件夹拷贝到调用工程的src/目录下（必须放在这个目录下，文件夹名、文件名、
 * 文件位置都不能更改）
 * 
 * The main face frame extraction synchronization service interface
 * 
 * @author yangjitao liulv
 *
 */
public class ExtractFaceRect {
	private final String TAG = "FPModuleClient";
	private FaceDetectInterface faceDetectInterface = null;
	private final int ERROR_REMOTE = -1000;
	private final int ERROR_NOCONNECT = -1001;
	private Context context;

	public ExtractFaceRect(Context context) {
		this.context = context;
		Intent intent = new Intent("com.THID.FaceSDK.FaceDetectSync");
		context.bindService(intent, connect, Context.BIND_AUTO_CREATE);
	}

	public void unBindService() {
		if (connect != null)
		context.unbindService(connect);
		faceDetectInterface = null;
		connect = null;
		Log.e(TAG, "enter unbindService");
	}

	/**
	 * 人脸框提取 Face frame detection
	 * 
	 * @param grayImage
	 *            传入的灰度图像(The need to detect grayImage)
	 * @param width
	 *            图像宽度 (image width)
	 * @param height
	 *            图像长度 (image height)
	 * @param faceRect
	 *            传出的人脸框位置 (The face frame result of detect )
	 * @param faceNum
	 *            检测到的人脸个数 (The number of faces in the detect)
	 * @return 0：检测正常 (detect success return 0)
	 */
	public int faceDetect(byte[] grayImage, int width, int height, THIDFaceRect[] faceRect, int[] faceNum) {
		if (faceDetectInterface == null)
			return ERROR_NOCONNECT;

		try {
			int[] faceRectArr = new int[40];
			faceDetectInterface.faceDetect(grayImage, width, height, faceRectArr, faceNum);
			for (int i = 0; i < faceNum[0]; i++) {
				faceRect[i].left = faceRectArr[i * 4 + 0];
				faceRect[i].top = faceRectArr[i * 4 + 1];
				faceRect[i].right = faceRectArr[i * 4 + 2];
				faceRect[i].bottom = faceRectArr[i * 4 + 3];
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			return ERROR_REMOTE;
		}
		return 0;
	}

	/***
	 * 初始化sdk
	 * 
	 * @return
	 */
	public int initSdk() {
		try {
			if (faceDetectInterface == null)
				return ERROR_NOCONNECT;
			return faceDetectInterface.initSDK();
		} catch (RemoteException e) {
			e.printStackTrace();
			//unBindService();
			return ERROR_REMOTE;
		}
	}
    /**
     * 通过灰度图提取特征
     * @param grayPic   gray image byte
     * @param width   gray image width
     * @param height   gray image height
     * @param faceFeature   return image feat
     * @return 0:sucess
     */
	public int extractFeat(byte[] grayPic, int width, int height, byte[] faceFeature) {
		try {
			if (faceDetectInterface == null)
				return ERROR_NOCONNECT;
			return faceDetectInterface.extractFeat(grayPic, width, height, faceFeature);
		} catch (RemoteException e) {
			e.printStackTrace();
			//unBindService();
			return ERROR_REMOTE;
		}
	}
    /**
     * 通过图片的地址提取特征
     * @param fileName  image path
     * @param faceFeatrue  return image feat
     * @return 0:sucess
     */
	public int exFeatFormFile(String fileName, byte[] faceFeatrue) {
		try {
			if (faceDetectInterface == null)
				return ERROR_NOCONNECT;
			int nRet=faceDetectInterface.exFeatFormFile(fileName, faceFeatrue);
			return nRet;
		} catch (RemoteException e) {
			e.printStackTrace();
			//unBindService();
			return ERROR_REMOTE;
		}
	}

	/**
	 * 同步转灰度图图片
	 * @param byRGBImage  ARGB image byte
	 * @param byGrayImage  return gray image byte
	 * @param nImgWidth   ARGB image width
	 * @param nImgHeight  ARGB image height
	 * @return
	 */
	@SuppressLint("NewApi")
	public int ARGBtoGray(byte[] byRGBImage, byte[] byGrayImage, int nImgWidth, int nImgHeight) {
		if (faceDetectInterface == null)
			return ERROR_NOCONNECT;
		// int nRet=faceDetectInterface.jniARGBtoGray(byRGBImage,
		// byGrayImage, nImgWidth, nImgHeight);
		// return nRet;
		int ret = 0;

		byte[] flag = new byte[0];
		ret=ARGBtoGray_small(flag, byGrayImage, nImgWidth, nImgHeight);
		
		int LEN = 512*1024;
		int perSendDataLength = LEN;
		int featNumRemainder = byRGBImage.length % perSendDataLength;
		int sendTimes = (featNumRemainder == 0)?
				byRGBImage.length/perSendDataLength : byRGBImage.length/perSendDataLength+1;
		
		for(int i = 0; i < sendTimes; i++){
			if(featNumRemainder != 0 && i == sendTimes-1){
				perSendDataLength = featNumRemainder;
			}
			byte[] sendData;
			sendData = Arrays.copyOfRange(byRGBImage, i*LEN, i*LEN+perSendDataLength);
			ret=ARGBtoGray_small(sendData, byGrayImage, nImgWidth, nImgHeight);
		}
		
		ret=ARGBtoGray_small(flag, byGrayImage, nImgWidth, nImgHeight);
		return ret;
	}
	/**
	 * 同步转灰度图图片
	 * @param byRGBImage	RGB565 image byte
	 * @param byGrayImage	return gray image byte
	 * @param nImgWidth		RGB565 image width
	 * @param nImgHeight	RGB565 image height
	 * @return	0：sucess | other :fail
	 */
	@SuppressLint("NewApi")
	public int RGB565toGray(byte[] byRGBImage, byte[] byGrayImage,
			int nImgWidth, int nImgHeight){
		int ret = -1;
		
		byte[] flag = new byte[0];
		ret=RGB565toGray_small(flag, byGrayImage, nImgWidth, nImgHeight);
		
		int LEN = 512*1024;
		int perSendDataLength = LEN;
		int featNumRemainder = byRGBImage.length % perSendDataLength;
		int sendTimes = (featNumRemainder == 0)?
				byRGBImage.length/perSendDataLength : byRGBImage.length/perSendDataLength+1;
		
		for(int i = 0; i < sendTimes; i++){
			if(featNumRemainder != 0 && i == sendTimes-1){
				perSendDataLength = featNumRemainder;
			}
			byte[] sendData;
			sendData = Arrays.copyOfRange(byRGBImage, i*LEN, i*LEN+perSendDataLength);
			ret=RGB565toGray_small(sendData, byGrayImage, nImgWidth, nImgHeight);
		}
		
		ret=RGB565toGray_small(flag, byGrayImage, nImgWidth, nImgHeight);
		return ret;
	}
    /**
     * 调用同步1:n接口     传递的数量有一定限制，一次最多传650个特征
     * sync interface for 1:N  ,ps:  the number N max is 650 by one time
     * @param srcFeat   the feat need to match
     * @param mutiFeat  n feat append to one byte
     * @param matchScore  return  match score
     * @return 0:sucess
     */
	public int faceMutch_1vN(byte[] srcFeat, byte[] mutiFeat, float[] matchScore) {
		try {
			if (faceDetectInterface == null)
				return ERROR_NOCONNECT;
			return faceDetectInterface.faceMatch_1vN(srcFeat, mutiFeat, matchScore);
		} catch (RemoteException e) {
			e.printStackTrace();
			//unBindService();
			return ERROR_REMOTE;
		}
	}
    /**
     * 调用同步1:n接口    传递的数量有一定的限制，比上面的接口传递的数量还要少
     * @param srcFeat  the feat need to match
     * @param mutiFeat  the list of feat
     * @param matchScore  return macth score 
     * @return 0:sucess
     */
	public int faceMutch_1vN(byte[] srcFeat, List mutiFeat, float[] matchScore) {
		try {
			if (faceDetectInterface == null)
				return ERROR_NOCONNECT;
			return faceDetectInterface.faceMatch_1vn(srcFeat, mutiFeat, matchScore);
		} catch (RemoteException e) {
			e.printStackTrace();
			//unBindService();
			return ERROR_REMOTE;
		}
	}
	/**
	 * 将RGB565转换成灰度    传递的byRGBImage长度 少于1024*1024
	 * @param byRGBImage   RGB Image byte max len is 1M
	 * @param byGrayImage
	 * @param nImgWidth
	 * @param nImgHeight
	 * @return 0:sucess
	 */
	private int RGB565toGray_small(byte[] byRGBImage, byte[] byGrayImage, int nImgWidth, int nImgHeight) {
		int ret = -1;
		if (faceDetectInterface == null)
			return ERROR_NOCONNECT;
		try {
			ret = faceDetectInterface.jniRGB565toGray(byRGBImage, byGrayImage, nImgWidth, nImgHeight);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * 将ARGB格式转化成灰度    传递的byRGBImage长度 少于1024*1024
	 * 
	 * @param byRGBImage  ARGB image byte  max len is 1M
	 * @param byGrayImage
	 * @param nImgWidth
	 * @param nImgHeight
	 * @return 0:sucess
	 */
	private int ARGBtoGray_small(byte[] byRGBImage, byte[] byGrayImage, int nImgWidth, int nImgHeight) {
		int ret = -1;
		if (faceDetectInterface == null)
			return ERROR_NOCONNECT;
		try {
			ret = faceDetectInterface.jniARGBtoGray(byRGBImage, byGrayImage, nImgWidth, nImgHeight);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return ret;
	}

	private ServiceConnection connect = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			Log.e(TAG, "enter onServiceDisconnected");
			faceDetectInterface = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.e(TAG, "enter onServiceConnected");
			faceDetectInterface = FaceDetectInterface.Stub.asInterface(service);
			initSdk();
		}
	};


	/**
	 * 人脸框结构体类 The Struct of face frame
	 */
	public static class THIDFaceRect extends Object {
		public int left;
		public int top;
		public int right;
		public int bottom;
		public float confidence;

		public THIDFaceRect() {
			left = 0;
			top = 0;
			right = 0;
			bottom = 0;
			confidence = 0.0f;
		}
	};
}