package com.dyman.opencvtest.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Range;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
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
    public static Bitmap crop(Bitmap srcBitmap, Rect rect) {
        return crop(null, srcBitmap, rect);
    }


    /** 图像裁剪(复用cropBitmap, 节省内存) */
    public static Bitmap crop(Bitmap cropBitmap, Bitmap srcBitmap, Rect rect) {
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

        if (cropBitmap == null || cropBitmap.getWidth() != rect.width() || cropBitmap.getHeight() != rect.height()) {
            cropBitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.RGB_565);
        }

        Mat srcMat = new Mat();
        Utils.bitmapToMat(srcBitmap, srcMat);
        Mat cropMat = new Mat(srcMat, new Range(rect.top, rect.bottom), new Range(rect.left, rect.right));
        Utils.matToBitmap(cropMat, cropBitmap);

        return cropBitmap;
    }


    /** 图像缩放 */
    public static Bitmap resize(Bitmap srcBitmap, float zoomScale) {

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


    /** 简单的Canny边缘检测 */
    public Bitmap simpleCanny(Bitmap srcBitmap) {
        Mat srcMat = new Mat();
        Mat dstMat = new Mat();
        Bitmap bitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);

        Utils.bitmapToMat(srcBitmap, srcMat);
        Imgproc.Canny(srcMat, dstMat, 100, 100);
        Utils.matToBitmap(dstMat, bitmap);

        return bitmap;
    }

    /** 高级的Canny边缘检测 */
    public Bitmap advantagedCanny(Bitmap srcBitmap) {
        Mat srcMat = new Mat();
        Mat edgeMat = new Mat();
        Mat grayMat = new Mat();
        Bitmap bitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);

        Utils.bitmapToMat(srcBitmap, srcMat);
        Imgproc.cvtColor(srcMat, grayMat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.blur(grayMat, edgeMat, new Size(3,3));
        Imgproc.Canny(srcMat, edgeMat, 100, 200);
        Utils.matToBitmap(edgeMat, bitmap);

        return bitmap;
    }


    public static Bitmap yuv2Bitmap(byte[] yuvData, int width, int height, int rotate) {
        Mat yuvMat = new Mat((int) (height * 1.5), width, CvType.CV_8UC1);
        Mat bgrMat = new Mat(height, width, CvType.CV_8UC3);
        yuvMat.put(0, 0, yuvData);

        Imgproc.cvtColor(yuvMat, bgrMat, Imgproc.COLOR_YUV2RGB_NV21);

        Mat dstMat = rotate(bgrMat, rotate);

        Bitmap bitmap = Bitmap.createBitmap(dstMat.cols(), dstMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dstMat, bitmap);
        return bitmap;
    }


    private static Mat rotate(Mat srcMat, int rotate) {
        Mat dstMat;
        switch (rotate) {
            case 0:
                dstMat = new Mat(srcMat.rows(), srcMat.cols(), CvType.CV_8UC3);
                dstMat = srcMat;
                break;

            case 90:
                dstMat = new Mat(srcMat.cols(), srcMat.rows(), CvType.CV_8UC3);
                Core.transpose(srcMat, dstMat);
                Core.flip(dstMat, dstMat, 1);   //  手机后置摄像头需要再镜像翻转一次
                break;

            case 180:
                dstMat = new Mat(srcMat.rows(), srcMat.cols(), CvType.CV_8UC3);
                Core.flip(srcMat, dstMat, -1);
                break;

            case 270:
                dstMat = new Mat(srcMat.cols(), srcMat.rows(), CvType.CV_8UC3);
                Core.transpose(srcMat, dstMat);
                Core.flip(srcMat, dstMat, 1);
                break;

            default:
                throw new IllegalArgumentException("rotate: 旋转的度数应该为90度的倍数");
        }

        return dstMat;
    }

}
