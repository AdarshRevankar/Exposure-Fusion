package com.adrino.renderscript;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity implements HDRManager.Viewer {

    HDRFilter hdrFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hdrFilter = new HDRFilter(this);
        setContentView(R.layout.activity_main);

        /*---------------------- Load Images ----------------------*/
        Bitmap[] bmpImages = new Bitmap[3];
        BitmapFactory.Options imgLoadOption = new BitmapFactory.Options();
        imgLoadOption.inSampleSize = 10;
        bmpImages[0] = BitmapFactory.decodeResource(getResources(), R.drawable.iphone1, imgLoadOption);
        bmpImages[1] = BitmapFactory.decodeResource(getResources(), R.drawable.iphone2, imgLoadOption);
        bmpImages[2] = BitmapFactory.decodeResource(getResources(), R.drawable.iphone3, imgLoadOption);

        /*---------------------- Apply Filters ----------------------*/
        Bitmap[] normal = hdrFilter.computeNormalWeighted(bmpImages);



        /*---------------------- Set Images ----------------------*/
        ((ImageView)findViewById(R.id.pic1)).setImageBitmap(bmpImages[0]);
        ((ImageView)findViewById(R.id.pic2)).setImageBitmap(bmpImages[1]);
        ((ImageView)findViewById(R.id.pic3)).setImageBitmap(bmpImages[2]);

        ((ImageView)findViewById(R.id.out1)).setImageBitmap(normal[0]);
        ((ImageView)findViewById(R.id.out2)).setImageBitmap(normal[1]);
        ((ImageView)findViewById(R.id.out3)).setImageBitmap(normal[2]);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hdrFilter.destoryRenderScript();
    }

    public void gotoNextPage(View view) {
        Intent i = new Intent(MainActivity.this, Pyramids.class);
        onDestroy();
        startActivity(i);
    }
}
