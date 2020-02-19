package com.adrino.renderscript;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements HDRManager.Viewer {

    private static final String TAG = "MainActivity";
    ExposureFusion expFusion;
    List<Bitmap> bmpImgList, saturation, contrast, exposed, norm;
    static int SOURCE1, SOURCE2, SOURCE3;
    final static int SCALE_THRUSHOLD = 1200;

    // For memory diagnosis purpose
    final Handler handler = new Handler();
    static ActivityManager.MemoryInfo mi;
    ActivityManager activityManager;
    private Runnable periodicUpdate = new Runnable() {
        @Override
        public void run() {
            assert activityManager != null;
            activityManager.getMemoryInfo(mi);
            double availableMegs = mi.availMem / 0x100000L;
            double totalMemory = mi.totalMem / 0x100000L;

            long current = (long) (totalMemory - availableMegs);
            ((TextView) findViewById(R.id.memoryStatus)).setText(current + " MB");

            handler.postDelayed(periodicUpdate, 300);
        }
    };

    // Memory boost
    static Boolean isTouched = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        expFusion = new ExposureFusion(this);
        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        mi = new ActivityManager.MemoryInfo();
        setContentView(R.layout.activity_main);

        /* - - - - - - - - Get images - - - - - - - */
        SOURCE1 = R.drawable.sarvesh_iphon1;
        SOURCE2 = R.drawable.sarvesh_iphone2;
        SOURCE3 = R.drawable.sarvesh_iphone3;

        /*---------------------- Load Images ----------------------*/
        bmpImgList = new ArrayList<>(3);
        bmpImgList.add(BitmapFactory.decodeResource(getResources(), SOURCE1));
        bmpImgList.add(BitmapFactory.decodeResource(getResources(), SOURCE2));
        bmpImgList.add(BitmapFactory.decodeResource(getResources(), SOURCE3));

        /*---------------------- Scale Images ----------------------*/
        int imgWidth = bmpImgList.get(0).getWidth();
        int imgHeight = bmpImgList.get(0).getHeight();
        int scaledWidth = imgHeight > imgWidth ? (imgWidth * SCALE_THRUSHOLD) / imgHeight : SCALE_THRUSHOLD;
        int scaledHeight = imgHeight > imgWidth ? SCALE_THRUSHOLD : (imgHeight * SCALE_THRUSHOLD) / imgWidth;
        for (int i = 0; i < bmpImgList.size(); i++) {
            bmpImgList.set(i, Bitmap.createScaledBitmap(bmpImgList.get(i), scaledWidth, scaledHeight, false));
        }

        /*---------------------- Set Images ----------------------*/
        ((ImageView) findViewById(R.id.pic1)).setImageBitmap(bmpImgList.get(0));
        ((ImageView) findViewById(R.id.pic2)).setImageBitmap(bmpImgList.get(1));
        ((ImageView) findViewById(R.id.pic3)).setImageBitmap(bmpImgList.get(2));

        /*---------------------- Init ----------------------*/
        expFusion.setMeta(bmpImgList.get(0).getWidth(), bmpImgList.get(0).getHeight(), bmpImgList.get(0).getConfig());

        // - - - - - - - - - - -+
        // Other UI Stuff       |
        // - - - - - - - - - - -+

        // For action bar
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        ExposureFusion.MEM_BOOST = false;

        // toggle listener
        (findViewById(R.id.memToggle)).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                isTouched = true;
                return false;
            }
        });
        ((Switch) findViewById(R.id.memToggle)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isTouched) {
                    isTouched = false;
                    ExposureFusion.MEM_BOOST = isChecked;
                    if(isChecked){
                        (findViewById(R.id.Contrast)).setVisibility(View.GONE);
                        (findViewById(R.id.Saturation)).setVisibility(View.GONE);
                        (findViewById(R.id.Exposure)).setVisibility(View.GONE);
                        (findViewById(R.id.normal)).setVisibility(View.GONE);
                        (findViewById(R.id.pyramidTest)).setVisibility(View.GONE);
                    }else{
                        (findViewById(R.id.Contrast)).setVisibility(View.VISIBLE);
                        (findViewById(R.id.Saturation)).setVisibility(View.VISIBLE);
                        (findViewById(R.id.Exposure)).setVisibility(View.VISIBLE);
                        (findViewById(R.id.normal)).setVisibility(View.VISIBLE);
                        (findViewById(R.id.pyramidTest)).setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        new Thread(periodicUpdate).start();
    }

    public void doGaussianLaplacian(View view) {
        Intent i = new Intent(MainActivity.this, Pyramids.class);
        startActivity(i);
    }

    public void doCollapse(View view) {
        Intent i = new Intent(MainActivity.this, Collapse.class);
        startActivity(i);
    }

    public void setContrast(View view) {
        final String functionName = "Contrast";

        (findViewById(R.id.llView)).setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (contrast == null)
                    contrast = expFusion.perform(bmpImgList, ExposureFusion.Actions.CONTRAST);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        (findViewById(R.id.bottomTxt)).setVisibility(View.VISIBLE);
                        ((TextView) findViewById(R.id.bottomTxt)).setText(functionName);
                        ((ImageView) findViewById(R.id.out1)).setImageBitmap(contrast.get(0));
                        ((TextView) findViewById(R.id.out1Text)).setText(functionName+" 1");
                        ((ImageView) findViewById(R.id.out2)).setImageBitmap(contrast.get(1));
                        ((TextView) findViewById(R.id.out2Text)).setText(functionName+" 2");
                        ((ImageView) findViewById(R.id.out3)).setImageBitmap(contrast.get(2));
                        ((TextView) findViewById(R.id.out3Text)).setText(functionName+" 3");
                    }
                });
            }
        }).start();
    }

    public void setSaturation(View view) {
        final String functionName = "Saturation";

        (findViewById(R.id.llView)).setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (saturation == null)
                    saturation = expFusion.perform(bmpImgList, ExposureFusion.Actions.SATURATION);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        (findViewById(R.id.bottomTxt)).setVisibility(View.VISIBLE);
                        ((TextView) findViewById(R.id.bottomTxt)).setText(functionName);
                        ((ImageView) findViewById(R.id.out1)).setImageBitmap(saturation.get(0));
                        ((TextView) findViewById(R.id.out1Text)).setText(functionName+" 1");
                        ((ImageView) findViewById(R.id.out2)).setImageBitmap(saturation.get(1));
                        ((TextView) findViewById(R.id.out2Text)).setText(functionName+" 2");
                        ((ImageView) findViewById(R.id.out3)).setImageBitmap(saturation.get(2));
                        ((TextView) findViewById(R.id.out3Text)).setText(functionName+" 3");
                    }
                });
            }
        }).start();
    }

    public void setExposure(View view) {
        final String functionName = "Exposure";

        (findViewById(R.id.llView)).setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (exposed == null)
                    exposed = expFusion.perform(bmpImgList, ExposureFusion.Actions.EXPOSED);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        (findViewById(R.id.bottomTxt)).setVisibility(View.VISIBLE);
                        ((TextView) findViewById(R.id.bottomTxt)).setText(functionName);
                        ((ImageView) findViewById(R.id.out1)).setImageBitmap(exposed.get(0));
                        ((TextView) findViewById(R.id.out1Text)).setText(functionName+" 1");
                        ((ImageView) findViewById(R.id.out2)).setImageBitmap(exposed.get(1));
                        ((TextView) findViewById(R.id.out2Text)).setText(functionName+" 2");
                        ((ImageView) findViewById(R.id.out3)).setImageBitmap(exposed.get(2));
                        ((TextView) findViewById(R.id.out3Text)).setText(functionName+" 3");
                    }
                });
            }
        }).start();
    }

    public void setNormal(View view) {
        final String functionName = "Normalization";
        (findViewById(R.id.llView)).setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (norm == null)
                    norm = expFusion.perform(bmpImgList, ExposureFusion.Actions.NORMAL);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        (findViewById(R.id.bottomTxt)).setVisibility(View.VISIBLE);
                        ((TextView) findViewById(R.id.bottomTxt)).setText(functionName);
                        ((TextView) findViewById(R.id.out1Text)).setText(functionName+" 1");
                        ((ImageView) findViewById(R.id.out1)).setImageBitmap(norm.get(0));
                        ((TextView) findViewById(R.id.out2Text)).setText(functionName+" 2");
                        ((ImageView) findViewById(R.id.out2)).setImageBitmap(norm.get(1));
                        ((TextView) findViewById(R.id.out3Text)).setText(functionName+" 3");
                        ((ImageView) findViewById(R.id.out3)).setImageBitmap(norm.get(2));
                    }
                });
            }
        }).start();
    }
}
