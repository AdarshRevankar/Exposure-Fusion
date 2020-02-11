package com.adrino.renderscript;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class Collapse extends AppCompatActivity {
    HDRFilter hdrFilter;
    Bitmap[] bmpImages;
    Bitmap hdrOutput;
    private static final String TAG = "Collapse";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collapse);
        hdrFilter = new HDRFilter(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap[] resBmp = generateResultant();

                for (int i = 0; i <resBmp.length; i++) {

                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG, "run: " + " Length(R) :" + resBmp.length);
                        ((ImageView) findViewById(R.id.res1)).setImageBitmap(resBmp[0]);
                        ((ImageView) findViewById(R.id.res2)).setImageBitmap(resBmp[1]);
                        ((ImageView) findViewById(R.id.res3)).setImageBitmap(resBmp[2]);
                        ((ImageView) findViewById(R.id.res4)).setImageBitmap(resBmp[3]);
                    }
                });

                hdrOutput = hdrFilter.collapseResultant(resBmp);

                saveBitmaps(hdrOutput, "HDROutput.jpg");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView) findViewById(R.id.hdr)).setImageBitmap(hdrOutput);
                    }
                });
            }
        }).start();
    }

    Bitmap[] generateResultant() {
//        if (bmpImages == null) {
//            bmpImages = new Bitmap[3];
//            BitmapFactory.Options imgLoadOption = new BitmapFactory.Options();
//            imgLoadOption.inSampleSize = 4;
//            bmpImages[0] = BitmapFactory.decodeResource(getResources(), R.drawable.exp1, imgLoadOption);
//            bmpImages[1] = BitmapFactory.decodeResource(getResources(), R.drawable.exp2, imgLoadOption);
//            bmpImages[2] = BitmapFactory.decodeResource(getResources(), R.drawable.exp3, imgLoadOption);
//
//            Bitmap[][] laplacianPyramids = hdrFilter.generateLaplacianPyramids(bmpImages);
//
//            Bitmap[] normalImage = hdrFilter.computeNormalWeighted(bmpImages);
//
////            Bitmap[] normalImage = new Bitmap[3];
////            normalImage[0] = BitmapFactory.decodeResource(getResources(), R.drawable.norm1, imgLoadOption);
////            normalImage[1] = BitmapFactory.decodeResource(getResources(), R.drawable.norm2, imgLoadOption);
////            normalImage[2] = BitmapFactory.decodeResource(getResources(), R.drawable.norm3, imgLoadOption);
//
//            Bitmap[][] gaussianPyramid = new Bitmap[3][4];
//            gaussianPyramid[0] = hdrFilter.generateGaussianPyramid(normalImage[0]);
//            gaussianPyramid[1] = hdrFilter.generateGaussianPyramid(normalImage[1]);
//            gaussianPyramid[2] = hdrFilter.generateGaussianPyramid(normalImage[2]);
//
//            // Resultant Image calculation
//            return hdrFilter.generateResultant(gaussianPyramid, laplacianPyramids);
////            return laplacianPyramids[2];
//        }
        return new Bitmap[0];
    }

    private void saveBitmaps(Bitmap bitmap, String filename){
        String path = Environment.getExternalStorageDirectory().toString();
        String directory = "HDRResult";

        path = path + "/"+ directory;

        if(!new File(path).exists()){
            Log.e(TAG, "saveBitmaps: "+path );
            if(new File(path).mkdir()){
                Log.v("Directory Created :", ""+path);

                try{
                    FileOutputStream out = new FileOutputStream(path+"/"+filename);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                } catch (FileNotFoundException e){
                    Log.e("Cannot Save", "File not found");
                    Log.e("path", ""+path);
                }
            } else{
                Log.e("Directory Error", "Directory cannot be created");
            }
        }

    }
}
