package com.dyman.opencvtest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.dyman.opencvtest.utils.CvFaceUtils;
import com.dyman.opencvtest.utils.OpenCVUtils;
import com.dyman.opencvtest.utils.Scanner;
import com.dyman.opencvtest.view.CropImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Point;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "MainActivity";

    private Bitmap srcBitmap;
    private Button showSrcBtn;
    private Button showGrayBtn;
    private Button rotateBitmapBtn;
    private Button resizeBitmapBtn;
    private CropImageView cropImageIv;
    private ImageView showImageIv;

    private OpenCVUtils cvUtils;
    private CvFaceUtils cvFaceUtils;
    private Bitmap bitmap;
    private Scanner mScanner;


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
        cvFaceUtils = new CvFaceUtils(this);

        initView();
    }


    private void initView() {
        srcBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.face);
        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.test_card);


        showSrcBtn = (Button) findViewById(R.id.showSrc_btn);
        showGrayBtn = (Button) findViewById(R.id.showGray_btn);
        rotateBitmapBtn = (Button) findViewById(R.id.rotateImage_btn);
        resizeBitmapBtn = (Button) findViewById(R.id.resizeImage_btn);
        showImageIv = (ImageView) findViewById(R.id.showImage_iv);
        cropImageIv = (CropImageView) findViewById(R.id.cropImage_iv);

        showSrcBtn.setOnClickListener(this);
        showGrayBtn.setOnClickListener(this);
        rotateBitmapBtn.setOnClickListener(this);
        resizeBitmapBtn.setOnClickListener(this);

        showImageIv.setImageBitmap(bitmap);
        if (bitmap != null) {
            cropImageIv.setImageToCrop(bitmap);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.showSrc_btn:
                Toast.makeText(MainActivity.this, "扫描选区", Toast.LENGTH_SHORT).show();
                cropImageIv.setImageToCrop(bitmap);
                break;

            case R.id.showGray_btn:
                Toast.makeText(MainActivity.this, "人脸检测", Toast.LENGTH_SHORT).show();
                long time = System.currentTimeMillis();
                Bitmap faceBitmap = cvFaceUtils.faceDetect(srcBitmap);
                Log.i(TAG, "onClick:    人脸检测耗时： "+(System.currentTimeMillis() - time));
                showImageIv.setImageBitmap(faceBitmap);
                break;

            case R.id.rotateImage_btn:
                Toast.makeText(MainActivity.this, "截取选区", Toast.LENGTH_SHORT).show();
                cropImageIv.setImageBitmap(cropImageIv.crop());
                break;

            case  R.id.resizeImage_btn:
                Toast.makeText(MainActivity.this, "边缘检测", Toast.LENGTH_SHORT).show();
                showImageIv.setImageBitmap(cvUtils.simpleCanny(srcBitmap));
                break;
        }
    }
}
