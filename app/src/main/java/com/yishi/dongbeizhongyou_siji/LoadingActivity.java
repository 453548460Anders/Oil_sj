package com.yishi.dongbeizhongyou_siji;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.megvii.licensemanager.Manager;
import com.megvii.livenessdetection.Detector;
import com.megvii.livenessdetection.LivenessLicenseManager;
import com.megvii.livenesslib.LivenessActivity;
import com.megvii.livenesslib.util.ConUtil;
import com.megvii.livenesslib.util.SharedUtil;
import mywebpage.WebActivity;
import com.yishi.dongbeizhongyou_siji.view.RotaterView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import static android.os.Build.VERSION_CODES.M;

public class LoadingActivity extends Activity implements View.OnClickListener {

    private String uuid;
    private LinearLayout barLinear;
    private Button btn;
    private TextView WarrantyText;
    private ProgressBar WarrantyBar;
    private Button againWarrantyBtn;
    private SharedUtil mSharedUtil;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_layout);

        init();
        netWorkWarranty();
    }

    private void init() {
        mSharedUtil = new SharedUtil(this);
        uuid = ConUtil.getUUIDString(this);
        barLinear = (LinearLayout) findViewById(R.id.loading_layout_barLinear);
        WarrantyText = (TextView) findViewById(R.id.loading_layout_WarrantyText);
        WarrantyBar = (ProgressBar) findViewById(R.id.loading_layout_WarrantyBar);
        againWarrantyBtn = (Button) findViewById(R.id.loading_layout_againWarrantyBtn);
        againWarrantyBtn.setOnClickListener(this);
        btn = (Button) findViewById(R.id.loading_layout_livenessBtn);
        btn.setOnClickListener(this);
        TextView versionNameView = ((TextView) findViewById(R.id.loading_layout_version));
        versionNameView.setText(Detector.getVersion());
    }

    /**
     * 联网授权
     */
    private void netWorkWarranty() {
        btn.setVisibility(View.GONE);
        barLinear.setVisibility(View.VISIBLE);
        againWarrantyBtn.setVisibility(View.GONE);
        WarrantyText.setText(R.string.meglive_auth_progress);
        WarrantyBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Manager manager = new Manager(LoadingActivity.this);
                LivenessLicenseManager licenseManager = new LivenessLicenseManager(LoadingActivity.this);
                manager.registerLicenseManager(licenseManager);
                manager.takeLicenseFromNetwork(uuid);

                if (licenseManager.checkCachedLicense() > 0) {
                    //授权成功
                    mHandler.sendEmptyMessage(1);
                }else {
                    //授权失败
                    mHandler.sendEmptyMessage(2);
                }
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.loading_layout_livenessBtn) {
            requestCameraPerm();
        } else if (id == R.id.loading_layout_againWarrantyBtn) {
            netWorkWarranty();
        }
    }

    private void requestCameraPerm() {
        if (android.os.Build.VERSION.SDK_INT >= M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                //进行权限请求
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        EXTERNAL_STORAGE_REQ_CAMERA_CODE);
            } else {
                enterNextPage();
            }
        } else {
            enterNextPage();
        }
    }

    private void enterNextPage() {
        startActivityForResult(new Intent(this, LivenessActivity.class), PAGE_INTO_LIVENESS);
    }

    public static final int EXTERNAL_STORAGE_REQ_CAMERA_CODE = 10;


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == EXTERNAL_STORAGE_REQ_CAMERA_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {// Permission Granted

                ConUtil.showToast(this, "获取相机权限失败");
            } else
                enterNextPage();
        }
    }


    private static final int PAGE_INTO_LIVENESS = 100;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAGE_INTO_LIVENESS && resultCode == RESULT_OK) {
//            String result = data.getStringExtra("result");
//            String delta = data.getStringExtra("delta");
//            Serializable images=data.getSerializableExtra("images");
//
            Bundle bundle=data.getExtras();
            ResultActivity.startActivity(this, bundle);
//            dataParse(bundle);
        }
    }

    private void dataParse(Bundle bundle)
    {
        String resultOBJ = bundle.getString("result");
        try {
            JSONObject result = new JSONObject(resultOBJ);
            int resID = result.getInt("resultcode");
            checkID(resID);
            boolean isSuccess = result.getString("result").equals(
                    getResources().getString(R.string.verify_success));
            if (isSuccess) {
                String delta = bundle.getString("delta");
                Map<String, byte[]> images = (Map<String, byte[]>) bundle.getSerializable("images");
                byte[] image_best = images.get("image_best");
                byte[] image_env = images.get("image_env");
                //N张动作图根据需求自取，对应字段action1、action2 ...
                // byte[] image_action1 = images.get("image_action1);

                //保存图片
                //bestPath = ConUtil.saveJPGFile(this, image_best, "image_best");
                //envPath = ConUtil.saveJPGFile(this, image_env, "image_env");
                //调用活体比对API
                //imageVerify(images,delta);
            }
            doRotate(isSuccess);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void checkID(int resID) {
        if (resID == R.string.verify_success) {
            doPlay(R.raw.meglive_success);
        } else if (resID == R.string.liveness_detection_failed_not_video) {
            doPlay(R.raw.meglive_failed);
        } else if (resID == R.string.liveness_detection_failed_timeout) {
            doPlay(R.raw.meglive_failed);
        } else if (resID == R.string.liveness_detection_failed) {
            doPlay(R.raw.meglive_failed);
        } else {
            doPlay(R.raw.meglive_failed);
        }
    }
    // 人声提醒
    private MediaPlayer mMediaPlayer = null;
    private void doPlay(int rawId) {
        if (mMediaPlayer == null)
            mMediaPlayer = new MediaPlayer();

        mMediaPlayer.reset();
        try {
            AssetFileDescriptor localAssetFileDescriptor = getResources()
                    .openRawResourceFd(rawId);
            mMediaPlayer.setDataSource(
                    localAssetFileDescriptor.getFileDescriptor(),
                    localAssetFileDescriptor.getStartOffset(),
                    localAssetFileDescriptor.getLength());
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (Exception localIOException) {
            localIOException.printStackTrace();
        }
    }
    private void doRotate(boolean success) {
        RotaterView rotaterView = (RotaterView) findViewById(R.id.result_rotater);
        rotaterView.setColour(success ? 0xff4ae8ab : 0xfffe8c92);
        final ImageView statusView = (ImageView) findViewById(R.id.result_status);
        statusView.setVisibility(View.INVISIBLE);
        statusView.setImageResource(success ? R.drawable.result_success
                : R.drawable.result_failded);

        ObjectAnimator objectAnimator = ObjectAnimator.ofInt(rotaterView,
                "progress", 0, 100);
        objectAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        objectAnimator.setDuration(600);
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Animation scaleanimation = AnimationUtils.loadAnimation(
                        LoadingActivity.this, R.anim.scaleoutin);
                statusView.startAnimation(scaleanimation);
                statusView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        objectAnimator.start();
    }

    private void startWebpage()
    {
        Intent intent = new Intent(this, WebActivity.class);
        startActivity(intent);

    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    btn.setVisibility(View.VISIBLE);
                    barLinear.setVisibility(View.GONE);
                    break;
                case 2:
                    againWarrantyBtn.setVisibility(View.VISIBLE);
                    WarrantyText.setText(R.string.meglive_auth_failed);
                    WarrantyBar.setVisibility(View.GONE);
                    break;
            }
        }
    };

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if(event.getKeyCode()==KeyEvent.KEYCODE_BACK)
        {
            ConUtil.showToast(this,getResources().getString(R.string.nojump));
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
}