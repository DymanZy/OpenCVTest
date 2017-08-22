package com.dyman.opencvtest.module;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.dyman.opencvtest.R;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "MainActivity";

    private Button scanCropBtn;
    private Button cameraFaceDetectBtn;
    private Button albumFaceDetectBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }


    private void initView() {
        scanCropBtn = (Button) findViewById(R.id.scanCrop_btn);
        cameraFaceDetectBtn = (Button) findViewById(R.id.faceDetect_camera_btn);
        albumFaceDetectBtn = (Button) findViewById(R.id.faceDetect_album_btn);

        scanCropBtn.setOnClickListener(this);
        cameraFaceDetectBtn.setOnClickListener(this);
        albumFaceDetectBtn.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.scanCrop_btn:
                startActivity(new Intent(MainActivity.this, SmartCropActivity.class));
                break;
            case R.id.faceDetect_camera_btn:
                startActivity(new Intent(MainActivity.this, CameraFaceDetectActivity.class));
                break;
            case R.id.faceDetect_album_btn:
                startActivity(new Intent(MainActivity.this, AlbumFaceDetectActivity.class));
                break;
        }
    }

}
