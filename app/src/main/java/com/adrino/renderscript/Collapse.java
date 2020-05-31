package com.adrino.renderscript;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.adrino.hdr.corehdr.Constants;
import com.adrino.hdr.corehdr.CreateHDR;
import com.adrino.hdr.corehdr.RsUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Collapse extends AppCompatActivity {
    CreateHDR createHDR;
    List<Bitmap> bmpImgList;
    private static final String TAG = "Collapse";
    private List<Bitmap> hdrOutput;
    private boolean set = false;
    private Context context;
    String path;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collapse);
        createHDR = new CreateHDR(this);
        context = this;

        Intent intent = getIntent();
        bmpImgList = new ArrayList<>(Constants.INPUT_IMAGE_SIZE);
        path = intent.getStringExtra("location");
        Log.e(TAG, "onCreate: " + path);
        if (path != null) {
            bmpImgList.add(BitmapFactory.decodeFile(new File(path, "pic" + 1 + ".jpg").getAbsolutePath()));
            bmpImgList.add(BitmapFactory.decodeFile(new File(path, "pic" + 2 + ".jpg").getAbsolutePath()));
            bmpImgList.add(BitmapFactory.decodeFile(new File(path, "pic" + 3 + ".jpg").getAbsolutePath()));

            // Resize
            bmpImgList = RsUtils.resizeBmp(bmpImgList);

        } else {
            Log.e(TAG, "onCreate: Cannot load the image");
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Bitmap> resultant = new CreateHDR(context).perform(bmpImgList, CreateHDR.Actions.RESULTANT);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView) findViewById(R.id.res1)).setImageBitmap(resultant.get(0));
                        ((ImageView) findViewById(R.id.res2)).setImageBitmap(resultant.get(1));
                        ((ImageView) findViewById(R.id.res3)).setImageBitmap(resultant.get(2));
                        ((ImageView) findViewById(R.id.res4)).setImageBitmap(resultant.get(3));
                    }
                });
                long start = System.currentTimeMillis();
                hdrOutput = createHDR.perform(bmpImgList, CreateHDR.Actions.HDR);
                long end = System.currentTimeMillis();

                try {
                    FileOutputStream file = new FileOutputStream(new File(path, "HDR.jpg"));
                    Bitmap out = hdrOutput.get(0);
                    out.compress(Bitmap.CompressFormat.JPEG, 100, file);
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.e(TAG, "run: Total time : " + (float) (end - start) / 1000 + " s");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView) findViewById(R.id.original)).setImageBitmap(bmpImgList.get(0));
                        ((ImageView) findViewById(R.id.hdr)).setImageBitmap(hdrOutput.get(0));
                    }
                });
            }
        }).start();

        (findViewById(R.id.hdr)).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                ImageView imgView = findViewById(R.id.expanded_image);
                if (!set) {
                    if (hdrOutput != null) {
                        imgView.setVisibility(View.VISIBLE);
                        imgView.setImageBitmap(hdrOutput.get(0));
                        set = true;
                    }
                } else {
                    imgView.setVisibility(View.INVISIBLE);
                    set = false;
                }

                return true;
            }
        });
    }

    private void saveBitmaps(Bitmap bitmap, String filename) {
        String path = Environment.getExternalStorageDirectory().toString();
        String directory = "HDRResult";

        path = path + "/" + directory;

        if (!new File(path).exists()) {
            Log.e(TAG, "saveBitmaps: " + path);
            if (new File(path).mkdir()) {
                Log.v("Directory Created :", "" + path);

                try {
                    FileOutputStream out = new FileOutputStream(path + "/" + filename);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                } catch (FileNotFoundException e) {
                    Log.e("Cannot Save", "File not found");
                    Log.e("path", "" + path);
                }
            } else {
                Log.e("Directory Error", "Directory cannot be created");
            }
        }
    }

    @Override
    protected void onDestroy() {
        this.createHDR.destroy();
        this.createHDR = null;
        bmpImgList = null;
        super.onDestroy();
    }
}