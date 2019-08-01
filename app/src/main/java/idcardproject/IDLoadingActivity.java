package idcardproject;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.megvii.idcardlib.IDCardScanActivity;
import com.megvii.idcardlib.util.SharedUtil;
import com.megvii.idcardlib.util.Util;
import com.megvii.idcardquality.IDCardQualityLicenseManager;
import com.megvii.licensemanager.Manager;
import com.yishi.dongbeizhongyou_siji.R;
import com.yishi.dongbeizhongyou_siji.userinfo.IDcardInfo;

import mywebpage.WebActivity;

import static android.os.Build.VERSION_CODES.M;
import static com.yishi.dongbeizhongyou_siji.userinfo.IDcardInfo.IDcardCheckJump;

/**
 * Created by binghezhouke on 15-8-12.
 */
public class IDLoadingActivity extends Activity implements View.OnClickListener {

	private Button selectBtn;
	private Button jumpBtn;
	boolean isVertical;
	private RelativeLayout contentRel;
	private LinearLayout barLinear;
	private TextView WarrantyText;
	private ProgressBar WarrantyBar;
	private Button againWarrantyBtn;
	private String uuid;
	private SharedUtil mShareUtil;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			setContentView(R.layout.activity_id_loading);
			init();
			initData();
			network();
			//
			clearForNewUser();
		}

	}

	private void init() {
		contentRel = (RelativeLayout) findViewById(R.id.loading_layout_contentRel);
		barLinear = (LinearLayout) findViewById(R.id.loading_layout_barLinear);
		WarrantyText = (TextView) findViewById(R.id.loading_layout_WarrantyText);
		WarrantyBar = (ProgressBar) findViewById(R.id.loading_layout_WarrantyBar);
		againWarrantyBtn = (Button) findViewById(R.id.loading_layout_againWarrantyBtn);
		selectBtn = (Button) findViewById(R.id.loading_layout_isVerticalBtn);
		jumpBtn = (Button) findViewById(R.id.idcard_jump);
		selectBtn.setOnClickListener(this);
		jumpBtn.setOnClickListener(this);
		uuid = Util.getUUIDString(this);
		findViewById(R.id.loading_back).setOnClickListener(this);
		findViewById(R.id.loading_front).setOnClickListener(this);
		mShareUtil = new SharedUtil(this);
	}

	private void initData() {
		if (isVertical) {
			selectBtn.setText("vertical");
		}
		else {
			selectBtn.setText("horizontal");
		}

	}

	private void clearForNewUser()
	{
		if(mShareUtil.contains(IDcardInfo.IDcardFront)) {

			mShareUtil.removeSharePreferences(IDcardInfo.IDcardFront);
		}
		if(mShareUtil.contains(IDcardInfo.IDcardBack))
		{
			mShareUtil.removeSharePreferences(IDcardInfo.IDcardBack);
		}
	}

	/**
	 * 联网授权
	 */
	private void network() {
		contentRel.setVisibility(View.GONE);
		barLinear.setVisibility(View.VISIBLE);
		againWarrantyBtn.setVisibility(View.GONE);
		WarrantyText.setText("正在联网授权中...");
		WarrantyBar.setVisibility(View.VISIBLE);
		new Thread(new Runnable() {
			@Override
			public void run() {
				Manager manager = new Manager(IDLoadingActivity.this);
				IDCardQualityLicenseManager idCardLicenseManager = new IDCardQualityLicenseManager(
						IDLoadingActivity.this);
				manager.registerLicenseManager(idCardLicenseManager);
				manager.takeLicenseFromNetwork(uuid);
				if (idCardLicenseManager.checkCachedLicense() > 0) {
					//授权成功
					UIAuthState(true);
				}else {
					//授权失败
					UIAuthState(false);
				}
			}
		}).start();
	}

	private void UIAuthState(final boolean isSuccess) {
		runOnUiThread(new Runnable() {
			public void run() {
				authState(isSuccess);
			}
		});
	}

	private void authState(boolean isSuccess) {
		if (isSuccess) {
			barLinear.setVisibility(View.GONE);
			WarrantyBar.setVisibility(View.GONE);
			againWarrantyBtn.setVisibility(View.GONE);
			contentRel.setVisibility(View.VISIBLE);
		} else {
			barLinear.setVisibility(View.VISIBLE);
			WarrantyBar.setVisibility(View.GONE);
			againWarrantyBtn.setVisibility(View.VISIBLE);
			contentRel.setVisibility(View.GONE);
			WarrantyText.setText("联网授权失败！请检查网络或找服务商");
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.loading_layout_againWarrantyBtn:
				network();
				break;
			case R.id.loading_layout_isVerticalBtn:
				isVertical = !isVertical;
				initData();
				break;
			case R.id.loading_front: {
				requestCameraPerm(0);
				break;
			}
			case R.id.loading_back: {
				requestCameraPerm(1);
				break;
			}
			case R.id.idcard_jump: {
				startWebpage();
				break;
			}
			default:
				break;
		}
	}

	int mSide = 0;
	private void requestCameraPerm(int side) {
		mSide = side;
		if (android.os.Build.VERSION.SDK_INT >= M) {
			if (ContextCompat.checkSelfPermission(this,
					Manifest.permission.CAMERA)
					!= PackageManager.PERMISSION_GRANTED) {
				//进行权限请求
				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.CAMERA},
						EXTERNAL_STORAGE_REQ_CAMERA_CODE);
			} else {
				enterNextPage(side);
			}
		} else {
			enterNextPage(side);
		}
	}

	private void enterNextPage(int side){
		Intent intent = new Intent(this, IDCardScanActivity.class);
		intent.putExtra("side", side);
		intent.putExtra("isvertical", isVertical);
		startActivityForResult(intent, INTO_IDCARDSCAN_PAGE);
	}

	public static final int EXTERNAL_STORAGE_REQ_CAMERA_CODE = 10;

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		if (requestCode == EXTERNAL_STORAGE_REQ_CAMERA_CODE) {
			if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {// Permission Granted

				Util.showToast(this, "获取相机权限失败");
			} else
				enterNextPage(mSide);
		}
	}


	private static final int INTO_IDCARDSCAN_PAGE = 100;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == INTO_IDCARDSCAN_PAGE && resultCode == RESULT_OK) {
			Intent intent = new Intent(this, IDResultActivity.class);
			intent.putExtra("side", data.getIntExtra("side", 0));
			intent.putExtra("idcardImg", data.getByteArrayExtra("idcardImg"));
			if (data.getIntExtra("side", 0) == 0) {
				intent.putExtra("portraitImg", data.getByteArrayExtra("portraitImg"));
			}
			startActivity(intent);
		}
	}

	private void startWebpage()
	{
		Intent intent = new Intent(this, WebActivity.class);
		Bundle bundle = new Bundle();
		bundle.putBoolean(IDcardCheckJump,true);
		intent.putExtras(bundle);
		startActivity(intent);
		finish();

	}
}