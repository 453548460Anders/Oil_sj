package com.megvii.livenesslib.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class DialogUtil {

	private Activity activity;
	private AlertDialog alertDialog;

	public DialogUtil(Activity activity) {
		this.activity = activity;
	}

	public void showDialog(String message) {
		alertDialog = new AlertDialog.Builder(activity)
				.setTitle(message)
				.setNegativeButton("确认", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						activity.finish();
					}
				}).setCancelable(false).create();
		alertDialog.show();
	}

	public void onDestory() {
		if(alertDialog!=null) {
		    alertDialog.dismiss();
		}
		activity = null;
	}

	public void showCheckDialog(String message)
	{
		alertDialog = new AlertDialog.Builder(activity)
				.setTitle(message)
				.setNegativeButton("跳过", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						activity.finish();
					}
				}).setCancelable(false).create();
		alertDialog.show();
	}

	public  void hideDialog()
	{
		if(alertDialog!=null) {
			alertDialog.dismiss();
		}
	}
}