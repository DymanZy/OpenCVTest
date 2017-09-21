package com.dyman.opencvtest.module;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.dyman.opencvtest.R;


public class MainActivity extends Activity implements View.OnClickListener{
    private static final String TAG = "MainActivity";

    private Button scanCropBtn;
    private Button cameraFaceDetectBtn;
    private Button albumFaceDetectBtn;
    private Button faceRecognizeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }


    private void initView() {
        scanCropBtn = findViewById(R.id.scanCrop_btn);
        cameraFaceDetectBtn = findViewById(R.id.faceDetect_camera_btn);
        albumFaceDetectBtn = findViewById(R.id.faceDetect_album_btn);
        faceRecognizeBtn = findViewById(R.id.faceRecognize_btn);

        scanCropBtn.setOnClickListener(this);
        cameraFaceDetectBtn.setOnClickListener(this);
        albumFaceDetectBtn.setOnClickListener(this);
        faceRecognizeBtn.setOnClickListener(this);
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
            case R.id.faceRecognize_btn:
                startActivity(new Intent(MainActivity.this, FaceRecognizeActivity.class));
                break;
        }
    }

}
