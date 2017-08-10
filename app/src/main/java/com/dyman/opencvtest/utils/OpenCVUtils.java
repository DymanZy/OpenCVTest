package com.dyman.opencvtest.utils;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.CvKNearest;

/**
 * Created by dyman on 2017/8/10.
 */

public class OpenCVUtils {

    private static final String TAG = OpenCVUtils.class.getSimpleName();

    public OpenCVUtils() {

    }


    /** 图片灰度化处理 */
    public Bitmap gray(Bitmap srcBitmap) {
        Mat rgbMat = new Mat();
        Mat grayMat = new Mat();
        Bitmap grayBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);

        Utils.bitmapToMat(srcBitmap, rgbMat);
        Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);
        Utils.matToBitmap(grayMat, grayBitmap);

        return grayBitmap;
    }


    /** 图像旋转 */
    public Bitmap rotate(Bitmap srcBitmap, int rotate) {
        Mat srcMat = new Mat();
        Mat dstMat = new Mat();

        Bitmap dstBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);

        Utils.bitmapToMat(srcBitmap, srcMat);

        rotate = rotate % 360;
        switch (rotate) {
            case 0:
                dstMat = srcMat;
                break;

            case 90:
                Core.transpose(srcMat, dstMat);
                break;

            case 180:
                Core.flip(srcMat, dstMat, -1);
                break;

            case 270:
                Core.transpose(srcMat, dstMat);
                Core.flip(srcMat, dstMat, 1);
                break;
        }
        Utils.matToBitmap(dstMat, dstBitmap);
        return dstBitmap;
    }


    public Bitmap crop(Bitmap srcBitmap, Rect rect) {

        if (srcBitmap == null || rect == null) {
            Log.e(TAG, "crop:   params is null!!!");
            return null;
        }

        if (rect.left < 0) {
            rect.left = 0;
        }
        if (rect.top < 0) {
            rect.top = 0;
        }
        if (rect.right > srcBitmap.getWidth()) {
            rect.right = srcBitmap.getWidth();
        }
        if (rect.bottom > srcBitmap.getHeight()) {
            rect.bottom = srcBitmap.getHeight();
        }

        Mat srcMat = new Mat();
        Mat cropMat = new Mat();
        Bitmap cropBitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.RGB_565);

        Utils.bitmapToMat(srcBitmap, srcMat);
//        cropMat = srcMat(new Range(rect.top, rect.bottom), new Range(rect.left, rect.right));
        Utils.matToBitmap(cropMat, cropBitmap);

        return cropBitmap;
    }


    /** 图像缩放 */
    public Bitmap resize(Bitmap srcBitmap, float zoomScale) {

        Mat srcMat = new Mat();
        Mat dstMat = new Mat();
        Bitmap bitmap = Bitmap.createBitmap((int) (srcBitmap.getWidth() * zoomScale),
                (int) (srcBitmap.getHeight() * zoomScale), Bitmap.Config.RGB_565);

        Utils.bitmapToMat(srcBitmap, srcMat);
        Size dSize = new Size(srcBitmap.getWidth() * zoomScale, srcBitmap.getHeight() * zoomScale);
        Imgproc.resize(srcMat, dstMat, dSize);
        Utils.matToBitmap(dstMat, bitmap);

        return bitmap;
    }


//    /** yuv转rgb数组， nv21类型(not test) */
//    public byte[] yuv2rgb(byte[] yuv, int width, int height) {
//        byte[] rgb = new byte[width * height * 3];
//
//        Mat yuvMat = new Mat();
//        Mat rgbMat = new Mat();
//
//        yuvMat.put(0, 0, yuv);
//        Imgproc.cvtColor(yuvMat, rgbMat, Imgproc.COLOR_YUV2BGR_NV21);
//
//        rgbMat.get(0, 0, rgb);
//
//        return rgb;
//    }


}
