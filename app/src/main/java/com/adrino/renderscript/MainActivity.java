package com.adrino.renderscript;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity implements HDRManager.Viewer {

    HDRFilter hdrFilter;
    Bitmap[] bmpImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hdrFilter = new HDRFilter(this);
        setContentView(R.layout.activity_main);

        /*---------------------- Load Images ----------------------*/
        bmpImages = new Bitmap[3];
        BitmapFactory.Options imgLoadOption = new BitmapFactory.Options();
        imgLoadOption.inSampleSize = 4;
        bmpImages[0] = BitmapFactory.decodeResource(getResources(), R.drawable.exp1, imgLoadOption);
        bmpImages[1] = BitmapFactory.decodeResource(getResources(), R.drawable.exp2, imgLoadOption);
        bmpImages[2] = BitmapFactory.decodeResource(getResources(), R.drawable.exp3, imgLoadOption);

        /*---------------------- Set Images ----------------------*/
        ((ImageView) findViewById(R.id.pic1)).setImageBitmap(bmpImages[0]);
        ((ImageView) findViewById(R.id.pic2)).setImageBitmap(bmpImages[1]);
        ((ImageView) findViewById(R.id.pic3)).setImageBitmap(bmpImages[2]);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hdrFilter.destoryRenderScript();
    }

    public void gotoNextPage(View view) {
        Intent i = new Intent(MainActivity.this, Pyramids.class);
        startActivity(i);
    }

    public void doCollapse(View view) {
        Intent i = new Intent(MainActivity.this, Collapse.class);
        startActivity(i);
    }

    public void setContrast(View view) {
        (findViewById(R.id.llView)).setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap conv1 = hdrFilter.applyConvolution3x3Filter(hdrFilter.applyGrayScaleFilter(bmpImages[0]));
                final Bitmap conv2 = hdrFilter.applyConvolution3x3Filter(hdrFilter.applyGrayScaleFilter(bmpImages[1]));
                final Bitmap conv3 = hdrFilter.applyConvolution3x3Filter(hdrFilter.applyGrayScaleFilter(bmpImages[2]));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView) findViewById(R.id.out1)).setImageBitmap(conv1);
                        ((ImageView) findViewById(R.id.out2)).setImageBitmap(conv2);
                        ((ImageView) findViewById(R.id.out3)).setImageBitmap(conv3);
                    }
                });
            }
        }).start();

    }

    public void setSaturation(View view) {
        (findViewById(R.id.llView)).setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap saturation1 = hdrFilter.applySaturationFilter(bmpImages[0]);
                final Bitmap saturation2 = hdrFilter.applySaturationFilter(bmpImages[1]);
                final Bitmap saturation3 = hdrFilter.applySaturationFilter(bmpImages[2]);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView) findViewById(R.id.out1)).setImageBitmap(saturation1);
                        ((ImageView) findViewById(R.id.out2)).setImageBitmap(saturation2);
                        ((ImageView) findViewById(R.id.out3)).setImageBitmap(saturation3);
                    }
                });
            }
        }).start();
    }

    public void setExposure(View view) {
        (findViewById(R.id.llView)).setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap exp1 = hdrFilter.applyExposureFilter(bmpImages[0]);
                final Bitmap exp2 = hdrFilter.applyExposureFilter(bmpImages[1]);
                final Bitmap exp3 = hdrFilter.applyExposureFilter(bmpImages[2]);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView) findViewById(R.id.out1)).setImageBitmap(exp1);
                        ((ImageView) findViewById(R.id.out2)).setImageBitmap(exp2);
                        ((ImageView) findViewById(R.id.out3)).setImageBitmap(exp3);
                    }
                });
            }
        }).start();
    }

    public void setNormal(View view){
        new Thread(new Runnable() {
            @Override
            public void run() {
                (findViewById(R.id.llView)).setVisibility(View.VISIBLE);
                /*---------------------- Apply Filters ----------------------*/
                Bitmap[] normal = hdrFilter.computeNormalWeighted(bmpImages);

                ((ImageView) findViewById(R.id.out1)).setImageBitmap(normal[0]);
                ((ImageView) findViewById(R.id.out2)).setImageBitmap(normal[1]);
                ((ImageView) findViewById(R.id.out3)).setImageBitmap(normal[2]);
            }
        }).start();
    }
}
