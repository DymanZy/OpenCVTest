package com.dyman.opencvtest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private Bitmap srcBitmap;
    private Bitmap grayBitmap;

    private Button showSrcBtn;
    private Button showGrayBtn;
    private ImageView showImageIv;


    static {
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "OpenCV not loaded");
        } else {
            Log.e(TAG, "OpenCV loaded！");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        proSrc2Gray();
    }


    private void initView() {
        showSrcBtn = (Button) findViewById(R.id.showSrc_btn);
        showGrayBtn = (Button) findViewById(R.id.showGray_btn);
        showImageIv = (ImageView) findViewById(R.id.showImage_iv);

        showSrcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "显示原图", Toast.LENGTH_SHORT).show();
                showImageIv.setImageBitmap(srcBitmap);
            }
        });
        showGrayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "显示灰度图", Toast.LENGTH_SHORT).show();
                showImageIv.setImageBitmap(grayBitmap);
            }
        });
    }


    private void proSrc2Gray() {
        Mat rgbMat = new Mat();
        Mat grayMat = new Mat();

        srcBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.logo);
        grayBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);
        Utils.bitmapToMat(srcBitmap, rgbMat);
        Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);
        Utils.matToBitmap(grayMat, grayBitmap);
    }

}
