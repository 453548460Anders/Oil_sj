package idcardproject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import android.widget.TextView;

import com.megvii.idcardlib.util.SharedUtil;
import com.megvii.idcardquality.bean.IDCardAttr;
import com.megvii.livenesslib.util.ConUtil;
import com.megvii.livenesslib.util.DialogUtil;
import com.yishi.dongbeizhongyou_siji.R;
import com.yishi.dongbeizhongyou_siji.help.ReqCheck;
import com.yishi.dongbeizhongyou_siji.userinfo.IDcardInfo;

import mywebpage.WebActivity;

import static com.yishi.dongbeizhongyou_siji.userinfo.IDcardInfo.CheckFailed;
import static com.yishi.dongbeizhongyou_siji.userinfo.IDcardInfo.CheckSuccess;
import static com.yishi.dongbeizhongyou_siji.userinfo.IDcardInfo.IDcardCheck;
import static com.yishi.dongbeizhongyou_siji.userinfo.IDcardInfo.IDcardCheckResult;

/**
 * The type Result activity.
 */
public class IDResultActivity extends Activity {
	private ImageView mIDCardImageView;
	private ImageView mPortraitImageView;
	private TextView mIDCardSize;
	private TextView mPortraitSize;
	IDCardAttr.IDCardSide mIDCardSide;
	private DialogUtil mDialogUtil;
	private SharedUtil mShareUtil;
	public static String mFrontPath;
	public static String mBackPath;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_id_resutl);

		mIDCardSide = getIntent().getIntExtra("side", 0) == 0 ? IDCardAttr.IDCardSide.IDCARD_SIDE_FRONT
				: IDCardAttr.IDCardSide.IDCARD_SIDE_BACK;
		init();
	}

	/**
	 * SDK 获取质量合格的身份证图片，识别信息通过调用OCR API 来实现。
	 */
	void init() {
		mDialogUtil = new DialogUtil(this);
		mShareUtil = new SharedUtil(this);
		mIDCardImageView = (ImageView) findViewById(R.id.result_idcard_image);
		mPortraitImageView = (ImageView) findViewById(R.id.result_portrait_image);

		mIDCardSize = (TextView) findViewById(R.id.result_idcard_size);
		mPortraitSize = (TextView) findViewById(R.id.result_portrait_size);
		{
			byte[] idcardImgData = getIntent().getByteArrayExtra("idcardImg");
			Bitmap idcardBmp = BitmapFactory.decodeByteArray(idcardImgData, 0,
					idcardImgData.length);
			mIDCardImageView.setImageBitmap(idcardBmp);
			mIDCardSize.setText(idcardBmp.getWidth() + "_"
					+ idcardBmp.getHeight());
			// get idcard info with img
			mDialogUtil.showCheckDialog(getResources().getString(R.string.ocridcrd));
			String path = ConUtil.saveBitmap(this,idcardBmp);
			if (mIDCardSide == IDCardAttr.IDCardSide.IDCARD_SIDE_FRONT) {
				mFrontPath = path;
			}else
			{
				mBackPath = path;
			}
			ReqCheck.getInstance().imageOCR(path,mHandler);
		}
		if (mIDCardSide == IDCardAttr.IDCardSide.IDCARD_SIDE_FRONT) {
			byte[] portraitImgData = getIntent().getByteArrayExtra(
					"portraitImg");
			Bitmap img = BitmapFactory.decodeByteArray(portraitImgData, 0,
					portraitImgData.length);
			mPortraitImageView.setImageBitmap(img);
			mPortraitSize.setText(img.getWidth() + "_" + img.getHeight());
		}
	}
	Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			mDialogUtil.hideDialog();
			switch (msg.what)
			{
				case CheckSuccess:
					ConUtil.showToast(IDResultActivity.this,getResources().getString(R.string.ocridcrdsuccess));
					if(msg.arg1 == IDcardInfo.Front) {
						mShareUtil.saveStringValue(IDcardInfo.IDcardFront, (String) msg.obj);
						// 检校其他信息
						if(mShareUtil.getStringValueByKey(IDcardInfo.IDcardBack)!=null)
						{
							ConUtil.showToast(IDResultActivity.this,getResources().getString(R.string.livecheck));
							startWebpage();
						}else
						{
							ConUtil.showToast(IDResultActivity.this,getResources().getString(R.string.ocridcrdback));
							finish();
						}
					}else
					{
						mShareUtil.saveStringValue(IDcardInfo.IDcardBack, (String) msg.obj);
						if(mShareUtil.getStringValueByKey(IDcardInfo.IDcardFront)!=null)
						{
							ConUtil.showToast(IDResultActivity.this,getResources().getString(R.string.livecheck));
							startWebpage();
						}else
						{
							ConUtil.showToast(IDResultActivity.this,getResources().getString(R.string.ocridcrdfront));
							finish();
						}
					}
					break;
				case CheckFailed:
					ConUtil.showToast(IDResultActivity.this,getResources().getString(R.string.ocridcrdfailed));
					break;
				default:
					break;
			}

		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mHandler = null;
	}

	private void startWebpage()
	{
		Intent intent = new Intent(this, WebActivity.class);
		Bundle bundle = new Bundle();
		bundle.putBoolean(IDcardCheckResult,true);
		bundle.putBoolean(IDcardCheck,true);
		intent.putExtras(bundle);
		startActivity(intent);
		finish();

	}

}