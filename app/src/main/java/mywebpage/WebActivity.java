package mywebpage;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.SeekBar;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.megvii.idcardlib.util.SharedUtil;
import com.megvii.livenesslib.util.ConUtil;
import com.megvii.livenesslib.util.Constant;
import com.yishi.dongbeizhongyou_siji.BuildConfig;
import com.yishi.dongbeizhongyou_siji.LoadingActivity;
import com.yishi.dongbeizhongyou_siji.R;
import com.yishi.dongbeizhongyou_siji.ResultActivity;
import com.yishi.dongbeizhongyou_siji.bdmap.BdmapManager;
import com.yishi.dongbeizhongyou_siji.bdmap.LocationCallback;
import com.yishi.dongbeizhongyou_siji.help.ReqCheck;
import com.yishi.dongbeizhongyou_siji.statusbar.StatusBarCompat;
import com.yishi.dongbeizhongyou_siji.tools.MyUtils;
import com.yishi.dongbeizhongyou_siji.tools.RequestCode;
import com.yishi.dongbeizhongyou_siji.tools.ThreadUtil;
import com.yishi.dongbeizhongyou_siji.userinfo.IDcardInfo;
import com.yishi.dongbeizhongyou_siji.userinfo.LiveInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import cn.jpush.android.api.JPushInterface;
import idcardproject.IDLoadingActivity;
import idcardproject.IDResultActivity;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;
import update.DownloadCallback;

@RuntimePermissions
public class WebActivity extends PicActivity {

    private WebView mWebView;
    private SharedUtil mShareUtil;
    private BdmapManager bdmapManager;
    private boolean mDoGetUpdate = false;
    String regId;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        regId = JPushInterface.getRegistrationID(this);
        StatusBarCompat.setStatusBarColor(this, getResources().getColor(R.color.statusbar_col));
        bdmapManager = new BdmapManager(getApplicationContext());
        initView();
        initData();
        WebActivityPermissionsDispatcher.needsPermission4LocationWithPermissionCheck(this);
        getUpdate();

    }

    @Override
    protected void onDestroy() {
        bdmapManager.stopLocation();
        super.onDestroy();
    }

    private void initData() {
        mWebView.loadUrl("file:///android_asset/index.html?reg=123123");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle bundle = intent.getExtras();
        if (bundle != null) {

            boolean IDcardCheckJump = bundle.getBoolean(IDcardInfo.IDcardCheckJump, false);
            if (IDcardCheckJump) {
                uploadJump();
            }
            boolean IDCheck = bundle.getBoolean(IDcardInfo.IDcardCheck, false);
            if (IDCheck) {
                boolean IDresult = bundle.getBoolean(IDcardInfo.IDcardCheckResult, false);
                uploadIdcardInfo(IDresult);
            }

            boolean LIVECheck = bundle.getBoolean(LiveInfo.LiveCheck, false);
            if (LIVECheck) {
                boolean LIVEresult = bundle.getBoolean(LiveInfo.LiveCheckResult, false);
                uploadLiveCheckResult(LIVEresult);
                uploadLiveOrcPic();
            }
        }
    }

    private void uploadJump() {
        mWebView.post(new Runnable() {
            @Override
            public void run() {

                // 注意调用的JS方法名要对应上
                // 调用javascript的callJS()方法
                mWebView.loadUrl("javascript:skip()");
            }
        });
    }

    private void uploadIdcardInfo(boolean IDresult) {
        if (IDresult) {
            final String idcardFrontRsp = mShareUtil.getStringValueByKey(IDcardInfo.IDcardFront);
            final String idcardBackRsp = mShareUtil.getStringValueByKey(IDcardInfo.IDcardBack);
            uploadIdCardOrcPic();
            mWebView.post(new Runnable() {
                @Override
                public void run() {

                    // 注意调用的JS方法名要对应上
                    // 调用javascript的callJS()方法
                    mWebView.loadUrl("javascript:idcardinfo(" + idcardFrontRsp + "," + idcardBackRsp + ")");
                }
            });
        }
    }

    private void uploadLiveCheckResult(boolean LIVEresult) {
        if (LIVEresult) {
            mWebView.post(new Runnable() {
                @Override
                public void run() {

                    // 注意调用的JS方法名要对应上
                    // 调用javascript的callJS()方法
                    mWebView.loadUrl("javascript:livecheckresult(0)");
                }
            });

        } else {
            mWebView.post(new Runnable() {
                @Override
                public void run() {

                    // 注意调用的JS方法名要对应上
                    // 调用javascript的callJS()方法
                    mWebView.loadUrl("javascript:livecheckresult(1)");
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void initView() {
        mWebView = (WebView) findViewById(R.id.mywebview);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setSavePassword(false);
        webSettings.setSupportZoom(false);
        webSettings.setTextSize(WebSettings.TextSize.NORMAL);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        mWebView.setWebViewClient(mWebViewClient);
        mWebView.addJavascriptInterface(new CallBack(), "CallBack");
        mShareUtil = new SharedUtil(this);
    }

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("tel:")) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                startActivity(intent);
                view.reload();
                return true;
            } else if (url.startsWith("weixin://") || url.startsWith("scheme:")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }

            view.loadUrl(url);
            return true;
        }

        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            return super.shouldOverrideKeyEvent(view, event);
        }
    };

    public class CallBack {

        @JavascriptInterface
        public void liveNess() {
            startActivity(new Intent(WebActivity.this, LoadingActivity.class));
        }

        @JavascriptInterface
        public void idCard() {
            startActivity(new Intent(WebActivity.this, IDLoadingActivity.class));
        }

        /*
        * mWebView.post(new Runnable() {
                        @Override
                        public void run() {

                            // 注意调用的JS方法名要对应上
                            // 调用javascript的callJS()方法
                            mWebView.loadUrl("javascript:lola(30,40)");
                        }
                    });
                    */
        @JavascriptInterface
        public void lola(String LO, String LA) {
            Log.e("lola", LO + ":" + LA);
            Toast.makeText(WebActivity.this, LO + ":" + LA, Toast.LENGTH_LONG).show();
        }

        @JavascriptInterface
        public void Locate() {
            WebActivityPermissionsDispatcher.needsPermission4LocationWithPermissionCheck(WebActivity.this);
        }

        @JavascriptInterface
        public void takephoto() {
            takePhotoWithCheck();
        }

        @JavascriptInterface
        public void pickpic() {
            choosePicWithCheck();
        }

        @JavascriptInterface
        public void Finish() {
            finish();
        }
        
        @JavascriptInterface
        public String register() {
            return regId;
        }
        
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    void needsPermission4Location() {
        locate();
    }

    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
    void permissionDenied4Location() {
        Toast.makeText(this,"未能获取定位权限,无法定位", Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_FINE_LOCATION)
    void neverAskAgain4Location() {
        MyUtils.showPermissionSetting(this, "请开启定位权限","获取定位权限失败");
    }

    private void locate() {
        bdmapManager.requestLocation(new LocationCallback() {
            @Override
            public void getLocationInfo(BDLocation location) {
                double jd = location.getLongitude();
                double wd = location.getLatitude();
                String city = location.getCity();
                String cityCode = location.getCityCode();


                int code = location.getLocType();
                if (!(code == 161 || code == 61 || code == 65)) {
                    jd = 0;
                    wd = 0;
                    city = "";
                    cityCode = "";
                }
                updatelocate(jd, wd, city, cityCode);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        WebActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    private void updatelocate(final double lo, final double la, final String cityName, final String cityCode) {
        mWebView.post(new Runnable() {
            @Override
            public void run() {

                // 注意调用的JS方法名要对应上
                // 调用javascript的callJS()方法
                mWebView.loadUrl("javascript:lola(" + lo + "," + la + ",\"" + cityName + "\",\"" + cityCode + "\")");
            }
        });
    }

    private void setRegisterId(final String regid) {
        mWebView.loadUrl("javascript:jgregister(" + regid + ")");
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == RequestCode.TAKE_PHOTO) {
                savePic(Uri.fromFile(new File(currentPhotoPath)));
            } else if (requestCode == RequestCode.CHOOSE_PIC) {
                savePic(data.getData());
            }
        }
    }

    private void savePic(final Uri data) {
        ThreadUtil.runOnThread(new Runnable() {
            @Override
            public void run() {
                String path = createPhotoPath();
                MyUtils.saveFile(path, data, WebActivity.this);

                final JSONObject JSbase64 = new JSONObject();
                String b64 = MyUtils.bitmapToString(path);
                try {
                    JSbase64.put("imgbase64", b64);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl("javascript:imgbase64(" + JSbase64.toString() + ")");
                    }
                });
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private String getImagePath(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1]; // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果不是document类型的Uri，则使用普通方式处理
            imagePath = getImagePath(uri, null);
        }
        return imagePath;
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null,
                null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    public static boolean base64ToFile(String base64Str, String path) {
        byte[] data = Base64.decode(base64Str, Base64.DEFAULT);
        for (int i = 0; i < data.length; i++) {
            if (data[i] < 0) {
                //调整异常数据
                data[i] += 256;
            }
        }
        OutputStream os = null;
        try {
            os = new FileOutputStream(path);
            os.write(data);
            os.flush();
            os.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    private void uploadIdCardOrcPic() {
        final JSONObject JSbase64 = new JSONObject();
        String imgbase64front = MyUtils.bitmapToString(IDResultActivity.mFrontPath);
        final JSONObject JSbase64Back = new JSONObject();
        String imgbase64back = MyUtils.bitmapToString(IDResultActivity.mBackPath);
        try {
            JSbase64.put("imgbase64front", imgbase64front);
            JSbase64Back.put("imgbase64back", imgbase64back);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mWebView.post(new Runnable() {
            @Override
            public void run() {

                // 注意调用的JS方法名要对应上
                // 调用javascript的callJS()方法
                mWebView.loadUrl("javascript:idcardimgbase64(" + JSbase64.toString() + ", " + JSbase64Back.toString() +")");
            }
        });
    }

    private void uploadLiveOrcPic() {
        final JSONObject JSbase64 = new JSONObject();
        String imgbase64bestPath = MyUtils.bitmapToString(ResultActivity.bestPath);
        String imgbase64envPath = MyUtils.bitmapToString(ResultActivity.envPath);
        try {
            JSbase64.put("imgbase64bestPath", imgbase64bestPath);
            JSbase64.put("imgbase64envPath", imgbase64envPath);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mWebView.post(new Runnable() {
            @Override
            public void run() {

                // 注意调用的JS方法名要对应上
                // 调用javascript的callJS()方法
                mWebView.loadUrl("javascript:liveimgbase64(" + JSbase64.toString() + ")");
            }
        });
    }

    private void getUpdate() {
        if (!mDoGetUpdate) {
            ReqCheck.getInstance().getUpdateInfo(mUpdateHandler);
            mDoGetUpdate = true;
        }
    }

    private void doUpdate(String updatepath) {
        ReqCheck.getInstance().doUpdate(updatepath, mUpdateHandler, new DownloadCallback() {

            @Override
            public void doUpdateProgress(int p) {
                seekBar.setProgress(p);
            }
        });
    }


    Handler mUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            /// 0 :下载完 1：失败  3：需要更新
            switch (msg.what) {
                case 0:
                    dismissUpdateDialog();
                    installAPK();
                    break;
                case 1:
                    dismissUpdateDialog();
                    ConUtil.showToast(WebActivity.this, getString(R.string.updateapkfailed));
//                    finish();
                    break;
                case 3:
                    if (msg.obj != null) {

                        if (TextUtils.isEmpty((String) msg.obj)) {
                            return;
                        }

                        showUpdateDialog((String) msg.obj);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void installAPK() {
        File apkFile = new File(Constant.updateapkpath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(
                    this
                    , BuildConfig.APPLICATION_ID
                    , apkFile);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        startActivity(intent);
    }

    private void dismissUpdateDialog() {
        if (updateDialog != null) {
            updateDialog.dismiss();
        }
    }

    private void updateSeekbarProgress(int writen, int total) {
        float wsize = writen;
        float tsize = total;

        float progress = (writen / total) * 100;

        seekBar.setProgress((int) progress);
    }

    Dialog updateDialog;
    SeekBar seekBar;

    private void showUpdateDialog(final String updatepath) {
        updateDialog = new Dialog(this);
        updateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        updateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        updateDialog.setContentView(R.layout.dialog_update_layout);
        updateDialog.setCanceledOnTouchOutside(false);
        seekBar = (SeekBar) updateDialog.findViewById(R.id.update_dialog_seekbar);
        updateDialog.findViewById(R.id.update_dialog_sure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekBar.setVisibility(View.VISIBLE);
                doUpdate(updatepath);
            }
        });
        updateDialog.findViewById(R.id.update_dialog_cancle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDialog.dismiss();
            }
        });
        updateDialog.show();
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    // 注意调用的JS方法名要对应上
                    // 调用javascript的callJS()方法
                    mWebView.loadUrl("javascript:back()");
                }
            });
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
}

