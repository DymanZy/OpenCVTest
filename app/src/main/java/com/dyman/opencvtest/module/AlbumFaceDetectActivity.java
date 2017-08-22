package com.dyman.opencvtest.module;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.Image;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.dyman.opencvtest.Globle;
import com.dyman.opencvtest.R;
import com.dyman.opencvtest.utils.CvFaceUtils;

import java.io.FileNotFoundException;

public class AlbumFaceDetectActivity extends AppCompatActivity {

    private Button openAlbumBtn;
    private ImageView showImageIv;

    private CvFaceUtils mCvFaceUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_face_detect);

        openAlbumBtn = (Button) findViewById(R.id.choosePhoto_iv);
        showImageIv = (ImageView) findViewById(R.id.image_iv);

        openAlbumBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent albumIntent = new Intent(Intent.ACTION_PICK);
                albumIntent.setType("image/*");
                if (albumIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(albumIntent, Globle.REQUEST_OPEN_ALBUM);
                }
            }
        });

        mCvFaceUtils = new CvFaceUtils(AlbumFaceDetectActivity.this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Globle.REQUEST_OPEN_ALBUM && data != null && data.getData() != null) {
            ContentResolver cr = getContentResolver();
            Uri bmpUri = data.getData();
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(cr.openInputStream(bmpUri), new Rect(), options);
                options.inJustDecodeBounds = false;
                options.inSampleSize = calculateSampleSize(options);
                Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(bmpUri), new Rect(), options);

                detectFace(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    /** 计算图片适合显示的大小 */
    private int calculateSampleSize(BitmapFactory.Options options) {
        int outHeight = options.outHeight;
        int outWidth = options.outWidth;
        int sampleSize = 1;
        int destHeight = 1000;
        int destWidth = 1000;
        if (outHeight > destHeight || outWidth > destHeight) {
            if (outHeight > outWidth) {
                sampleSize = outHeight / destHeight;
            } else {
                sampleSize = outWidth / destWidth;
            }
        }
        if (sampleSize < 1) {
            sampleSize = 1;
        }
        return sampleSize;
    }


    /** 人脸检测 */
    private void detectFace(Bitmap bitmap) {
        bitmap = mCvFaceUtils.faceDetect(bitmap);
        showImageIv.setImageBitmap(bitmap);
    }
}
