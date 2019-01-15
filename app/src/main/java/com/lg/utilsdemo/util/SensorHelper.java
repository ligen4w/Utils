package com.lg.utilsdemo.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.OrientationEventListener;

import java.lang.ref.WeakReference;

/**
 * 重力感应帮助类
 * Created by ligen on 2018/8/29.
 */

public class SensorHelper {

    private WeakReference<Activity> wr_activity;
    private boolean portrait = false;
    private boolean reversePortrait = false;
    private boolean landscape = false;
    private boolean reverseLandscape = false;
    private boolean isEnable;

    private OrientationEventListener eventListener;
    private ContentObserver contentObserver;
    private ContentResolver contentResolver;

    public SensorHelper(Activity activity) {
        contentResolver = activity.getApplicationContext().getContentResolver();
        wr_activity = new WeakReference<>(activity);
        eventListener = new OrientationEventListener(activity.getApplicationContext()){
            @Override
            public void onOrientationChanged(int orientation) {
                if(mOnDegreeChangedListener != null){
                    mOnDegreeChangedListener.onDegreeChanged(orientation);
                }
                Activity activity = wr_activity.get();
                if(activity != null){
                    int screenOrientation = activity.getRequestedOrientation();
                    /**
                     * 当portrait、reversePortrait、landscape和reverseLandscape全设置为false时，
                     * screenOrientation值为ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                     */
                    if(screenOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED){
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                    if (((orientation >= 0) && (orientation < 45)) || (orientation > 315)) {
                        //设置竖屏
                        if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                            if(portrait){
                                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                            }
                        }
                    } else if (orientation > 225 && orientation < 315) {
                        //设置横屏
                        if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                            if(landscape){
                                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                            }
                        }
                    } else if (orientation > 45 && orientation < 135) {
                        // 设置反向横屏
                        if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                            if(reverseLandscape){
                                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                            }
                        }
                    } else if (orientation > 135 && orientation < 225) {
                        if (screenOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
                            if(reversePortrait){
                                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                            }
                        }
                    }
                }
            }
        };

        contentObserver = new ContentObserver(new Handler()){
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                //监听是否打开“自动旋转”设置
                boolean autoRotateOn = (Settings.System.getInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0) == 1);
                //根据系统设置来控制重力感应是否可用
                if (autoRotateOn) {
                    enable();
                }else{
                    disable();
                }
            }
        };
    }


    /**
     * 设置支持的旋转方向
     * @param portrait 竖屏
     * @param reversePortrait 反向竖屏
     * @param landscape 横屏
     * @param reverseLandscape 反向横屏
     */
    public void setSupportOrientation(boolean portrait, boolean reversePortrait, boolean landscape,boolean reverseLandscape){
        this.portrait = portrait;
        this.reversePortrait = reversePortrait;
        this.landscape = landscape;
        this.reverseLandscape = reverseLandscape;
        enable();
    }

    /**
     * 打开方向旋转
     */
    public void enable(){
        isEnable = true;
        eventListener.enable();
    }

    /**
     * 关闭方向旋转
     */
    public void disable(){
        isEnable = false;
        eventListener.disable();
    }

    /**
     * 监听“自动旋转"设置
     * 当用户打开该系统设置，屏幕会根据重力感应改变方向
     */
    public void startObserver() {
        contentResolver.registerContentObserver(
                Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION), false, contentObserver);
    }

    /**
     * 取消监听“自动旋转"设置
     */
    public void stopObserver() {
        contentResolver.unregisterContentObserver(contentObserver);
    }

    // 是否竖屏
    public static boolean isPortrait(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    // 是否横屏
    public static boolean isLandscape(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    private OnDegreeChangedListener mOnDegreeChangedListener;
    public void setOnDegreeChangedListener(OnDegreeChangedListener listener){
        mOnDegreeChangedListener = listener;
    }

    /**
     * onDegreeChanged 精确角度度数
     * onOrientationChanged 方向改变对应的角度度数
     */
    public interface OnDegreeChangedListener{
        void onDegreeChanged(int exactDegree);
    }
}
