package com.adrino.renderscript;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.adrino.hdr.Manager;
import com.adrino.hdr.corehdr.CreateHDR;
import com.adrino.hdr.corehdr.RsUtils;
import com.adrino.renderscript.visual.ViewDialog;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private List<Bitmap> bmpImgList;
    private ViewDialog viewDialog = new ViewDialog(this);
    private Manager hdrManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set the content
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hdrManager = new Manager(getApplicationContext());

        // Show the Camera Activity
        hdrManager.perform(this);
    }

    /**
     * ========================================================================
     * On Click Listeners
     * ========================================================================
     */
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
        updateUI(view, CreateHDR.Actions.CONTRAST, bmpImgList);
    }

    public void setSaturation(View view) {
        updateUI(view, CreateHDR.Actions.SATURATION, bmpImgList);
    }

    public void setExposure(View view) {
        updateUI(view, CreateHDR.Actions.EXPOSED, bmpImgList);
    }

    public void setNormal(View view) {
        updateUI(view, CreateHDR.Actions.NORMAL, bmpImgList);
    }

    /**
     * ========================================================================
     * Helper Functions
     * ========================================================================
     */
    public void showCustomLoadingDialog(View view) {
        // Loader - For Matching the UI for holding processing
        viewDialog.showDialog();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                viewDialog.hideDialog();
            }
        }, 100);
    }

    public void updateUI(View view, final CreateHDR.Actions action, final List<Bitmap> inputImageList){
        // Load Images
        if (bmpImgList == null) {
            loadImages();
        }

        (findViewById(R.id.llView)).setVisibility(View.VISIBLE);
        showCustomLoadingDialog(view);

        // Start a thread for Doing Processing + Updating UI
        new Thread(new Runnable() {
            @Override
            public void run() {
                    final List<Bitmap> outputImageList = hdrManager.perform(inputImageList, action);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        (findViewById(R.id.bottomTxt)).setVisibility(View.VISIBLE);
                        ((TextView) findViewById(R.id.bottomTxt)).setText(action.toString());
                        ((TextView) findViewById(R.id.out1Text)).setText(action.toString() + " 1");
                        ((ImageView) findViewById(R.id.out1)).setImageBitmap(outputImageList.get(0));
                        ((TextView) findViewById(R.id.out2Text)).setText(action.toString() + " 2");
                        ((ImageView) findViewById(R.id.out2)).setImageBitmap(outputImageList.get(1));
                        ((TextView) findViewById(R.id.out3Text)).setText(action.toString() + " 3");
                        ((ImageView) findViewById(R.id.out3)).setImageBitmap(outputImageList.get(2));
                    }
                });
            }
        }).start();
    }

    void loadImages(){
        bmpImgList = new ArrayList<>(hdrManager.getBmpImageList(getExternalFilesDir(null)));
        new Thread(new Runnable() {
            @Override
            public void run() {
                bmpImgList = RsUtils.resizeBmp(bmpImgList);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView) findViewById(R.id.pic1)).setImageBitmap(bmpImgList.get(0));
                        ((ImageView) findViewById(R.id.pic2)).setImageBitmap(bmpImgList.get(1));
                        ((ImageView) findViewById(R.id.pic3)).setImageBitmap(bmpImgList.get(2));
                    }
                });
            }
        }).start();
    }

}