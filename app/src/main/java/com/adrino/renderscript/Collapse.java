package com.adrino.renderscript;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.adrino.renderscript.MainActivity.SCALE_THRUSHOLD;
import static com.adrino.renderscript.MainActivity.SOURCE1;
import static com.adrino.renderscript.MainActivity.SOURCE2;
import static com.adrino.renderscript.MainActivity.SOURCE3;

public class Collapse extends AppCompatActivity {
    CreateHDR createHDR;
    List<Bitmap> bmpImages;
    private static final String TAG = "Collapse";
    private List<Bitmap> hdrOutput;
    private boolean set = false;
    private Context context;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collapse);
        createHDR = new CreateHDR(this);
        context = this;
//
//        bmpImages = new ArrayList<>();
//
//        bmpImages.add(BitmapFactory.decodeResource(getResources(), SOURCE1));
//        bmpImages.add(BitmapFactory.decodeResource(getResources(), SOURCE2));
//        bmpImages.add(BitmapFactory.decodeResource(getResources(), SOURCE3));
//
//        int imgWidth = bmpImages.get(0).getWidth();
//        int imgHeight = bmpImages.get(0).getHeight();
//        int scaledWidth = imgHeight > imgWidth ? (imgWidth * SCALE_THRUSHOLD) / imgHeight : SCALE_THRUSHOLD;
//        int scaledHeight = imgHeight > imgWidth ? SCALE_THRUSHOLD : (imgHeight * SCALE_THRUSHOLD) / imgWidth;
//        for (int i = 0; i < bmpImages.size(); i++) {
//            bmpImages.set(i, Bitmap.createScaledBitmap(bmpImages.get(i), scaledWidth, scaledHeight, false));
//        }
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                if (!Constant.MEM_BOOST) {
//                    final List<Bitmap> resultant = new CreateHDR(context).perform(bmpImages, CreateHDR.Actions.RESULTANT);
//
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            ((ImageView) findViewById(R.id.res1)).setImageBitmap(resultant.get(0));
//                            ((ImageView) findViewById(R.id.res2)).setImageBitmap(resultant.get(1));
//                            ((ImageView) findViewById(R.id.res3)).setImageBitmap(resultant.get(2));
//                            ((ImageView) findViewById(R.id.res4)).setImageBitmap(resultant.get(3));
//                        }
//                    });
//                }
//                long start = System.currentTimeMillis();
//                hdrOutput = createHDR.perform(bmpImages, CreateHDR.Actions.HDR);
//                long end = System.currentTimeMillis();
//
//                Log.e(TAG, "run: Total time : " + (float) (end - start) / 1000 + " s");
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        ((ImageView) findViewById(R.id.original)).setImageBitmap(bmpImages.get(2));
//                        ((ImageView) findViewById(R.id.hdr)).setImageBitmap(hdrOutput.get(0));
//                    }
//                });
//            }
//        }).start();
//
//        (findViewById(R.id.hdr)).setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//
//                ImageView imgView = findViewById(R.id.expanded_image);
//                if (!set) {
//                    if (hdrOutput != null) {
//                        imgView.setVisibility(View.VISIBLE);
//                        imgView.setImageBitmap(hdrOutput.get(0));
//                        set = true;
//                    }
//                } else {
//                    imgView.setVisibility(View.INVISIBLE);
//                    set = false;
//                }
//
//                return true;
//            }
//        });
//    }
//
//    private void saveBitmaps(Bitmap bitmap, String filename) {
//        String path = Environment.getExternalStorageDirectory().toString();
//        String directory = "HDRResult";
//
//        path = path + "/" + directory;
//
//        if (!new File(path).exists()) {
//            Log.e(TAG, "saveBitmaps: " + path);
//            if (new File(path).mkdir()) {
//                Log.v("Directory Created :", "" + path);
//
//                try {
//                    FileOutputStream out = new FileOutputStream(path + "/" + filename);
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
//                } catch (FileNotFoundException e) {
//                    Log.e("Cannot Save", "File not found");
//                    Log.e("path", "" + path);
//                }
//            } else {
//                Log.e("Directory Error", "Directory cannot be created");
//            }
//        }

    }
}
