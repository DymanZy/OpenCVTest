package com.dyman.opencvtest;

import android.app.Application;
import android.util.Log;

import org.opencv.android.OpenCVLoader;

/**
 * Created by dyman on 2017/8/13.
 */

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        //  init load OpenCV
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "OpenCV not loaded");
        } else {
            Log.e(TAG, "OpenCV loaded！");
        }
    }
}
