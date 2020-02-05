package com.adrino.renderscript;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.renderscript.RenderScript;
import android.widget.ImageView;

public class Pyramids extends AppCompatActivity {

    HDRFilter hdrFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pyramids);
        hdrFilter = new HDRFilter( this);

        final Bitmap[] bmpImages = new Bitmap[3];
        BitmapFactory.Options imgLoadOption = new BitmapFactory.Options();
        imgLoadOption.inSampleSize = 10;
        bmpImages[0] = BitmapFactory.decodeResource(getResources(), R.drawable.iphone1, imgLoadOption);
        bmpImages[1] = BitmapFactory.decodeResource(getResources(), R.drawable.iphone2, imgLoadOption);
        bmpImages[2] = BitmapFactory.decodeResource(getResources(), R.drawable.iphone3, imgLoadOption);

        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap[] gaussianLayes = hdrFilter.compute(bmpImages);

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
