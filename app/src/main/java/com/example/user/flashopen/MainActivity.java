package com.example.user.flashopen;

import android.content.Context;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TimePicker;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {


    private CameraManager manager;// 声明CameraManager对象
    private Camera m_Camera = null;// 声明Camera对象
    private int countDownTimer = 0;
    private CheckBox cb_ctl;
    private TimePicker timePicker;
    private Switch switch1;
    private Switch switch2;

    Timer timer;
    TimerTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cb_ctl = findViewById(R.id.cb_ctl);
        timePicker = findViewById(R.id.timePicker);
        switch1 = findViewById(R.id.switch1);
        switch2 = findViewById(R.id.switch2);

        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(0);
        timePicker.setCurrentMinute(0);


        manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] camerList = manager.getCameraIdList();
            for (String str : camerList) {
            }
        } catch (CameraAccessException e) {
            Log.e("error", e.getMessage());
        }


        cb_ctl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    timer = new Timer(false);
                    timePicker.setVisibility(View.INVISIBLE);
                    switch2.setVisibility(View.INVISIBLE);
                    lightSwitch(false);

                    countDownTimer = timePicker.getCurrentHour() * 60 + timePicker.getCurrentMinute();
                    if (countDownTimer > 0) {
                        task = new TimerTask() {
                            @Override
                            public void run() {
                                handler.sendEmptyMessage(1);
                            }
                        };
                        int unit = 1;
                        if (switch2.isChecked()) {
                            unit = 60;
                        }
                        timer.scheduleAtFixedRate(task, 0, 1000 * unit);
                    }
                } else {
                    countDownTimer = 0;

                    timePicker.setCurrentHour(0);
                    timePicker.setCurrentMinute(0);
                    timePicker.setVisibility(View.VISIBLE);
                    switch2.setVisibility(View.VISIBLE);

                    lightSwitch(true);
                    if (task != null) {
                        task.cancel();
                    }
                    if (timer != null) {

                        timer.cancel();
                        timer.purge();
                    }

                    if (switch1.isChecked()) {
                        MainActivity.this.finish();
                    }
                }
            }
        });


    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (countDownTimer > 0) {
                countDownTimer--;
            } else {
                cb_ctl.setChecked(false);
            }
        }
    };

    /**
     * 手电筒控制方法
     *
     * @param lightStatus
     * @return
     */
    private void lightSwitch(final boolean lightStatus) {
        if (lightStatus) { // 关闭手电筒
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    manager.setTorchMode("0", false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                if (m_Camera != null) {
                    m_Camera.stopPreview();
                    m_Camera.release();
                    m_Camera = null;
                }
            }
        } else { // 打开手电筒
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    manager.setTorchMode("0", true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                final PackageManager pm = getPackageManager();
                final FeatureInfo[] features = pm.getSystemAvailableFeatures();
                for (final FeatureInfo f : features) {
                    if (PackageManager.FEATURE_CAMERA_FLASH.equals(f.name)) { // 判断设备是否支持闪光灯
                        if (null == m_Camera) {
                            m_Camera = Camera.open();
                        }
                        final Camera.Parameters parameters = m_Camera.getParameters();
                        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        m_Camera.setParameters(parameters);
                        m_Camera.startPreview();
                    }
                }
            }
        }
    }


    /**
     * 判断Android系统版本是否 >= M(API23)
     */
    private boolean isM() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return true;
        } else {
            return false;
        }
    }
}
