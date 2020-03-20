package com.adrino.renderscript;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.adrino.hdr.Manager;
import com.adrino.hdr.corehdr.CreateHDR;
import com.adrino.renderscript.visual.ViewDialog;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    List<Bitmap> bmpImgList, saturation, contrast, exposed, norm;
    private ViewDialog viewDialog;
    private Manager hdrManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hdrManager = new Manager(this);
        setContentView(R.layout.activity_main);
        viewDialog = new ViewDialog(this);

        hdrManager.perform(this, true);
        bmpImgList = new ArrayList<>(hdrManager.getBmpImageList());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    public void doGaussianLaplacian(View view) {
        Intent i = new Intent(MainActivity.this, Pyramids.class);
        i.putExtra("location", this.getExternalFilesDir(null).toString());
        startActivity(i);
    }

    public void doCollapse(View view) {
        Intent i = new Intent(MainActivity.this, Collapse.class);
        i.putExtra("location", this.getExternalFilesDir(null).toString());
        startActivity(i);
    }

    public void setContrast(View view) {
        final String functionName = "Contrast";
        (findViewById(R.id.llView)).setVisibility(View.VISIBLE);
        showCustomLoadingDialog(view);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (contrast == null)
                    contrast = hdrManager.perform(bmpImgList, CreateHDR.Actions.CONTRAST);
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
        showCustomLoadingDialog(view);

        (findViewById(R.id.llView)).setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (saturation == null)
                    saturation = hdrManager.perform(bmpImgList, CreateHDR.Actions.SATURATION);
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
        showCustomLoadingDialog(view);

        (findViewById(R.id.llView)).setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (exposed == null)
                    exposed = hdrManager.perform(bmpImgList, CreateHDR.Actions.EXPOSED);
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
        showCustomLoadingDialog(view);

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (norm == null)
                    norm = hdrManager.perform(bmpImgList, CreateHDR.Actions.NORMAL);
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

    /**
     * Loader - For Matching the UI for holding processing
     */
    public void showCustomLoadingDialog(View view) {

        viewDialog.showDialog();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                viewDialog.hideDialog();
            }
        }, 300);
    }
}