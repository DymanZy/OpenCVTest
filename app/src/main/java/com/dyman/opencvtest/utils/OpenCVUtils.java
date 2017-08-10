package com.dyman.opencvtest.utils;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Range;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Created by dyman on 2017/8/10.
 *  封装了一些OpenCV的图像处理的方法
 */

public class OpenCVUtils {

    private static final String TAG = OpenCVUtils.class.getSimpleName();

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
            default:
                Log.i(TAG, "rotate: 旋转的度数应该为90度的倍数");
                return null;
        }
        Utils.matToBitmap(dstMat, dstBitmap);
        return dstBitmap;
    }


    /** 图像裁剪 */
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

        Bitmap cropBitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.RGB_565);
        Mat srcMat = new Mat();
        Utils.bitmapToMat(srcBitmap, srcMat);
        Mat cropMat = new Mat(srcMat, new Range(rect.top, rect.bottom), new Range(rect.left, rect.right));
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


    /** 方框滤波 */
    public Bitmap boxFilter(Bitmap srcBitmap, int depth, Size size) {
        Mat srcMat = new Mat();
        Mat dstMat = new Mat();
        Bitmap bitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);

        Utils.bitmapToMat(srcBitmap, srcMat);
        Imgproc.boxFilter(srcMat, dstMat, depth, size);

        Utils.matToBitmap(dstMat, bitmap);

        return bitmap;
    }


    /** 均值滤波 */
    public Bitmap blur(Bitmap srcBitmap, Size size) {
        Mat srcMat = new Mat();
        Mat dstMat = new Mat();
        Bitmap bitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);

        Utils.bitmapToMat(srcBitmap, srcMat);
        Imgproc.blur(srcMat, dstMat, size);

        Utils.matToBitmap(dstMat, bitmap);

        return bitmap;
    }


    /** 高斯滤波 */
    public Bitmap gaussianBlur(Bitmap srcBitmap, Size size) {
        Mat srcMat = new Mat();
        Mat dstMat = new Mat();
        Bitmap bitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);

        Utils.bitmapToMat(srcBitmap, srcMat);
        Imgproc.GaussianBlur(srcMat, dstMat, size, 0);
        Utils.matToBitmap(dstMat, bitmap);

        return bitmap;
    }

}
