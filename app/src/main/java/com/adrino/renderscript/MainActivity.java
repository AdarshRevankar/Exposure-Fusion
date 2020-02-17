package com.adrino.renderscript;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements HDRManager.Viewer {

    private static final String TAG = "MainActivity";
    ExposureFusion expFusion;
    List<Bitmap> bmpImgList, saturation, contrast, exposed, norm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        expFusion = new ExposureFusion(this);
        setContentView(R.layout.activity_main);

        /*---------------------- Load Images ----------------------*/
        bmpImgList = new ArrayList<>(3);
        BitmapFactory.Options imgLoadOption = new BitmapFactory.Options();
        imgLoadOption.inSampleSize = ExposureFusion.SAMPLE_SIZE;
        bmpImgList.add(BitmapFactory.decodeResource(getResources(), R.drawable.exp1, imgLoadOption));
        bmpImgList.add(BitmapFactory.decodeResource(getResources(), R.drawable.exp2, imgLoadOption));
        bmpImgList.add(BitmapFactory.decodeResource(getResources(), R.drawable.exp3, imgLoadOption));

        /*---------------------- Set Images ----------------------*/
        ((ImageView) findViewById(R.id.pic1)).setImageBitmap(bmpImgList.get(0));
        ((ImageView) findViewById(R.id.pic2)).setImageBitmap(bmpImgList.get(1));
        ((ImageView) findViewById(R.id.pic3)).setImageBitmap(bmpImgList.get(2));

        /*---------------------- Init ----------------------*/
        expFusion.setMeta(bmpImgList.get(0).getWidth(), bmpImgList.get(0).getHeight(), bmpImgList.get(0).getConfig());
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        expFusion.destoryRenderScript();
//    }

    public void doGaussianLaplacian(View view) {
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
                if(contrast == null) contrast = expFusion.perform(bmpImgList, ExposureFusion.Actions.CONTRAST);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView) findViewById(R.id.out1)).setImageBitmap(contrast.get(0));
                        ((ImageView) findViewById(R.id.out2)).setImageBitmap(contrast.get(1));
                        ((ImageView) findViewById(R.id.out3)).setImageBitmap(contrast.get(2));
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
                if(saturation == null) saturation = expFusion.perform(bmpImgList, ExposureFusion.Actions.SATURATION);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView) findViewById(R.id.out1)).setImageBitmap(saturation.get(0));
                        ((ImageView) findViewById(R.id.out2)).setImageBitmap(saturation.get(1));
                        ((ImageView) findViewById(R.id.out3)).setImageBitmap(saturation.get(2));
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
                if(exposed == null) exposed = expFusion.perform(bmpImgList, ExposureFusion.Actions.EXPOSED);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView) findViewById(R.id.out1)).setImageBitmap(exposed.get(0));
                        ((ImageView) findViewById(R.id.out2)).setImageBitmap(exposed.get(1));
                        ((ImageView) findViewById(R.id.out3)).setImageBitmap(exposed.get(2));
                    }
                });
            }
        }).start();
    }

    public void setNormal(View view) {
        (findViewById(R.id.llView)).setVisibility(View.VISIBLE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if(norm == null) norm = expFusion.perform(bmpImgList, ExposureFusion.Actions.NORMAL);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView) findViewById(R.id.out1)).setImageBitmap(norm.get(0));
                        ((ImageView) findViewById(R.id.out2)).setImageBitmap(norm.get(1));
                        ((ImageView) findViewById(R.id.out3)).setImageBitmap(norm.get(2));
                    }
                });
            }
        }).start();
    }
}
