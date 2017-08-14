package com.dyman.opencvtest.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by dyman on 2017/8/14.
 *
 *  This class is a simple View to display the faces
 */

public class FaceOverlayView extends View{

    private Paint mPaint;
    private int mDisplayOrientation;
    private int mOrientation;
    private int previewWidth = 720, previewHeight = 1280;
    private boolean isFront = false;
    private Rect[] faces;


    public FaceOverlayView(Context context) {
        super(context);
        initialize();
    }

    public FaceOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public FaceOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }


    private void initialize() {

        DisplayMetrics metrics = getResources().getDisplayMetrics();

        int stroke = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, metrics);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeWidth(stroke);
        mPaint.setStyle(Paint.Style.STROKE);
    }


    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }


    public void setDisplayOrientation(int displayOrientation) {
        mDisplayOrientation = displayOrientation;
    }


    public void setPreviewWidth(int previewWidth) {
        this.previewWidth = previewWidth;
    }


    public void setPreviewHeight(int previewHeight) {
        this.previewHeight = previewHeight;
    }


    public void setFaces(Rect[] faces) {
        this.faces = faces;
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (faces != null && faces.length != 0) {
            float scaleX = (float) getWidth() / (float) previewWidth;
            float scaleY = (float) getHeight() / (float) previewHeight;

            switch (mDisplayOrientation) {
                case 90:
                case 270:
                    scaleX = (float) getWidth() / (float) previewHeight;
                    scaleY = (float) getHeight() / (float) previewWidth;
                    break;
            }

            canvas.save();
            canvas.rotate(-mOrientation);
            try {
                for (Rect rect : faces) {

                    //  比例缩放
                    rect.left *= scaleX;
                    rect.top *= scaleY;
                    rect.right *= scaleX;
                    rect.bottom *= scaleY;

                    //  是否需要镜像翻转
                    if (isFront) {
                        float left = rect.left;
                        float right = rect.right;
                        rect.left = (int) (getWidth() - right);
                        rect.right = (int) (getWidth() - left);
                    }

                    canvas.drawRect(rect, mPaint);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            canvas.restore();
        }
    }
}
