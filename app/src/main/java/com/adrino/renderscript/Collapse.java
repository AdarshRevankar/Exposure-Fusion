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
import java.util.ArrayList;
import java.util.List;

import static com.adrino.renderscript.MainActivity.SOURCE1;
import static com.adrino.renderscript.MainActivity.SOURCE2;
import static com.adrino.renderscript.MainActivity.SOURCE3;

public class Collapse extends AppCompatActivity {
    ExposureFusion exposureFusion;
    List<Bitmap> bmpImages;
    private static final String TAG = "Collapse";
    private List<Bitmap> hdrOutput;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collapse);
        exposureFusion = new ExposureFusion(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Bitmap> resultant = generateResultant();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView) findViewById(R.id.res1)).setImageBitmap(resultant.get(0));
                        ((ImageView) findViewById(R.id.res2)).setImageBitmap(resultant.get(1));
                        ((ImageView) findViewById(R.id.res3)).setImageBitmap(resultant.get(2));
                        ((ImageView) findViewById(R.id.res4)).setImageBitmap(resultant.get(3));
                    }
                });

                hdrOutput = exposureFusion.perform(bmpImages, ExposureFusion.Actions.COLLAPSE);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView)findViewById(R.id.original)).setImageBitmap(bmpImages.get(0));
                        ((ImageView) findViewById(R.id.hdr)).setImageBitmap(hdrOutput.get(0));
                    }
                });
            }
        }).start();
    }

    List<Bitmap> generateResultant() {
        if (bmpImages == null) {
            bmpImages = new ArrayList<>();
            BitmapFactory.Options imgLoadOption = new BitmapFactory.Options();
            imgLoadOption.inSampleSize = ExposureFusion.SAMPLE_SIZE;
            bmpImages.add(BitmapFactory.decodeResource(getResources(), SOURCE1, imgLoadOption));
            bmpImages.add(BitmapFactory.decodeResource(getResources(), SOURCE2, imgLoadOption));
            bmpImages.add(BitmapFactory.decodeResource(getResources(), SOURCE3, imgLoadOption));
            return exposureFusion.perform(bmpImages, ExposureFusion.Actions.RESULTANT);
        }
        return null;
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
