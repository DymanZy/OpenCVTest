package com.dyman.opencvtest.module;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.dyman.opencvtest.R;
import com.dyman.opencvtest.utils.CvFaceUtils;
import com.dyman.opencvtest.utils.OpenCVUtils;
import com.dyman.opencvtest.utils.ScreenUtil;
import com.dyman.opencvtest.view.FaceOverlayView;

import java.util.List;

public class CameraFaceDetectActivity extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback{
    private static final String TAG = "CFDActivity";
    private static final int DRAW_FRAME = 100;

    private SurfaceView mSurfaceView;
    private FaceOverlayView mOverlay;

    private Camera mCamera; // 摄像头
    private int previewWidth, previewHeight;
    private int mDisplayOrientation, mDisplayRotation;
    private Bitmap fullBitmap;

    private CvFaceUtils mCvFaceUtils;
    private Rect[] faceRects;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ScreenUtil.setFullScreen(this);//全屏
        ScreenUtil.keepScreenLight(this);
        ScreenUtil.hideNavigation(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detect);
        initView();
        initData();
    }


    private void initView() {
        mSurfaceView = findViewById(R.id.surfaceView_sv);
        mOverlay = findViewById(R.id.faceOverlay_fov);
    }


    private void initData() {
        mCvFaceUtils = new CvFaceUtils(CameraFaceDetectActivity.this);
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
            if (isScreenOrientationPortrait(CameraFaceDetectActivity.this)) {
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

        long time = System.currentTimeMillis();
        fullBitmap = OpenCVUtils.yuv2Bitmap(bytes, previewWidth, previewHeight, rotate);
        Log.i(TAG, "onPreviewFrame: --------------------------- yuv2Bitmap, 耗时：" + (System.currentTimeMillis() - time));

        time = System.currentTimeMillis();
        Bitmap resizeBmp = OpenCVUtils.resize(fullBitmap, zoomScale);
        Log.i(TAG, "onPreviewFrame: --------------------------- resize, 耗时：" + (System.currentTimeMillis() - time));

        time = System.currentTimeMillis();
        faceRects = mCvFaceUtils.faceDetect(resizeBmp, 99);
        Log.i(TAG, "onPreviewFrame: --------------------------- getFaceRect, 耗时：" + (System.currentTimeMillis() - time));

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
        if (isScreenOrientationPortrait(CameraFaceDetectActivity.this)) {
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
        mDisplayRotation = ScreenUtil.getDisplayRotation(CameraFaceDetectActivity.this);
        mDisplayOrientation = ScreenUtil.getDisplayOrientation(mDisplayRotation, 0);

        mCamera.setDisplayOrientation(mDisplayOrientation);
    }
}
