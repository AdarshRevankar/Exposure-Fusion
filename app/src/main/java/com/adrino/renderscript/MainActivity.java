package com.adrino.renderscript;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity implements HDRManager.Viewer {

    HDRFilter hdrFilter;
    BitmapFactory.Options imgLoadOption;
    Bitmap imageBitmap;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            imageBitmap = (Bitmap) extras.get("data");
//            ((ImageView)findViewById(R.id.pic1)).setImageBitmap(imageBitmap);
//
//            Bitmap conv = hdrFilter.applyConvolution3x3Filter(hdrFilter.applyGrayScaleFilter(imageBitmap));
//            Bitmap sat= hdrFilter.applySaturationFilter(imageBitmap);
//            Bitmap exp = hdrFilter.applyExposureFilter(imageBitmap);
//            Bitmap weighted = hdrFilter.computeWeightedFilter(conv, sat, exp);
//
//            Bitmap[] bmpImages = new Bitmap[3];
//            imgLoadOption = new BitmapFactory.Options();
//            imgLoadOption.inSampleSize = 4;
//            bmpImages[0] = BitmapFactory.decodeResource(getResources(), R.drawable.iphone1, imgLoadOption);
//            bmpImages[1] = BitmapFactory.decodeResource(getResources(), R.drawable.iphone2, imgLoadOption);
//            bmpImages[2] = BitmapFactory.decodeResource(getResources(), R.drawable.iphone3, imgLoadOption);
//
////            Bitmap[] bmpImages = {imageBitmap, imageBitmap, imageBitmap};
//            Bitmap normal = hdrFilter.computeNormalWeighted(bmpImages);
//
//            ((ImageView)findViewById(R.id.pic1)).setImageBitmap(imageBitmap);
//            ((ImageView)findViewById(R.id.contrast)).setImageBitmap(conv);
//            ((ImageView)findViewById(R.id.saturated)).setImageBitmap(sat);
//            ((ImageView)findViewById(R.id.exposure)).setImageBitmap(exp);
//            ((ImageView)findViewById(R.id.weighted)).setImageBitmap(weighted);
//            ((ImageView)findViewById(R.id.normal)).setImageBitmap(normal);
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hdrFilter = new HDRFilter(this);
        setContentView(R.layout.activity_main);

//        dispatchTakePictureIntent();

        // Load Images

        Bitmap[] bmpImages = new Bitmap[3];
        imgLoadOption = new BitmapFactory.Options();
        imgLoadOption.inSampleSize = 4;
        bmpImages[0] = BitmapFactory.decodeResource(getResources(), R.drawable.iphone1, imgLoadOption);
        bmpImages[1] = BitmapFactory.decodeResource(getResources(), R.drawable.iphone2, imgLoadOption);
        bmpImages[2] = BitmapFactory.decodeResource(getResources(), R.drawable.iphone3, imgLoadOption);

        Bitmap conv = hdrFilter.applyConvolution3x3Filter(hdrFilter.applyGrayScaleFilter(bmpImages[0]));
        Bitmap sat= hdrFilter.applySaturationFilter(bmpImages[0]);
        Bitmap exp = hdrFilter.applyExposureFilter(bmpImages[0]);
        Bitmap weighted = hdrFilter.computeWeightedFilter(conv, sat, exp);
        Bitmap normal = hdrFilter.computeNormalWeighted(bmpImages);

        ((ImageView)findViewById(R.id.pic1)).setImageBitmap(bmpImages[0]);
        ((ImageView)findViewById(R.id.contrast)).setImageBitmap(conv);
        ((ImageView)findViewById(R.id.saturated)).setImageBitmap(sat);
        ((ImageView)findViewById(R.id.exposure)).setImageBitmap(exp);
        ((ImageView)findViewById(R.id.weighted)).setImageBitmap(weighted);
        ((ImageView)findViewById(R.id.normal)).setImageBitmap(normal);

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
