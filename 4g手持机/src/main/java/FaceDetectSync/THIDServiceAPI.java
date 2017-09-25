package FaceDetectSync;

import android.content.Intent;

//可以作为共用类函数
public class THIDServiceAPI {

	/*
	 * AlgType		算法类型
	 */
	public static final int ALGTYPE_FACEID = 6;		//人脸识别
	public static final int ALGTYPE_PLATEID = 21;	//车牌识别
	public static final int ALGTYPE_FINGERID = 13;	//指纹识别

	/*
	 * ServiceName	服务名称
	 */
	public static final String SERIVCE_FACEID = "com.THID.FaceSDK.FaceIDService";
	public static final String SERIVCE_PLATEID = "com.THID.FaceSDK.FaceIDService";
	public static final String SERIVCE_FINGERID = "com.THID.FingerSDK.FingerIDService";

	/*
	 * Action	结束服务返回信息的 IntentFilter：过滤标识
	 */
	public static final String ACTION_RECV_MSG_FACE = "com.THID.FaceSDK.intent.action.RESULT_MESSAGE";
	public static final String ACTION_RECV_MSG_PLATE = "com.THID.FaceSDK.intent.action.PLATEID_MESSAGE";
	public static final String ACTION_RECV_MSG_FINGER = "com.THID.FingerSDK.intent.action.RESULT_MESSAGE";

	/*
	 * Message
	 */
	public final static String MESSAGE_ID="MSG_reqid";
	public final static String MESSAGE_IN="MSG_input";
	public final static String MESSAGE_ARG="MSG_arg";
	public final static String MESSAGE_AlgType="MSG_algtype";
	public final static String MESSAGE_OUT="MSG_output";
	public final static String MESSAGE_PKGNAME="MSG_pkgname";


	/**生成THID识别服务intent
	 * @param algType	算法类型
	 * @param taskID	任务编号
	 * @param inPath	指定路径
	 * @param jsonArg	指定参数
	 * @return
	 */
	static public Intent GenTHIDServiceIntent( int algType, String taskID, String inPath, String jsonArg ){
		Intent msgIntent = new Intent( );

		if( ALGTYPE_FINGERID == algType ){		//指纹v3算法
			msgIntent.setAction( SERIVCE_FINGERID );
		}else if( ALGTYPE_FACEID == algType ){	//人脸v65算法
			msgIntent.setAction( SERIVCE_FACEID );
		}else if( ALGTYPE_PLATEID == algType ){	//车牌v4算法
			msgIntent.setAction( SERIVCE_PLATEID );
		}else
			return null;

		msgIntent.putExtra( MESSAGE_AlgType, algType );	//算法标识
		msgIntent.putExtra( MESSAGE_ID, taskID );		//返回结果时可以此标记区分
		msgIntent.putExtra( MESSAGE_IN, inPath );		//输入工作路径
		msgIntent.putExtra( MESSAGE_ARG,  jsonArg );	//指定参数

		return msgIntent;
	}

}