package com.dyman.opencvtest.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.dyman.opencvtest.R;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by dyman on 2017/8/10.
 *
 *  基于OpenCV实现的人脸识别帮助类
 */

public class CvFaceUtils {
    private static final String TAG = "CvFaceUtils";

    private Context mContext;
    private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);

    private File mCascadeFile;
    private CascadeClassifier ccf;

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

    public Bitmap faceDetect(Bitmap srcBitmap) {
        Mat srcMat = new Mat();
        Mat grayMat = new Mat();
        MatOfRect faces = new MatOfRect();
        Bitmap bitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);


        Utils.bitmapToMat(srcBitmap, srcMat);
        Imgproc.cvtColor(srcMat, grayMat, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(grayMat, grayMat);

        if (ccf == null) {
            Log.i(TAG, "faceDetect:        ccf为空！！！！！！");
            return null;
        }
        ccf.detectMultiScale(grayMat, faces);
        Rect[] facesArray = faces.toArray();
        Log.i(TAG, "faceDetect:     检测到的人脸数："+facesArray.length);
        for (int i = 0; i < facesArray.length; i++) {
            Core.rectangle(srcMat, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
        }

        Utils.matToBitmap(srcMat, bitmap);

        return bitmap;
    }

}
