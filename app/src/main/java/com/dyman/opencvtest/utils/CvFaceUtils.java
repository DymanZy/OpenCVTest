package com.dyman.opencvtest.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.dyman.opencvtest.R;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.CvHistogram;

import static org.bytedeco.javacpp.helper.opencv_imgproc.cvCalcHist;
import static org.bytedeco.javacpp.opencv_core.CV_HIST_ARRAY;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;
import static org.bytedeco.javacpp.opencv_imgproc.CV_COMP_CORREL;
import static org.bytedeco.javacpp.opencv_imgproc.CV_COMP_INTERSECT;
import static org.bytedeco.javacpp.opencv_imgproc.cvCompareHist;
import static org.bytedeco.javacpp.opencv_imgproc.cvNormalizeHist;

import static org.opencv.highgui.Highgui.CV_LOAD_IMAGE_GRAYSCALE;

/**
 * Created by dyman on 2017/8/10.
 *
 *  基于OpenCV实现的人脸检测帮助类
 */

public class CvFaceUtils {
    private static final String TAG = "CvFaceUtils";

    private Context mContext;
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);

    private File mCascadeFile;
    private CascadeClassifier ccf;


    /** 初始化人脸检测帮助类，加载训练集合 */
    public CvFaceUtils(Context context) {
        this.mContext = context;

        try {
            InputStream is = mContext.getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = mContext.getDir("cascade", Context.MODE_PRIVATE);
            mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            ccf = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            if (ccf.empty()) {
                Log.e(TAG, "Failed to load cascade classifier");
                ccf = null;
            } else
                Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

            cascadeDir.delete();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /** 对传入的图片进行人脸检测 */
    public Bitmap faceDetect(Bitmap srcBitmap) {
        if (ccf == null) {
            throw new IllegalArgumentException(" CascadeClassifier can not be null! Make sure your cascadeFile is available. ");
        }

        Mat srcMat = new Mat();
        Mat grayMat = new Mat();
        MatOfRect faces = new MatOfRect();
        Bitmap bitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);

        Utils.bitmapToMat(srcBitmap, srcMat);

        Imgproc.cvtColor(srcMat, grayMat, Imgproc.COLOR_BGR2GRAY);  //灰度化处理
        Imgproc.equalizeHist(grayMat, grayMat); //直方图均衡化，增强图像的对比度
        ccf.detectMultiScale(grayMat, faces);   //人脸检测

        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++) {
            //绘制人脸画框
            Core.rectangle(srcMat, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
        }

        Utils.matToBitmap(srcMat, bitmap);

        return bitmap;
    }


    /** 对传入的图片进行人脸检测 */
    public android.graphics.Rect[] faceDetect(Bitmap srcBitmap, int maxFaceNum) {
        return faceDetect(srcBitmap, maxFaceNum, false);
    }


    public android.graphics.Rect[] faceDetect(Bitmap srcBitmap, int maxFaceNum, boolean specificDetection) {
        if (ccf == null) {
            throw new IllegalArgumentException(" CascadeClassifier can not be null! Make sure your cascadeFile is available. ");
        }

        Bitmap partialBmp = srcBitmap;
        int fixedHeight = 0;
        //  抛弃图片顶部和底部，只检测中间区域，提高检测速度
        if (specificDetection) {
            android.graphics.Rect rect = new android.graphics.Rect();
            fixedHeight = srcBitmap.getHeight() / 5;
            rect.left = 0;
            rect.top = srcBitmap.getHeight() / 5;
            rect.right = srcBitmap.getWidth();
            rect.bottom = srcBitmap.getHeight() / 5 * 4;
            partialBmp = OpenCVUtils.crop(partialBmp, srcBitmap, rect);
        }

        android.graphics.Rect[] rects;
        Mat srcMat = new Mat();
        Mat grayMat = new Mat();
        MatOfRect faces = new MatOfRect();
        Utils.bitmapToMat(partialBmp, srcMat);

        Imgproc.cvtColor(srcMat, grayMat, Imgproc.COLOR_BGR2GRAY);  //灰度化处理
        Imgproc.equalizeHist(grayMat, grayMat); //直方图均衡化，增强图像的对比度
        ccf.detectMultiScale(grayMat, faces);   //人脸检测

        Rect[] facesArray = faces.toArray();
        facesArray = faceSortByArea(facesArray);

        maxFaceNum = maxFaceNum < facesArray.length ? maxFaceNum : facesArray.length;
        rects = new android.graphics.Rect[maxFaceNum];
        for (int i = 0; i < maxFaceNum; i++) {
            int left = (int) facesArray[i].tl().x;
            int top = (int) facesArray[i].tl().y + fixedHeight;
            int right = (int) facesArray[i].br().x;
            int bottom = (int) facesArray[i].br().y + fixedHeight;

            android.graphics.Rect rect = new android.graphics.Rect(left, top, right, bottom);
            rects[i] = rect;
        }

        if (partialBmp != null) {
            partialBmp.recycle();
        }
        return rects;
    }


    /** 按面积排序，从大到小 */
    private Rect[] faceSortByArea(Rect[] faces) {
        boolean hasChange = true;
        for (int i = 0; i < faces.length && hasChange; i++) {
            hasChange = false;
            for (int j = faces.length - 2; j >= i ; j--) {
                if (faces[j].area() < faces[j+1].area()) {
                    hasChange = true;
                    Rect temp = faces[j];
                    faces[j] = faces[j+1];
                    faces[j+1] = temp;
                }
            }
        }
        return faces;
    }


    public static boolean saveImage(Context context, Bitmap image, String fileName) {
        Mat srcMat = new Mat();
        Utils.bitmapToMat(image, srcMat);
        Mat grayMat = new Mat();
        Imgproc.cvtColor(srcMat, grayMat, Imgproc.COLOR_BGR2GRAY);
        Mat mat = new Mat();
        Size size = new Size(100, 100);
        Imgproc.resize(grayMat, mat, size);
        return Highgui.imwrite(getFilePath(context, fileName), mat);
    }


    public static double compare(Context context, String fileName1, String fileName2) {

        try {
            String pathFile1 = getFilePath(context, fileName1);
            String pathFile2 = getFilePath(context, fileName2);
            IplImage image1 = cvLoadImage(pathFile1, opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
            IplImage image2 = cvLoadImage(pathFile2, opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
            if (null == image1 || null == image2) {
                return -1;
            }

            int l_bins = 256;
            int hist_size[] = {l_bins};
            float v_ranges[] = {0, 255};
            float ranges[][] = {v_ranges};

            opencv_core.IplImage imageArr1[] = {image1};
            opencv_core.IplImage imageArr2[] = {image2};
            CvHistogram histogram1 = CvHistogram.create(1, hist_size, opencv_core.CV_HIST_ARRAY, ranges, 1);
            CvHistogram histogram2 = CvHistogram.create(1, hist_size, opencv_core.CV_HIST_ARRAY, ranges, 1);
            cvCalcHist(imageArr1, histogram1, 0, null);
            cvCalcHist(imageArr2, histogram2, 0, null);
            cvNormalizeHist(histogram1, 100.0);
            cvNormalizeHist(histogram2, 100.0);

            double c1 = cvCompareHist(histogram1, histogram2, CV_COMP_CORREL) * 100;
            double c2 = cvCompareHist(histogram1, histogram2, CV_COMP_INTERSECT);

            return (c1+c2)/2;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }


    private static String getFilePath(Context context, String fileName) {
        if ("".equals(fileName) || fileName == null) {
            return null;
        }

        return context.getApplicationContext().getFilesDir().getPath() + fileName + ".jpg";
    }
}
