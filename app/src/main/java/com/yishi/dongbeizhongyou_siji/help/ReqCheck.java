package com.yishi.dongbeizhongyou_siji.help;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.megvii.livenessdetection.bean.FaceIDDataStruct;
import com.megvii.livenesslib.util.Constant;
import com.yishi.dongbeizhongyou_siji.BuildConfig;
import com.yishi.dongbeizhongyou_siji.userinfo.IDcardInfo;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;


import update.DownloadCallback;

import static com.megvii.livenesslib.util.Constant.appkey;
import static com.megvii.livenesslib.util.Constant.appsecret;
import static com.megvii.livenesslib.util.Constant.updateapkpath;
import static com.yishi.dongbeizhongyou_siji.userinfo.IDcardInfo.CheckFailed;
import static com.yishi.dongbeizhongyou_siji.userinfo.IDcardInfo.CheckSuccess;

/**
 * 代码参考
 * 
 * 这里写了一些代码帮助（仅供参考）
 */
public class ReqCheck {

	private static ReqCheck ReqCheck;


	private ReqCheck() {}

	public static ReqCheck getInstance()
	{
		if(ReqCheck == null)
		{
			ReqCheck = new ReqCheck();
		}
		return ReqCheck;
	}

	/**
	 * 获取活体检测的BestImage和Delta 注意：需要在活体检测成功后调用
	 *
	 * 如何获取idDataStruct： （从活体检测器中获取） FaceIDDataStruct idDataStruct =
	 * detector.getFaceIDDataStruct();
	 */
	public void getBestImageAndDelta(FaceIDDataStruct idDataStruct) {
		String delta = idDataStruct.delta; // 获取delta；
		HashMap<String, byte[]> images = (HashMap<String, byte[]>) idDataStruct.images;// 获取所有图片
		for (String key : idDataStruct.images.keySet()) {
			byte[] data = idDataStruct.images.get(key);
			if (key.equals("image_best")) {
				byte[] imageBestData = data;// 这是最好的一张图片
			} else if (key.equals("image_env")) {
				byte[] imageEnvData = data;// 这是一张全景图
			} else {
				// 其余为其他图片，根据需求自取
			}
		}
	}

	/**
	 * 如何调用detect方法
	 */
	private void imageDetect(String imgpath) {
		AsyncHttpClient asyncHttpclient = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		params.put("api_key", appkey);
		params.put("api_secret", appsecret);

		try {
			params.put("image", new File(imgpath));// 传入照片路径
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String url = "https://api.faceid.com/faceid/v1/detect";
		asyncHttpclient.post(url, params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					byte[] responseByte) {
				// 上传成功获取token
				String successStr = new String(responseByte);
				try {
					JSONObject jObject = new JSONObject(successStr)
							.getJSONArray("faces").getJSONObject(0);
					String token = jObject.getString("token");
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				// 上传失败
			}
		});
	}

	/**
	 * 如何调用Verify1.0方法
	 * 
	 * 如何获取idDataStruct： （从活体检测器中获取） FaceIDDataStruct idDataStruct =
	 * detector.getFaceIDDataStruct();
	 */
	public void imageVerify_1(FaceIDDataStruct idDataStruct,String name,String idcard,String imgpath) {
		RequestParams requestParams = new RequestParams();
		requestParams.put("idcard_name", name);
		requestParams.put("idcard_number", idcard);
		try {
			requestParams.put("image_idcard", new File(imgpath));// 传入身份证头像照片路径
		} catch (Exception e) {
		}
		requestParams.put("delta", idDataStruct.delta);
		requestParams.put("api_key", appkey);
		requestParams.put("api_secret", appsecret);

		for (Entry<String, byte[]> entry : idDataStruct.images.entrySet()) {
			requestParams.put(entry.getKey(),
					new ByteArrayInputStream(entry.getValue()));
		}
		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		String url = "https://api.faceid.com/faceid/v1/verify";
		asyncHttpClient.post(url, requestParams,
				new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(int i, Header[] headers, byte[] bytes) {
						try {
							String successStr = new String(bytes);
							JSONObject jsonObject = new JSONObject(successStr);
							if (!jsonObject.has("error")) {
								// 活体最好的一张照片和公安部系统上身份证上的照片比较
								double faceidConfidence = jsonObject
										.getJSONObject("result_faceid")
										.getDouble("confidence");
								JSONObject jsonObject2 = jsonObject
										.getJSONObject("result_faceid")
										.getJSONObject("thresholds");
								double faceidThreshold = jsonObject2
										.getDouble("1e-3");
								double faceidTenThreshold = jsonObject2
										.getDouble("1e-4");
								double faceidHundredThreshold = jsonObject2
										.getDouble("1e-5");

								try {
									// 活体最好的一张照片和拍摄身份证上的照片的比较
									JSONObject jObject = jsonObject
											.getJSONObject("result_idcard");
									double idcardConfidence = jObject
											.getDouble("confidence");
									double idcardThreshold = jObject
											.getJSONObject("thresholds")
											.getDouble("1e-3");
									double idcardT = jObject.getJSONObject(
											"thresholds").getDouble("1e-4");
									double idcardHundredThreshold = jObject
											.getJSONObject("thresholds")
											.getDouble("1e-5");
								} catch (Exception e) {

								}

								String verifyresult = jsonObject.toString();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					@Override
					public void onFailure(int i, Header[] headers,
							byte[] bytes, Throwable throwable) {
						// 请求失败
					}
				});
	}

	/**
	 * 如何调用Verify2.0方法
	 * 
	 * 如何获取idDataStruct： （从活体检测器中获取） FaceIDDataStruct idDataStruct =
	 * detector.getFaceIDDataStruct();
	 */
	public void imageVerify_2(FaceIDDataStruct idDataStruct) {
		RequestParams requestParams = new RequestParams();
		requestParams.put("name", "身份证姓名");
		requestParams.put("idcard", "身份证号码");
		try {
			requestParams.put("image_ref1", new FileInputStream(new File(
					"image_idcard")));// 传入身份证头像照片路径
		} catch (Exception e) {
		}
		requestParams.put("delta", idDataStruct.delta);
		requestParams.put("api_key", "API_KEY");
		requestParams.put("api_secret", "API_SECRET");

		requestParams.put("comparison_type", 1 + "");
		requestParams.put("face_image_type", "meglive");

		for (Entry<String, byte[]> entry : idDataStruct.images.entrySet()) {
			requestParams.put(entry.getKey(),
					new ByteArrayInputStream(entry.getValue()));
		}
		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		String url = "https://api.megvii.com/faceid/v2/verify";
		asyncHttpClient.post(url, requestParams,
				new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(int i, Header[] headers, byte[] bytes) {

						String successStr = new String(bytes);
						JSONObject jsonObject;
						try {
							jsonObject = new JSONObject(successStr);
							if (!jsonObject.has("error")) {
								// 活体最好的一张照片和公安部系统上身份证上的照片比较
								double confidence = jsonObject.getJSONObject(
										"result_faceid")
										.getDouble("confidence");
								JSONObject jsonObject2 = jsonObject
										.getJSONObject("result_faceid")
										.getJSONObject("thresholds");
								double threshold = jsonObject2
										.getDouble("1e-3");
								double tenThreshold = jsonObject2
										.getDouble("1e-4");
								double hundredThreshold = jsonObject2
										.getDouble("1e-5");

								try {
									// 活体最好的一张照片和拍摄身份证上的照片的比较
									JSONObject jObject = jsonObject
											.getJSONObject("result_ref1");
									double idcard_confidence = jObject
											.getDouble("confidence");
									double idcard_threshold = jObject
											.getJSONObject("thresholds")
											.getDouble("1e-3");
									double idcard_tenThreshold = jObject
											.getJSONObject("thresholds")
											.getDouble("1e-4");
									double idcard_hundredThreshold = jObject
											.getJSONObject("thresholds")
											.getDouble("1e-5");
								} catch (Exception e) {

								}
								// 解析faceGen
								JSONObject jObject = jsonObject
										.getJSONObject("face_genuineness");

								float mask_confidence = (float) jObject
										.getDouble("mask_confidence");
								float mask_threshold = (float) jObject
										.getDouble("mask_threshold");
								float screen_replay_confidence = (float) jObject
										.getDouble("screen_replay_confidence");
								float screen_replay_threshold = (float) jObject
										.getDouble("screen_replay_threshold");
								float synthetic_face_confidence = (float) jObject
										.getDouble("synthetic_face_confidence");
								float synthetic_face_threshold = (float) jObject
										.getDouble("synthetic_face_threshold");
							}
						} catch (JSONException e1) {
							e1.printStackTrace();
						}
					}

					@Override
					public void onFailure(int i, Header[] headers,
							byte[] bytes, Throwable throwable) {
						// 请求失败
					}
				});
	}

	/**
	 * ocridcard接口调用 身份信息识别
	 */
	public void imageOCR(String path, final Handler callback) {
		RequestParams rParams = new RequestParams();
		rParams.put("api_key", appkey);
		rParams.put("api_secret", appsecret);
		try {
			rParams.put("image", new File(path));// 身份证照片图片地址
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		rParams.put("legality", 1 + "");// 传入1可以判断身份证是否
										// 被编辑/是真实身份证/是复印件/是屏幕翻拍/是临时身份证
		AsyncHttpClient asyncHttpclient = new AsyncHttpClient();
		String url = "https://api.faceid.com/faceid/v1/ocridcard";
		asyncHttpclient.post(url, rParams, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					byte[] responseByte) {
				String successStr = new String(responseByte);
				Log.e("idcard success",successStr);
				Message msg = new Message();
				try {
					JSONObject jObject = new JSONObject(successStr);
					if ("back".equals(jObject.getString("side"))) {
						// 身份证背后信息
						msg.arg1 = IDcardInfo.Back;
					} else {
						// 身份证正面信息
						msg.arg1 = IDcardInfo.Front;
						IDcardInfo.IDcardName = (String) jObject.get("name");
						IDcardInfo.IDcardNum = (String) jObject.get("id_card_number");
					}
				} catch (Exception e) {
				}

				// success 0  failed 1;
				msg.what = CheckSuccess;
				msg.obj = successStr;
				if(callback!=null) {
					callback.handleMessage(msg);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] responseBody, Throwable error) {
				// 上传失败
				Log.e("idcard failde","statusCode:"+statusCode);
				Message msg = new Message();
				msg.what = CheckFailed;
				if(callback!=null) {
					callback.handleMessage(msg);
				}
			}
		});
	}
	
	/**
	 * 根据byte数组，生成图片
	 */
	public static String saveJPGFile(Context mContext, byte[] data, String key) {
		if (data == null)
			return null;

		File mediaStorageDir = mContext
				.getExternalFilesDir(Constant.cacheImage);

		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				return null;
			}
		}

		BufferedOutputStream bos = null;
		FileOutputStream fos = null;
		try {
			String jpgFileName = System.currentTimeMillis() + ""
					+ new Random().nextInt(1000000) + "_" + key + ".jpg";
			fos = new FileOutputStream(mediaStorageDir + "/" + jpgFileName);
			bos = new BufferedOutputStream(fos);
			bos.write(data);
			return mediaStorageDir.getAbsolutePath() + "/" + jpgFileName;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		return null;
	}

	public void doUpdate(String updateurl, final Handler mHandler, final DownloadCallback callback) {
		File myappfile =  new File(updateapkpath);
		if (myappfile.exists())
		{
			myappfile.delete();
		}
		try {
			myappfile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}


		AsyncHttpClient asyncHttpclient = new AsyncHttpClient();
		if(TextUtils.isEmpty(updateurl))
		{

		}
		asyncHttpclient.get(updateurl, new FileAsyncHttpResponseHandler(myappfile) {

			@Override
			public void onFailure(int i, Header[] headers, Throwable throwable, File file) {

				if(mHandler!=null)
				{
					Message msg = new Message();
					msg.what = 1;
					mHandler.handleMessage(msg);
				}

			}

			@Override
			public void onSuccess(int i, Header[] headers, File file) {

				if(mHandler!=null)
				{
					Message msg = new Message();
					msg.what = 0;
					mHandler.handleMessage(msg);
				}

			}

			@Override
			public void onProgress(int bytesWritten, int totalSize) {
				super.onProgress(bytesWritten, totalSize);
				float wsize = bytesWritten;
				float tsize = totalSize;
				float progress = (wsize/tsize)*100;
				callback.doUpdateProgress((int) progress);
			}
		});
	}

	//
	public void getUpdateInfo(final Handler mHandler) {
		AsyncHttpClient asyncHttpclient = new AsyncHttpClient();
		String url = "http://lh.ch000.com/plug/js/appApi.ashx?type=GetVersion?searchtype=0";
		asyncHttpclient.get(url, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers,
								  byte[] responseByte) {
				// 上传成功获取token
				String successStr = new String(responseByte);
				try {
					JSONObject jObject = new JSONObject(successStr);
					String version = (String) jObject.get("version");
					if(compareToUpdate(getVersion(version)))
					{
						String path = (String) jObject.get("path");
//						path = "http://otherbk.d.ireader.com/group8/M00/2E/31/wKgHhlr1EhGEGmUmAAAAAORkabc636391309.apk?v=62LRIAft&t=wKgHhlr1EhE.&p2=108044";
						if(mHandler!=null)
						{
							Message msg = new Message();
							msg.what = 3;
							msg.obj  = path;
							mHandler.handleMessage(msg);
						}

					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
								  byte[] responseBody, Throwable error) {

			}
		});
	}

	private String getVersion(String version)
	{
		String versionNum = null;
		if(version.contains("v"))
		{
			String[] versionArry = version.split("v");
			versionNum = versionArry[1];
		}
		return versionNum;
	}

	private boolean compareToUpdate(String versionNum)
	{
		float latestVersion = Float.valueOf(versionNum);
		String versionName = BuildConfig.VERSION_NAME;
		float mVersion = Float.valueOf(versionName);
		return latestVersion>mVersion ? true:false;

	}



}