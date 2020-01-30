package com.adrino.renderscript;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity implements HDRManager.Viewer {

    HDRFilter hdrFilter;
    BitmapFactory.Options imgLoadOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hdrFilter = new HDRFilter(this);
        setContentView(R.layout.activity_main);

        // Load Images
        imgLoadOption = new BitmapFactory.Options();
        imgLoadOption.inSampleSize = 3;
        Bitmap[] bmpImages = new Bitmap[3];
        bmpImages[0] = BitmapFactory.decodeResource(getResources(), R.drawable.pic1, imgLoadOption);
        bmpImages[1] = BitmapFactory.decodeResource(getResources(), R.drawable.pic2, imgLoadOption);
        bmpImages[2] = BitmapFactory.decodeResource(getResources(), R.drawable.pic3, imgLoadOption);

        // Process Images
        Bitmap bitmapNormalWeighted = hdrFilter.computeNormalWeighted(bmpImages);

//        Bitmap bitmapWeighted = hdrFilter.computeWeightedFilter(
//                hdrFilter.applyConvolution3x3Filter(hdrFilter.applyGrayScaleFilter(bmpImages[0])),
//                hdrFilter.applySaturationFilter(bmpImages[0]),
//                hdrFilter.applyExposureFilter(bmpImages[0]));

        ((ImageView)findViewById(R.id.weighted)).setImageBitmap(bitmapNormalWeighted);
//        saveBitmaps(bitmapWeighted, "weighted.png");

        System.gc();
    }

    private void saveBitmaps(Bitmap bitmap, String filename){
        String path = Environment.getExternalStorageDirectory().toString();
        String directory = "HDRResult";

        path = path + "/"+ directory;

        if(!new File(path).exists()){
            if(new File(path).mkdir()){
                Log.v("Directory Created :", ""+path);

                try{
                    FileOutputStream out = new FileOutputStream(path+"/"+filename);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                } catch (FileNotFoundException e){
                    Log.e("Cannot Save", "File not found");
                    Log.e("path", ""+path);
                }
            } else{
                Log.e("Directory Error", "Directory cannot be created");
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hdrFilter.destoryRenderScript();
    }
}
