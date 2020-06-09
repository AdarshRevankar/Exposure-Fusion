package com.adrino.renderscript;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adrino.hdr.Manager;
import com.adrino.hdr.corehdr.CreateHDR;
import com.adrino.hdr.corehdr.RsUtils;
import com.adrino.renderscript.utils.ImageItem;
import com.adrino.renderscript.utils.ItemsAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private List<Bitmap> bmpImgList;
    private Manager hdrManager;
    private static boolean darkModeOn = false;
    private ProgressBar progressBar;
    private TextView headerTextView;
    private static int[] btnIds = {
            R.id.btnContrast,
            R.id.btnExposure,
            R.id.btnSaturation,
            R.id.btnNorm,
            R.id.btnGP,
            R.id.btnLP,
            R.id.btnRes,
            R.id.btnHDR};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(getThemeMode());                                   // For Dark Mode
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);   // Initial Animation

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hdrManager = new Manager(getApplicationContext());

        progressBar = findViewById(R.id.main_progress_cycle);
        headerTextView = findViewById(R.id.status);
    }

    /**
     * ========================================================================
     * On Click Listeners
     * ========================================================================
     */
    public void setContrast(View view) {
        updateUI(view, CreateHDR.Actions.CONTRAST);
    }

    public void setSaturation(View view) {
        updateUI(view, CreateHDR.Actions.SATURATION);
    }

    public void setExposure(View view) {
        updateUI(view, CreateHDR.Actions.EXPOSED);
    }

    public void setNormal(View view) {
        updateUI(view, CreateHDR.Actions.NORMAL);
    }

    public void setHDR(View view) {
        updateUI(view, CreateHDR.Actions.HDR);
    }

    public void setResultantPyr(View view) {
        updateUI(view, CreateHDR.Actions.RESULTANT);
    }

    public void setLaplacianPyr(View view) {
        updateUI(view, CreateHDR.Actions.LAPLACIAN);
    }

    public void setGaussianPyr(View view) {
        updateUI(view, CreateHDR.Actions.GAUSSIAN);
    }

    public void captureImage(View view) {
        // Clear the Previous Images captured
        if (bmpImgList != null) {
            bmpImgList.clear();
            bmpImgList = null;
        }

        // Inflate CameraActivity and Capture
        hdrManager.perform(this);
    }

    public void moreInfoAction(View view) {
        // More information - OnClick Listeners
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        if (view.getId() == R.id.btnGithub) {
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse("https://github.com/AdarshRevankar/Exposure-Fusion"));
        } else if (view.getId() == R.id.btnMail) {
            intent.setAction(Intent.ACTION_SENDTO);
            intent.setData(Uri.fromParts("mailto", String.valueOf(R.string.email), null));
        } else if (view.getId() == R.id.btnLink) {
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse("https://adarshrevankar.github.io/Exposure-Fusion"));
        }
        startActivity(intent);
    }

    public void showMoreInfo(View view) {
        // Inflate the UI
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.more_info);
        dialog.show();
    }

    /**
     * ========================================================================
     * Helper Functions
     * ========================================================================
     */
    public void updateUI(View view, final CreateHDR.Actions action) {
        // Get selected button id
        changeButtonView(view.getId());

        // Show the progress bar
        progressBar.setVisibility(View.VISIBLE);

        // Load Images
        if (bmpImgList == null) {
            loadImages();
        }

        // Start a thread for Doing Processing + Updating UI
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Load Image & Meta info
                final List<Bitmap> outputImageList = hdrManager.perform(bmpImgList, action);
                final List<String> metaList = new ArrayList<>(outputImageList.size());
                for (int i = 1; i <= outputImageList.size(); i++) {
                    metaList.add(action + " " + i);
                }

                // Update the UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Update - TextView
                        headerTextView.setText(action.toString());

                        // Recycler View - Result View
                        RecyclerView rvContacts = findViewById(R.id.rvResult);
                        ArrayList<ImageItem> contacts = ImageItem.createImageItemList(outputImageList, metaList);
                        rvContacts.setAdapter(new ItemsAdapter(contacts));
                        rvContacts.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                findViewById(R.id.llcamera).setVisibility(View.GONE);
                                findViewById(R.id.lltopBar).setVisibility(View.VISIBLE);
                                findViewById(R.id.rvOrgImage).setVisibility(View.VISIBLE);
                                findViewById(R.id.rvResult).setVisibility(View.VISIBLE);
                            }
                        }).start();
                    }
                });

            }
        }).start();
    }

    private static int prevBtn;

    private void changeButtonView(int selectedBtnId) {
        // Change the color of other buttons and
        // keep the selected color button to Green
        int color = 0;
        for (int id : btnIds) {
            if (((Button) findViewById(id)).getTextColors().getDefaultColor() !=
                    Color.rgb(0, 124, 255)) {
                color = ((Button) findViewById(id)).getTextColors().getDefaultColor();
                break;
            }
        }

        for (int id : btnIds) {
            ((Button) findViewById(id)).setTextColor(color);
        }

        ((Button) findViewById(selectedBtnId)).setTextColor(Color.rgb(0, 124, 255));
    }

    void loadImages() {
        // Load Images from Storage
        bmpImgList = hdrManager.getBmpImageList(getExternalFilesDir(null));
        Log.e(TAG, "loadImages: " + bmpImgList.size());
        if (bmpImgList.size() < 3 || bmpImgList.get(0) == null) {
            Toast.makeText(MainActivity.this, "Images not loaded / not present", Toast.LENGTH_LONG);
            captureImage(null);
        }

        // Create the name for each of the item
        ArrayList<String> descList = new ArrayList<String>();
        descList.add("Original 1");
        descList.add("Original 2");
        descList.add("Original 3");

        // RecyclerView - Showing Original Image
        RecyclerView rvContacts = findViewById(R.id.rvOrgImage);
        ArrayList<ImageItem> contacts = ImageItem.createImageItemList(RsUtils.resizeBmp(bmpImgList), descList);
        rvContacts.setAdapter(new ItemsAdapter(contacts));
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
    }

    private int getThemeMode() {
        darkModeOn = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
        return darkModeOn ? R.style.DarkTheme : R.style.AppTheme;
    }
}