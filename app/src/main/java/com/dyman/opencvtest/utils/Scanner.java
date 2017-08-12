package com.dyman.opencvtest.utils;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * Created by dyman on 2017/8/12.
 *
 *  截图智能选区帮助类
 */

public class Scanner {

    private static final String TAG = "Scanner";


    private int resizeThreshold = 500;
    private Mat srcMat;
    private float resizeScale = 1.0f;

    public Scanner(Bitmap srcBitmap) {
        srcMat = new Mat();
        Utils.bitmapToMat(srcBitmap, srcMat);
    }


    public android.graphics.Point[] scanPoint() {
        //  图像缩放
        Mat image = resizeImage(srcMat);
        //  图像预处理
        Mat scanImage = preProcessImage(image);

        List<MatOfPoint> contours = new ArrayList<>();  //  检测到的轮廓
        Mat hierarchy = new Mat();  //  各轮廓的继承关系
        //  提取边框
        Imgproc.findContours(scanImage, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

        //  按面积排序
        Collections.sort(contours, new Comparator<MatOfPoint>() {
            @Override
            public int compare(MatOfPoint matOfPoint1, MatOfPoint matOfPoint2) {
                double oneArea = Math.abs(Imgproc.contourArea(matOfPoint1));
                double twoArea = Math.abs(Imgproc.contourArea(matOfPoint2));
                return Double.compare(twoArea, oneArea);
            }
        });


        Point[] resultArr = new Point[4];
        if (contours.size() > 0) {
            //  取面积最大的
            MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(0).toArray());
            double arc = Imgproc.arcLength(contour2f, true);

            MatOfPoint2f outDpMat = new MatOfPoint2f();
            Imgproc.approxPolyDP(contour2f, outDpMat, 0.02 * arc, true);    //  多边形逼近
            //  筛选去除相近的点
            MatOfPoint2f selectMat = selectPoint(outDpMat, 1);

            //test
            Point[] points = selectMat.toArray();
            for (Point p : points) {
                Log.i(TAG, "sortPointClockwise: ------------待处理的点： x="+p.x+"    y="+p.y);
            }

            if (selectMat.toArray().length != 4) {
                RotatedRect rotatedRect = Imgproc.minAreaRect(selectMat);
                Point[] p = new Point[4];
                rotatedRect.points(p);
                resultArr = p;
            } else {
                resultArr = selectMat.toArray();
            }

            for (Point p : resultArr) {
                p.x *= resizeScale;
                p.y *= resizeScale;
            }

            for (Point p : resultArr) {
                Log.i(TAG, "sortPointClockwise: ------------缩放还原处理后的点： x="+p.x+"    y="+p.y);
            }
        }
        Point[] result = sortPointClockwise(resultArr);
        android.graphics.Point[] rs = new android.graphics.Point[result.length];
        for (int i = 0; i < result.length; i++) {
            android.graphics.Point p = new android.graphics.Point((int) result[i].x, (int) result[i].y);
            rs[i] = p;
        }
        return rs;
    }


    private Mat resizeImage(Mat image) {
        int width = image.cols();
        int height = image.rows();
        Log.i(TAG, "resizeImage: ------------srcWidth="+width+"     srcHeight="+height);
        int maxSize = width > height ? width : height;
        if (maxSize > resizeThreshold) {
            resizeScale = 1.0f * maxSize / resizeThreshold;
            width = (int) (width / resizeScale);
            height = (int) (height / resizeScale);
            Log.i(TAG, "resizeImage: ------------resizeWidth="+width+"     resizeHeight="+height);
            Size size = new Size(width, height);
            Mat resizeMat = new Mat();
            Imgproc.resize(image, resizeMat, size);
            return resizeMat;
        }
        return image;
    }


    private Mat preProcessImage(Mat image) {

        Mat grayMat = new Mat();
        Imgproc.cvtColor(image, grayMat, Imgproc.COLOR_BGR2GRAY);

        Mat blurMat = new Mat();
        Imgproc.GaussianBlur(grayMat, blurMat, new Size(5,5), 0);

        Mat cannyMat = new Mat();
        Imgproc.Canny(blurMat, cannyMat, 0, 5);

        Mat thresholdMat = new Mat();
        Imgproc.threshold(cannyMat, thresholdMat, 0, 255, Imgproc.THRESH_OTSU);

        return cannyMat;
    }


    private MatOfPoint2f selectPoint(MatOfPoint2f outDpMat, int selectTimes) {
        List<Point> pointList = new ArrayList<>();
        pointList.addAll(outDpMat.toList());
        if (pointList.size() > 4) {
            double arc = Imgproc.arcLength(outDpMat, true);
            for (int i = pointList.size() - 1; i >= 0; i--) {
                if (pointList.size() == 4) {
                    Point[] resultPoints = new Point[pointList.size()];
                    for (int j = 0; j < pointList.size(); j++) {
                        resultPoints[j] = pointList.get(j);
                    }
                    return new MatOfPoint2f(resultPoints);
                }

                if (i != pointList.size() - 1) {
                    Point itor = pointList.get(i);
                    Point lastP = pointList.get(i + 1);

                    double pointLength = Math.sqrt(Math.pow(itor.x-lastP.x, 2) + Math.pow(itor.y - lastP.y, 2));
                    if (pointLength < arc * 0.01 * selectTimes && pointList.size() > 4) {
                        pointList.remove(i);
                    }
                }
            }

            if (pointList.size() > 4) {
                //  要手动逐个强转
                Point[] againPoints = new Point[pointList.size()];
                for (int i = 0; i < pointList.size(); i++) {
                    againPoints[i] = pointList.get(i);
                }
                return selectPoint(new MatOfPoint2f(againPoints), selectTimes + 1);
            }
        }

        return outDpMat;
    }


    private Point[] sortPointClockwise(Point[] points) {
        if (points.length != 4) {
            return points;
        }

        Point unFoundPoint = new Point();
        Point[] result = {unFoundPoint, unFoundPoint, unFoundPoint, unFoundPoint};

        long minDistance = -1;
        for (Point point : points) {
            long distance = (long) (point.x * point.x + point.y * point.y);
            if (minDistance == -1 || distance < minDistance) {
                result[0] = point;
                minDistance = distance;
            }
        }

        if (result[0] != unFoundPoint) {
            Point leftTop = result[0];
            Point[] p1 = new Point[3];
            int i = 0;
            for (Point point : points) {
                if (point.x == leftTop.x && point.y == leftTop.y)
                    continue;
                p1[i] = point;
                i++;
            }
            if ((pointSideLine(leftTop, p1[0], p1[1]) * pointSideLine(leftTop, p1[0], p1[2])) < 0) {
                result[2] = p1[0];
            } else if ((pointSideLine(leftTop, p1[1], p1[0]) * pointSideLine(leftTop, p1[1], p1[2])) < 0) {
                result[2] = p1[1];
            } else if ((pointSideLine(leftTop, p1[2], p1[0]) * pointSideLine(leftTop, p1[2], p1[1])) < 0) {
                result[2] = p1[2];
            }
        }

        if (result[0] != unFoundPoint && result[2] != unFoundPoint) {
            Point leftTop = result[0];
            Point rightBottom = result[2];
            Point[] p1 = new Point[2];
            int i = 0;
            for (Point point : points) {
                if (point.x == leftTop.x && point.y == leftTop.y)
                    continue;
                if (point.x == rightBottom.x && point.y == rightBottom.y)
                    continue;
                p1[i] = point;
                i++;
            }
            if (pointSideLine(leftTop, rightBottom, p1[0]) > 0) {
                result[1] = p1[0];
                result[3] = p1[1];
            } else {
                result[1] = p1[1];
                result[3] = p1[0];
            }
        }

        if (result[0] != unFoundPoint && result[1] != unFoundPoint && result[2] != unFoundPoint && result[3] != unFoundPoint) {
            return result;
        }

        return points;
    }


    private double pointSideLine(Point lineP1, Point lineP2, Point point) {
        double x1 = lineP1.x;
        double y1 = lineP1.y;
        double x2 = lineP2.x;
        double y2 = lineP2.y;
        double x = point.x;
        double y = point.y;
        return (x - x1)*(y2 - y1) - (y - y1)*(x2 - x1);
    }

}
