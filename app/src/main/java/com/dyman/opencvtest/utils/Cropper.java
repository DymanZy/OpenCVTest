package com.dyman.opencvtest.utils;

import android.graphics.Bitmap;
import android.graphics.Point;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Created by dyman on 2017/8/13.
 *
 *  截取选区并转换成矩形的帮助类
 */

public class Cropper {

    /**
     *  裁剪图片
     * @param srcBmp   待裁剪的图片
     * @param cropPoints    裁剪区域顶点，顶点坐标以图像大小为准
     * @return  返回裁剪后的图片
     */
    public static Bitmap crop(Bitmap srcBmp, Point[] cropPoints) {
        if (srcBmp == null || cropPoints == null) {
            throw new IllegalArgumentException("srcBmp and cropPoints cannot be null");
        }
        if (cropPoints.length != 4) {
            throw new IllegalArgumentException("The length of cropPoints must be 4, and sort by leftTop, rightTop, rightBottom, leftBottom");
        }

        Mat srcMat = new Mat();
        Utils.bitmapToMat(srcBmp, srcMat);
        Mat dstMat = new Mat();

        Point leftTop = cropPoints[0];
        Point rightTop = cropPoints[1];
        Point rightBottom = cropPoints[2];
        Point leftBottom = cropPoints[3];
        int cropWidth = (int) (getPointsDistance(leftTop, rightTop) + getPointsDistance(leftBottom, rightBottom))/2;
        int cropHeight = (int) (getPointsDistance(leftTop, leftBottom) + getPointsDistance(rightTop, rightBottom))/2;
        Bitmap cropBitmap = Bitmap.createBitmap(cropWidth, cropHeight, Bitmap.Config.ARGB_8888);

        Mat srcTriangleMat = new Mat(4, 1, CvType.CV_32FC2);
        srcTriangleMat.put(0,0,
                leftTop.x, leftTop.y,
                rightTop.x, rightTop.y,
                leftBottom.x, leftBottom.y,
                rightBottom.x, rightBottom.y);
        Mat dstTriangleMat = new Mat(4, 1, CvType.CV_32FC2);
        dstTriangleMat.put(0,0,
                0,0,
                cropWidth,0,
                0, cropHeight,
                cropWidth, cropHeight);
        //  由四对点计算透射变换
        Mat transformMat = Imgproc.getPerspectiveTransform(srcTriangleMat, dstTriangleMat);
        //  对图像进行透射变化
        Imgproc.warpPerspective(srcMat, dstMat, transformMat, new Size(cropWidth, cropHeight));

        Utils.matToBitmap(dstMat, cropBitmap);
        return cropBitmap;
    }


    public static double getPointsDistance(Point p1, Point p2) {
        return getPointsDistance(p1.x, p1.y, p2.x, p2.y);
    }

    public static double getPointsDistance(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }
}
