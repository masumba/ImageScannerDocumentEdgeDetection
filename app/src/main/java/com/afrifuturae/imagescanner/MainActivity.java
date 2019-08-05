package com.afrifuturae.imagescanner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.SparseArray;
import android.widget.ImageView;
import android.widget.Toast;

import com.adityaarora.liveedgedetection.activity.ScanActivity;
import com.adityaarora.liveedgedetection.constants.ScanConstants;
import com.adityaarora.liveedgedetection.util.ScanUtils;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.gms.vision.text.TextRecognizer.Builder;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 101;
    private ImageView scannedImageView;
    private Bitmap baseBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scannedImageView = findViewById(R.id.scanned_image);

        startScan();

    }

    private void startScan() {
        Intent intent = new Intent(this, ScanActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (null != data && null != data.getExtras()) {
                    String filePath = data.getExtras().getString(ScanConstants.SCANNED_RESULT);
                    baseBitmap = ScanUtils.decodeBitmapFromFile(filePath, ScanConstants.IMAGE_NAME);
                    scannedImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    scannedImageView.setImageBitmap(baseBitmap);

                    /**/
                    ocrBitmap();
                    /**/
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                finish();
            }
        }
    }


    /*OCR*/
    private void ocrBitmap() {

        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        if (!textRecognizer.isOperational()){
            Toast.makeText(this, "Dependencies Not Found", Toast.LENGTH_SHORT).show();
        } else {

            Bitmap bitmapTemp = Bitmap.createBitmap(baseBitmap.getWidth(),baseBitmap.getHeight(),Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmapTemp);
            ColorMatrix ma = new ColorMatrix();
            ma.setSaturation(0);
            Paint paint = new Paint();
            paint.setColorFilter(new ColorMatrixColorFilter(ma));
            canvas.drawBitmap(baseBitmap, 0, 0, paint);

            Frame frame = new Frame.Builder().setBitmap(baseBitmap).build();
            SparseArray<TextBlock> textBlockSparseArray = textRecognizer.detect(frame);

            Paint rectPaint = new Paint();
            rectPaint.setColor(Color.WHITE);
            rectPaint.setStyle(Paint.Style.STROKE);
            rectPaint.setStrokeWidth(4.0f);

            if (textBlockSparseArray.size() != 0){
                for (int i=0;i<textBlockSparseArray.size();i++){
                    TextBlock item = textBlockSparseArray.valueAt(i);

                    rectPaint.setColor(Color.BLACK);
                    rectPaint.setTextSize(560);
                    //canvas.drawText("My Text",0,0,rectPaint);

                    RectF rectF = new RectF(item.getBoundingBox());
                    canvas.drawRect(rectF,rectPaint);

                    scannedImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    scannedImageView.setImageBitmap(bitmapTemp);
                }
            }
        }
    }
}
