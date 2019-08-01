package com.yishi.dongbeizhongyou_siji.bdmap;

import android.content.Context;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

/**
 * 百度地图
 * Created by Administrator on 2018/1/15 0015.
 */

public class BdmapManager {

    private Context context;
    private LocationClient locationClient;
    private LocationCallback callback;

    private BDLocation locationFront;

    public BdmapManager(Context context) {
        this.context = context;
        init();
    }

    private void init() {
        initLocation();
    }

    private void initLocation() {
        locationClient = new LocationClient(context);
        //声明LocationClient类
        locationClient.registerLocationListener(new MyLocationListener());

        LocationClientOption option = new LocationClientOption();

        option.setIsNeedAddress(true);

        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，设置定位模式，默认高精度
        //LocationMode.Hight_Accuracy：高精度；
        //LocationMode. Battery_Saving：低功耗；
        //LocationMode. Device_Sensors：仅使用设备；
        option.setCoorType("bd09ll");
        //可选，设置返回经纬度坐标类型，默认gcj02
        //gcj02：国测局坐标；
        //bd09ll：百度经纬度坐标；
        //bd09：百度墨卡托坐标；
        //海外地区定位，无需设置坐标类型，统一返回wgs84类型坐标
        option.setScanSpan(0);
        //可选，设置发起定位请求的间隔，int类型，单位ms
        //如果设置为0，则代表单次定位，即仅定位一次，默认为0
        //如果设置非0，需设置1000ms以上才有效
        option.setOpenGps(false);
        //可选，设置是否使用gps，默认false
        //使用高精度和仅用设备两种定位模式的，参数必须设置为true
        option.setLocationNotify(false);
        //可选，设置是否当GPS有效时按照1S/1次频率输出GPS结果，默认false
        option.setIgnoreKillProcess(false);
        //可选，定位SDK内部是一个service，并放到了独立进程。
        //设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnoreKillProcess(true)
        option.setEnableSimulateGps(false);
        //可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false
        locationClient.setLocOption(option);
        //mLocationClient为第二步初始化过的LocationClient对象
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        //更多LocationClientOption的配置，请参照类参考中LocationClientOption类的详细说明
    }

    public void restartLocation() {
        locationClient.restart();
    }

    public void stopLocation() {
        locationClient.stop();
    }

    public void requestLocation(LocationCallback callback) {
        this.callback = callback;
        if (locationFront != null)
            callback.getLocationInfo(locationFront);
        locationClient.start();
    }

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取经纬度相关（常用）的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明

//            double lat = location.getLatitude();    //获取纬度信息
//            double lng = location.getLongitude();    //获取经度信息
            locationFront = location;
            if (callback != null)
                callback.getLocationInfo(location);
            stopLocation();
        }

    }
}
