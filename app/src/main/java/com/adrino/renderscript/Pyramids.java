package com.adrino.renderscript;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import static com.adrino.renderscript.MainActivity.SOURCE1;
import static com.adrino.renderscript.MainActivity.SOURCE2;
import static com.adrino.renderscript.MainActivity.SOURCE3;

public class Pyramids extends AppCompatActivity {

    ExposureFusion exposureFusion;
    private static int SELECTED_INDEX = 0;
    private static boolean isGauss = true;
    private List<Bitmap> bmpImgList;
    private List<Bitmap> gaussianLayers;
    private List<Bitmap> laplacianPyr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pyramids);

        exposureFusion = new ExposureFusion(this);
        bmpImgList = new ArrayList<>();

        BitmapFactory.Options imgLoadOption = new BitmapFactory.Options();
        imgLoadOption.inSampleSize = ExposureFusion.SAMPLE_SIZE;
        bmpImgList.add(BitmapFactory.decodeResource(getResources(), SOURCE1, imgLoadOption));
        bmpImgList.add(BitmapFactory.decodeResource(getResources(), SOURCE2, imgLoadOption));
        bmpImgList.add(BitmapFactory.decodeResource(getResources(), SOURCE3, imgLoadOption));
        exposureFusion.setMeta(bmpImgList.get(0).getWidth(), bmpImgList.get(0).getHeight(), bmpImgList.get(0).getConfig());
    }

    public void createLaplacian(View view) {
        isGauss = false;

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (gaussianLayers != null) {
                    laplacianPyr = exposureFusion.perform(bmpImgList, ExposureFusion.Actions.LAPLACIAN, SELECTED_INDEX);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            (findViewById(R.id.btnLP)).setBackgroundColor(Color.parseColor("#ff262626"));
                            (findViewById(R.id.btnGP)).setBackgroundColor(Color.parseColor("#ff060606"));
                            (findViewById(R.id.HDR)).setBackgroundColor(Color.parseColor("#ff060606"));
                            ((ImageView) findViewById(R.id.g0)).setImageBitmap(laplacianPyr.get(0));
                            ((ImageView) findViewById(R.id.g1)).setImageBitmap(laplacianPyr.get(1));
                            ((ImageView) findViewById(R.id.g2)).setImageBitmap(laplacianPyr.get(2));
                            ((ImageView) findViewById(R.id.g3)).setImageBitmap(laplacianPyr.get(3));
                            ((ImageView) findViewById(R.id.g4)).setImageBitmap(laplacianPyr.get(4));
                            ((ImageView) findViewById(R.id.g5)).setImageBitmap(laplacianPyr.get(5));
                        }
                    });
                }
            }
        }).start();

    }

    public void computeHDR(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap hdr = exposureFusion.computeHDR(bmpImgList);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView)findViewById(R.id.g0)).setImageBitmap(hdr);
                    }
                });
            }
        }).start();
    }

    public void createGauzz(View view) {
        isGauss = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                gaussianLayers = exposureFusion.perform(bmpImgList, ExposureFusion.Actions.GAUSSIAN, SELECTED_INDEX);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        (findViewById(R.id.btnLP)).setBackgroundColor(Color.parseColor("#ff060606"));
                        (findViewById(R.id.btnGP)).setBackgroundColor(Color.parseColor("#ff262626"));
                        (findViewById(R.id.HDR)).setBackgroundColor(Color.parseColor("#ff060606"));
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
        if (isGauss) {
            createGauzz(view);
        } else {
            createLaplacian(view);
        }
    }

    public void set2(View view) {
        SELECTED_INDEX = 1;
        if (isGauss) {
            createGauzz(view);
        } else {
            createLaplacian(view);
        }
    }

    public void set3(View view) {
        SELECTED_INDEX = 2;
        if (isGauss) {
            createGauzz(view);
        } else {
            createLaplacian(view);
        }
    }
}
