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

import com.adrino.hdr.corehdr.Constants;
import com.adrino.hdr.corehdr.CreateHDR;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

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

        bmpImages = new ArrayList<>();

        bmpImages.add(BitmapFactory.decodeResource(getResources(), R.drawable.sarvesh_iphon1));
        bmpImages.add(BitmapFactory.decodeResource(getResources(), R.drawable.sarvesh_iphone2));
        bmpImages.add(BitmapFactory.decodeResource(getResources(), R.drawable.sarvesh_iphone3));

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!Constants.MEM_BOOST) {
                    final List<Bitmap> resultant = new CreateHDR(context).perform(bmpImages, CreateHDR.Actions.RESULTANT);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((ImageView) findViewById(R.id.res1)).setImageBitmap(resultant.get(0));
                            ((ImageView) findViewById(R.id.res2)).setImageBitmap(resultant.get(1));
                            ((ImageView) findViewById(R.id.res3)).setImageBitmap(resultant.get(2));
                            ((ImageView) findViewById(R.id.res4)).setImageBitmap(resultant.get(3));
                        }
                    });
                }
                long start = System.currentTimeMillis();
                hdrOutput = createHDR.perform(bmpImages, CreateHDR.Actions.HDR);
                long end = System.currentTimeMillis();

                Log.e(TAG, "run: Total time : " + (float) (end - start) / 1000 + " s");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView) findViewById(R.id.original)).setImageBitmap(bmpImages.get(2));
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
}
