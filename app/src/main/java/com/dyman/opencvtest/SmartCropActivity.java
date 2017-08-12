package com.dyman.opencvtest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.dyman.opencvtest.utils.Scanner;

import org.opencv.core.Point;

public class SmartCropActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = SmartCropActivity.class.getSimpleName();

    private Button scanBtn;
    private Button cropBtn;
    private ImageView showIv;

    private Bitmap bitmap;
    private Scanner mScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_crop);

        initView();
    }



    private void initView() {

        scanBtn = (Button) findViewById(R.id.scan_btn);
        cropBtn = (Button) findViewById(R.id.crop_btn);
        showIv = (ImageView) findViewById(R.id.showImage_iv);

        scanBtn.setOnClickListener(this);
        cropBtn.setOnClickListener(this);

        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.test_card);
        showIv.setImageBitmap(bitmap);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.scan_btn:
                Toast.makeText(SmartCropActivity.this, "扫描选区", Toast.LENGTH_SHORT).show();
                break;

            case R.id.crop_btn:
                Toast.makeText(SmartCropActivity.this, "截取选区（待完善）", Toast.LENGTH_SHORT).show();
                break;
        }
    }






}
