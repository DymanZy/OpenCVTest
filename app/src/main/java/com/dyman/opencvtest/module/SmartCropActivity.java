package com.dyman.opencvtest.module;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.dyman.opencvtest.R;
import com.dyman.opencvtest.utils.Scanner;
import com.dyman.opencvtest.view.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;

public class SmartCropActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = SmartCropActivity.class.getSimpleName();
    private static final int REQUEST_OPEN_ALBUM = 100;
    private static final int REQUEST_OPEN_CAMERA = 200;

    private Button chooseBtn;
    private Button scanBtn;
    private Button cropBtn;
    private CropImageView cropImageIv;
    private ImageView showImageIv;

    private Bitmap bitmap;
    private File tempFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_crop);
        initView();
    }


    private void initView() {

        chooseBtn = (Button) findViewById(R.id.choosePhoto_iv);
        scanBtn = (Button) findViewById(R.id.scan_btn);
        cropBtn = (Button) findViewById(R.id.crop_btn);
        showImageIv = (ImageView) findViewById(R.id.showImage_iv);
        cropImageIv = (CropImageView) findViewById(R.id.cropImage_iv);

        chooseBtn.setOnClickListener(this);
        scanBtn.setOnClickListener(this);
        cropBtn.setOnClickListener(this);

        tempFile = new File(getExternalFilesDir("img"), "temp.jpg");
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.choosePhoto_iv:
                showChooseDialog(SmartCropActivity.this);
                break;
            case R.id.scan_btn:
                if (bitmap != null) {
                    Toast.makeText(SmartCropActivity.this, "扫描选区", Toast.LENGTH_SHORT).show();
                    showImageIv.setVisibility(View.INVISIBLE);
                    cropImageIv.setVisibility(View.VISIBLE);
                    cropImageIv.setImageToCrop(bitmap);
                } else {
                    Toast.makeText(SmartCropActivity.this, "请先选择图片", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.crop_btn:
                Toast.makeText(SmartCropActivity.this, "截取选区", Toast.LENGTH_SHORT).show();
                Bitmap crop = cropImageIv.crop();
                cropImageIv.setVisibility(View.INVISIBLE);
                showImageIv.setVisibility(View.VISIBLE);
                showImageIv.setImageBitmap(crop);
                break;
        }
    }


    private void showChooseDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setItems(new CharSequence[]{"打开相机", "打开相册"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        Toast.makeText(context, "打开相机", Toast.LENGTH_SHORT).show();
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
                        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                            startActivityForResult(cameraIntent, REQUEST_OPEN_CAMERA);
                        }

                        break;
                    case 1:
                        Toast.makeText(context, "打开相册", Toast.LENGTH_SHORT).show();
                        Intent albumIntent = new Intent(Intent.ACTION_PICK);
                        albumIntent.setType("image/*");
                        if (albumIntent.resolveActivity(getPackageManager()) != null) {
                            startActivityForResult(albumIntent, REQUEST_OPEN_ALBUM);
                        }
                        break;
                }
            }
        })
        .setCancelable(false)
        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        })
        .show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        Bitmap selectedBitmap = null;
        if (requestCode == REQUEST_OPEN_CAMERA && tempFile.exists()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(tempFile.getPath(), options);
            options.inJustDecodeBounds = false;
            options.inSampleSize = calculateSampleSize(options);
            selectedBitmap = BitmapFactory.decodeFile(tempFile.getPath(), options);
        } else if (requestCode == REQUEST_OPEN_ALBUM && data != null && data.getData() != null) {
            ContentResolver cr = getContentResolver();
            Uri bmpUri = data.getData();
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(cr.openInputStream(bmpUri), new Rect(), options);
                options.inJustDecodeBounds = false;
                options.inSampleSize = calculateSampleSize(options);
                selectedBitmap = BitmapFactory.decodeStream(cr.openInputStream(bmpUri), new Rect(), options);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (selectedBitmap != null) {
            bitmap = selectedBitmap;
            cropImageIv.setVisibility(View.INVISIBLE);
            showImageIv.setVisibility(View.VISIBLE);
            showImageIv.setImageBitmap(bitmap);
        }
    }


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
}
