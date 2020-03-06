package com.adrino.renderscript;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.adrino.hdr.corehdr.Constants;
import com.adrino.hdr.corehdr.CreateHDR;
import com.adrino.hdr.corehdr.RsUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Pyramids extends AppCompatActivity {

    private static final String TAG = "Pyramids";
    CreateHDR createHDR;
    private static int SELECTED_INDEX = 0;
    private static boolean isGauss = true;
    private List<Bitmap> bmpImgList;
    private List<Bitmap> gaussianLayers;
    private List<Bitmap> laplacianPyr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pyramids);
        createHDR = new CreateHDR(this);
        Intent intent = getIntent();
        bmpImgList = new ArrayList<>(Constants.INPUT_IMAGE_SIZE);
        String path = intent.getStringExtra("location");
        Log.e(TAG, "onCreate: Path present"+path );
        if(path != null){
            bmpImgList.add(BitmapFactory.decodeFile(new File(path, "pic"+1+".jpg").getAbsolutePath()));
            bmpImgList.add(BitmapFactory.decodeFile(new File(path, "pic"+2+".jpg").getAbsolutePath()));
            bmpImgList.add(BitmapFactory.decodeFile(new File(path, "pic"+3+".jpg").getAbsolutePath()));
        } else {
            bmpImgList.add(BitmapFactory.decodeResource(getResources(), R.drawable.sarvesh_iphon1));
            bmpImgList.add(BitmapFactory.decodeResource(getResources(), R.drawable.sarvesh_iphone2));
            bmpImgList.add(BitmapFactory.decodeResource(getResources(), R.drawable.sarvesh_iphone3));
        }

        bmpImgList = RsUtils.resizeBmp(bmpImgList);
    }

    public void createLaplacian(View view) {
        isGauss = false;

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (gaussianLayers != null) {
                    laplacianPyr = createHDR.perform(bmpImgList, CreateHDR.Actions.LAPLACIAN, SELECTED_INDEX);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            (findViewById(R.id.btnLP)).setBackgroundColor(Color.parseColor("#ff262626"));
                            (findViewById(R.id.btnGP)).setBackgroundColor(Color.parseColor("#ff060606"));
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

    public void createGauzz(View view) {
        isGauss = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                gaussianLayers = createHDR.perform(bmpImgList, CreateHDR.Actions.GAUSSIAN, SELECTED_INDEX);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        (findViewById(R.id.btnLP)).setBackgroundColor(Color.parseColor("#ff060606"));
                        (findViewById(R.id.btnGP)).setBackgroundColor(Color.parseColor("#ff262626"));
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

    @Override
    protected void onDestroy() {
        this.createHDR.destroy();
        this.createHDR = null;
        super.onDestroy();
    }
}