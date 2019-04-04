package com.lg.utilsdemo.activity;

import android.animation.ObjectAnimator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.lg.utilsdemo.R;
import com.lg.utilsdemo.util.SensorHelper;

public class SensorActivity extends AppCompatActivity {

    private ImageView icIcon;
    private SensorHelper sensorHelper;
    private int lastDegree = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        icIcon = (ImageView) findViewById(R.id.iv_icon);
        sensorHelper = SensorHelper.newInstance(this);
        sensorHelper.setSupportOrientation(false,false,false,false);
        sensorHelper.setOnDegreeChangedListener(new SensorHelper.OnDegreeChangedListener() {
            @Override
            public void onDegreeChanged(int exactDegree) {
                Log.i("lg","exactDegree:" + exactDegree);
                int difDegree = exactDegree - lastDegree;
                if(difDegree >= -180 && difDegree <= 180){
                    ObjectAnimator.ofFloat(icIcon, "rotation", exactDegree).start();
                }else{
                    ObjectAnimator.ofFloat(icIcon, "rotation", difDegree - 360).start();
                }
                lastDegree = exactDegree;
            }
        });
        //打开监听
//        sensorHelper.startObserver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //使重力感应可用
        sensorHelper.enable();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //使重力感应不可用
        sensorHelper.disable();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //关闭监听
//        sensorHelper.stopObserver();
    }
}
