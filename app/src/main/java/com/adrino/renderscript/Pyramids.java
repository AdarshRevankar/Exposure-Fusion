package com.adrino.renderscript;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.renderscript.Allocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Pyramids extends AppCompatActivity {

    ExposureFusion exposureFusion;
    private static int SELECTED_INDEX = 0;
    private static boolean isGauss = true;
    private List<Bitmap> bmpImgList;
    private List<Bitmap> gaussianLayers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pyramids);

        exposureFusion = new ExposureFusion(this);
        bmpImgList = new ArrayList<>();
    }

//    public void gotoLapPage(View view) {
//        isGauss = false;
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//            if(gaussianLayes != null){
//                lapImg = exposureFusion.perform(bmpImgList, ExposureFusion.Actions.GAUSSIAN);
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        (findViewById(R.id.lapButton)).setBackgroundColor(Color.parseColor("#ff262626"));
//                        (findViewById(R.id.txtGP)).setBackgroundColor(Color.parseColor("#ff060606"));
//                        ((ImageView)findViewById(R.id.g0)).setImageBitmap(lapImg.get(SELECTED_INDEX)[0]);
//                        ((ImageView)findViewById(R.id.g1)).setImageBitmap(lapImg[SELECTED_INDEX][1]);
//                        ((ImageView)findViewById(R.id.g2)).setImageBitmap(lapImg[SELECTED_INDEX][2]);
//                        ((ImageView)findViewById(R.id.g3)).setImageBitmap(lapImg[SELECTED_INDEX][3]);
//                    }
//                });
//            }
//            }
//        }).start();
//
//    }

    public void createGauzz(View view) {
            isGauss = true;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    BitmapFactory.Options imgLoadOption = new BitmapFactory.Options();
                    imgLoadOption.inSampleSize = 4;
                    bmpImgList.add(BitmapFactory.decodeResource(getResources(), R.drawable.exp1, imgLoadOption));
                    bmpImgList.add(BitmapFactory.decodeResource(getResources(), R.drawable.exp2, imgLoadOption));
                    bmpImgList.add(BitmapFactory.decodeResource(getResources(), R.drawable.exp3, imgLoadOption));
                    exposureFusion.setMeta(bmpImgList.get(0).getWidth(), bmpImgList.get(0).getHeight(), bmpImgList.get(0).getConfig());

                    gaussianLayers = exposureFusion.perform(bmpImgList, ExposureFusion.Actions.GAUSSIAN, SELECTED_INDEX);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            (findViewById(R.id.lapButton)).setBackgroundColor(Color.parseColor("#ff060606"));
                            (findViewById(R.id.txtGP)).setBackgroundColor(Color.parseColor("#ff262626"));
                            ((ImageView) findViewById(R.id.g0)).setImageBitmap(gaussianLayers.get(0));
                            ((ImageView) findViewById(R.id.g1)).setImageBitmap(gaussianLayers.get(1));
                            ((ImageView) findViewById(R.id.g2)).setImageBitmap(gaussianLayers.get(2));
                            ((ImageView) findViewById(R.id.g3)).setImageBitmap(gaussianLayers.get(3));
                            ((ImageView) findViewById(R.id.g4)).setImageBitmap(gaussianLayers.get(4));
                            ((ImageView) findViewById(R.id.g5)).setImageBitmap(gaussianLayers.get(5));
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
            //gotoLapPage(view);
        }
    }

    public void set2(View view) {
        SELECTED_INDEX = 1;
        if(isGauss){
            createGauzz(view);
        }else{
            //gotoLapPage(view);
        }
    }

    public void set3(View view) {
        SELECTED_INDEX = 2;
        if(isGauss){
            createGauzz(view);
        }else{
            //gotoLapPage(view);
        }
    }
}
