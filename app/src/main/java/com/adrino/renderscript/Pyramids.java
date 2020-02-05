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
    Bitmap[] lapImg;
    Bitmap[] gaussianLayes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pyramids);
        hdrFilter = new HDRFilter( this);
    }

    public void gotoLapPage(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(gaussianLayes != null && lapImg == null){
                    lapImg = hdrFilter.computeLaplc(gaussianLayes);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        (findViewById(R.id.lapButton)).setBackgroundColor(Color.parseColor("#ff262626"));
                        (findViewById(R.id.txtGP)).setBackgroundColor(Color.parseColor("#ff060606"));
                        ((ImageView)findViewById(R.id.g0)).setImageBitmap(lapImg[0]);
                        ((ImageView)findViewById(R.id.g1)).setImageBitmap(lapImg[1]);
                        ((ImageView)findViewById(R.id.g2)).setImageBitmap(lapImg[2]);
                        ((ImageView)findViewById(R.id.g3)).setImageBitmap(lapImg[3]);
                    }
                });
            }
        }).start();

    }

    public void createGauzz(View view) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    if(gaussianLayes == null) {

                        final Bitmap[] bmpImages = new Bitmap[3];
                        BitmapFactory.Options imgLoadOption = new BitmapFactory.Options();
                        imgLoadOption.inSampleSize = 6;
                        bmpImages[0] = BitmapFactory.decodeResource(getResources(), R.drawable.iphone1, imgLoadOption);
                        bmpImages[1] = BitmapFactory.decodeResource(getResources(), R.drawable.iphone2, imgLoadOption);
                        bmpImages[2] = BitmapFactory.decodeResource(getResources(), R.drawable.iphone3, imgLoadOption);

                        gaussianLayes = hdrFilter.computeGauz(bmpImages);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((ImageView)findViewById(R.id.g0)).setImageBitmap(gaussianLayes[0]);
                            ((ImageView)findViewById(R.id.g1)).setImageBitmap(gaussianLayes[1]);
                            ((ImageView)findViewById(R.id.g2)).setImageBitmap(gaussianLayes[2]);
                            ((ImageView)findViewById(R.id.g3)).setImageBitmap(gaussianLayes[3]);
                        }
                    });
                }
            }).start();
    }
}
