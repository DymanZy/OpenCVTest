package com.dyman.opencvtest.module;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.Image;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dyman.opencvtest.R;
import com.dyman.opencvtest.utils.CvFaceUtils;
import com.dyman.opencvtest.utils.OpenCVUtils;
import com.dyman.opencvtest.utils.ScreenUtil;
import com.dyman.opencvtest.view.FaceOverlayView;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.List;

public class FaceRecognizeActivity extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback{

    private static final String TAG = "CFDActivity";
    private static final int DRAW_FRAME = 100;

    private SurfaceView mSurfaceView;
    private FaceOverlayView mOverlay;
    private Button cropFaceBtn;
    private ImageView faceOneIv;
    private ImageView faceTwoIv;
    private TextView confidenceTv;

    private Camera mCamera; // 摄像头
    private int previewWidth, previewHeight;
    private int mDisplayOrientation, mDisplayRotation;
    private Bitmap fullBitmap;

    private CvFaceUtils mCvFaceUtils;
    private Rect[] faceRects;
    private Bitmap faceCropOne = null;
    private Bitmap faceCropTwo = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ScreenUtil.setFullScreen(this);//全屏
        ScreenUtil.keepScreenLight(this);
        ScreenUtil.hideNavigation(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_recognize);
        initView();
        initData();
    }


    private void initView() {
        mSurfaceView = findViewById(R.id.surfaceView_sv);
        mOverlay = findViewById(R.id.faceOverlay_fov);
        cropFaceBtn = findViewById(R.id.cropFace_btn);
        faceOneIv = findViewById(R.id.faceOne_iv);
        faceTwoIv = findViewById(R.id.faceTwo_iv);
        confidenceTv = findViewById(R.id.confidence_tv);

        cropFaceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fullBitmap == null || faceRects.length == 0) {
                    return;
                }

                //  截图人脸并保存
                if (faceCropOne == null) {
                    Log.e(TAG, "onClick: ------------ crop face one");
                    faceCropOne = OpenCVUtils.crop(fullBitmap, faceRects[0]);
                    faceOneIv.setImageBitmap(faceCropOne);
                    CvFaceUtils.saveImage(FaceRecognizeActivity.this, faceCropOne, "faceCropOne");
                } else if (faceCropTwo == null) {
                    Log.e(TAG, "onClick: ------------ crop face two");
                    faceCropTwo = OpenCVUtils.crop(fullBitmap, faceRects[0]);
                    faceTwoIv.setImageBitmap(faceCropTwo);
                    CvFaceUtils.saveImage(FaceRecognizeActivity.this, faceCropTwo, "faceCropTwo");

                    double result = CvFaceUtils.compare(FaceRecognizeActivity.this, "faceCropOne", "faceCropTwo");
                    Log.e(TAG, "onClick: ------------ confidence = " + result);
                    DecimalFormat df = new DecimalFormat("#.00");
                    confidenceTv.setText("相似度：" + df.format(result));

                } else {
                    Log.e(TAG, "onClick: ------------ reset");
                    faceCropOne = null;
                    faceCropTwo = null;
                }
            }
        });
    }


    private void initData() {
        mCvFaceUtils = new CvFaceUtils(FaceRecognizeActivity.this);
    }


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.addCallback(this);
        holder.setFormat(ImageFormat.NV21);
        holder.setType(SurfaceHolder.SURFACE_TYPE_HARDWARE);
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try{
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            if (mCamera == null) {
                mCamera = Camera.open(0);
            }
            Camera.getCameraInfo(0, cameraInfo);

            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mOverlay.setFront(true);
            }

            mCamera.setPreviewDisplay(mSurfaceView.getHolder());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int width, int height) {

        mOverlay.setPreviewWidth(1080);
        mOverlay.setPreviewHeight(1920);

        if (mOverlay != null) {
            if (isScreenOrientationPortrait(FaceRecognizeActivity.this)) {
                mOverlay.setDisplayOrientation(0);
            } else {
                mOverlay.setDisplayOrientation(90);
            }
        }

        try {
            if (surfaceHolder.getSurface() == null && mCamera == null) {
                return;
            }
            mCamera.stopPreview();
            configureCamera(width, height);
            setDisplayOrientation();
            startPreView();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (mCamera != null) {
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.setErrorCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }


    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        int rotate = mDisplayOrientation + mDisplayRotation;
        float zoomScale = 0.2f;

        fullBitmap = OpenCVUtils.yuv2Bitmap(bytes, previewWidth, previewHeight, rotate);
        Bitmap resizeBmp = OpenCVUtils.resize(fullBitmap, zoomScale);
        faceRects = mCvFaceUtils.faceDetect(resizeBmp, 1);

        for (Rect rect : faceRects) {
            int x = (int) (rect.centerX() / zoomScale);
            int y = (int) (rect.centerY() / zoomScale);
            int width = (int) (rect.width() / zoomScale);
            int height = (int) (rect.height() / zoomScale);

            rect.set(x - width/2,
                    y - height/2,
                    x + width/2,
                    y + height/2);
        }

        mHandler.sendEmptyMessage(DRAW_FRAME);
    }


    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case DRAW_FRAME:
                    if (faceRects != null){
                        mOverlay.setFaces(faceRects);
                    }
                    break;
            }
        }
    };


    private void startPreView() {
        if (mCamera != null) {
            mCamera.startPreview();
            mCamera.setPreviewCallback(this);
        }
    }


    public static boolean isScreenOrientationPortrait(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }


    /** 设置摄像机参数，显示大小、自动对焦等等 */
    private void configureCamera(int width, int height) {
        Camera.Parameters parameters = mCamera.getParameters();

        //  设置相机支持的预览画面的宽高
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        Camera.Size size = getCloselyPreSize(width, height, previewSizes);
        parameters.setPreviewSize(size.width, size.height);
        previewWidth = size.width;
        previewHeight = size.height;

        //  设置相机自动调焦
        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        mCamera.setParameters(parameters);
    }


    private Camera.Size getCloselyPreSize(int surfaceWidth, int surfaceHeight, List<Camera.Size> previewSizeList) {

        int ReqTmpWidth;
        int ReqTmpHeight;
        // 当屏幕为垂直的时候需要把宽高值进行调换，保证宽大于高
        if (isScreenOrientationPortrait(FaceRecognizeActivity.this)) {
            ReqTmpWidth = surfaceHeight;
            ReqTmpHeight = surfaceWidth;
        } else {
            ReqTmpWidth = surfaceWidth;
            ReqTmpHeight = surfaceHeight;
        }
        //先查找preview中是否存在与surfaceview相同宽高的尺寸
        for (Camera.Size size : previewSizeList) {
            if ((size.width == ReqTmpWidth) && (size.height == ReqTmpHeight)) {
                return size;
            }
        }

        // 得到与传入的宽高比最接近的size
        float reqRatio = ((float) ReqTmpWidth) / ReqTmpHeight;
        float curRatio, deltaRatio;
        float deltaRatioMin = Float.MAX_VALUE;
        Camera.Size retSize = null;
        for (Camera.Size size : previewSizeList) {
            curRatio = ((float) size.width) / size.height;
            deltaRatio = Math.abs(reqRatio - curRatio);
            if (deltaRatio < deltaRatioMin) {
                deltaRatioMin = deltaRatio;
                retSize = size;
            }
        }

        return retSize;
    }


    /** 设置摄像机画面的显示方向 */
    private void setDisplayOrientation() {
        mDisplayRotation = ScreenUtil.getDisplayRotation(FaceRecognizeActivity.this);
        mDisplayOrientation = ScreenUtil.getDisplayOrientation(mDisplayRotation, 0);

        mCamera.setDisplayOrientation(mDisplayOrientation);
    }
}
