package mywebpage;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.yishi.dongbeizhongyou_siji.tools.Constants;
import com.yishi.dongbeizhongyou_siji.tools.MyUtils;

import java.io.File;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

@SuppressLint("Registered")
@RuntimePermissions
public abstract class PicActivity extends Activity {
    protected String currentPhotoPath;

    private void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PicActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnPermissionDenied({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void permissionDenied4Camera() {
        showToast("未获取拍照、读写权限，无法拍照");
    }

    @OnNeverAskAgain({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void neverAskAgain4Camera() {
        MyUtils.showPermissionSetting(this, "拍照需要您开启拍照、读写权限，请点击设置并开启", "未获取拍照权限，无法拍照");
    }

    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE})
    void takePhoto() {
        currentPhotoPath = MyUtils.createPhotoPath();
        MyUtils.takePhoto(this, currentPhotoPath);
    }

    @OnPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
    void permissionDenied4Read() {
        showToast("未获取读取文件权限，无法选择");
    }

    @OnNeverAskAgain(Manifest.permission.READ_EXTERNAL_STORAGE)
    void neverAskAgain4Read() {
        MyUtils.showPermissionSetting(this, "选择图片需要您开启读写权限，请点击设置并开启", "未获取读写权限，无法选择");
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    void choosePic() {
        MyUtils.choosePic(this);
    }

    public void choosePicWithCheck() {
        PicActivityPermissionsDispatcher.choosePicWithPermissionCheck(this);
    }

    public void takePhotoWithCheck() {
        PicActivityPermissionsDispatcher.takePhotoWithPermissionCheck(this);
    }

    public static String createPhotoPath() {
        String savePath = Constants.FILE_PATH;
        File folder = new File(savePath);
        if (!folder.exists()) folder.mkdirs();
        return Constants.FILE_PATH + System.currentTimeMillis() + ".jpg";
    }
}
