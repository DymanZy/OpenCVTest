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

import com.dyman.opencvtest.utils.OpenCVUtils;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "MainActivity";

    private Bitmap srcBitmap;
    private Button showSrcBtn;
    private Button showGrayBtn;
    private Button rotateBitmapBtn;
    private Button resizeBitmapBtn;
    private ImageView showImageIv;

    private OpenCVUtils cvUtils;


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

        cvUtils = new OpenCVUtils();

        initView();
    }


    private void initView() {
        srcBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.logo);

        showSrcBtn = (Button) findViewById(R.id.showSrc_btn);
        showGrayBtn = (Button) findViewById(R.id.showGray_btn);
        rotateBitmapBtn = (Button) findViewById(R.id.rotateImage_btn);
        resizeBitmapBtn = (Button) findViewById(R.id.resizeImage_btn);
        showImageIv = (ImageView) findViewById(R.id.showImage_iv);

        showSrcBtn.setOnClickListener(this);
        showGrayBtn.setOnClickListener(this);
        rotateBitmapBtn.setOnClickListener(this);
        resizeBitmapBtn.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.showSrc_btn:
                Toast.makeText(MainActivity.this, "显示原图", Toast.LENGTH_SHORT).show();
                showImageIv.setImageBitmap(srcBitmap);
                break;

            case R.id.showGray_btn:
                Toast.makeText(MainActivity.this, "显示灰度图", Toast.LENGTH_SHORT).show();
                showImageIv.setImageBitmap(cvUtils.gray(srcBitmap));
                break;

            case R.id.rotateImage_btn:
                Toast.makeText(MainActivity.this, "图像旋转90度", Toast.LENGTH_SHORT).show();
                showImageIv.setImageBitmap(cvUtils.rotate(srcBitmap, 90));
                break;

            case  R.id.resizeImage_btn:
                Toast.makeText(MainActivity.this, "图像缩放", Toast.LENGTH_SHORT).show();
                showImageIv.setImageBitmap(cvUtils.resize(srcBitmap, 0.1f));
                break;
        }
    }
}
