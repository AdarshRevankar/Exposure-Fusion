package com.adrino.renderscript;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class Pyramids extends AppCompatActivity {

    HDRFilter hdrFilter;
    Bitmap[] bmpImages;
    Bitmap[][] lapImg;
    Bitmap[] gaussianLayes;
    private static int SELECTED_INDEX = 0;
    private static boolean isGauss = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pyramids);
        bmpImages = new Bitmap[3];
        hdrFilter = new HDRFilter( this);
    }

    public void gotoLapPage(View view) {
        isGauss = false;

        new Thread(new Runnable() {
            @Override
            public void run() {
            if(gaussianLayes != null){
                lapImg = hdrFilter.generateLaplacianPyramids(bmpImages);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        (findViewById(R.id.lapButton)).setBackgroundColor(Color.parseColor("#ff262626"));
                        (findViewById(R.id.txtGP)).setBackgroundColor(Color.parseColor("#ff060606"));
                        ((ImageView)findViewById(R.id.g0)).setImageBitmap(lapImg[SELECTED_INDEX][0]);
                        ((ImageView)findViewById(R.id.g1)).setImageBitmap(lapImg[SELECTED_INDEX][1]);
                        ((ImageView)findViewById(R.id.g2)).setImageBitmap(lapImg[SELECTED_INDEX][2]);
                        ((ImageView)findViewById(R.id.g3)).setImageBitmap(lapImg[SELECTED_INDEX][3]);
                    }
                });
            }
            }
        }).start();

    }

    public void createGauzz(View view) {
            isGauss = true;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    BitmapFactory.Options imgLoadOption = new BitmapFactory.Options();
                    imgLoadOption.inSampleSize = 4;
                    bmpImages[0] = BitmapFactory.decodeResource(getResources(), R.drawable.norm1, imgLoadOption);
                    bmpImages[1] = BitmapFactory.decodeResource(getResources(), R.drawable.norm2, imgLoadOption);
                    bmpImages[2] = BitmapFactory.decodeResource(getResources(), R.drawable.norm3, imgLoadOption);

                    gaussianLayes = hdrFilter.generateGaussianPyramid(bmpImages[SELECTED_INDEX]);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            (findViewById(R.id.lapButton)).setBackgroundColor(Color.parseColor("#ff060606"));
                            (findViewById(R.id.txtGP)).setBackgroundColor(Color.parseColor("#ff262626"));
                            ((ImageView) findViewById(R.id.g0)).setImageBitmap(gaussianLayes[0]);
                            ((ImageView) findViewById(R.id.g1)).setImageBitmap(gaussianLayes[1]);
                            ((ImageView) findViewById(R.id.g2)).setImageBitmap(gaussianLayes[2]);
                            ((ImageView) findViewById(R.id.g3)).setImageBitmap(gaussianLayes[3]);
                        }
                    });

                }
            }).start();
    }

    public void set1(View view) {
        SELECTED_INDEX = 0;
        if(isGauss){
            createGauzz(view);
        }else{
            gotoLapPage(view);
        }
    }

    public void set2(View view) {
        SELECTED_INDEX = 1;
        if(isGauss){
            createGauzz(view);
        }else{
            gotoLapPage(view);
        }
    }

    public void set3(View view) {
        SELECTED_INDEX = 2;
        if(isGauss){
            createGauzz(view);
        }else{
            gotoLapPage(view);
        }
    }
}
