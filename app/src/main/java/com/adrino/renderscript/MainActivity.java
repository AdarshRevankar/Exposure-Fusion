package com.adrino.renderscript;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adrino.hdr.Manager;
import com.adrino.hdr.corehdr.CreateHDR;
import com.adrino.hdr.corehdr.RsUtils;
import com.adrino.renderscript.utils.ImageItem;
import com.adrino.renderscript.utils.ItemsAdapter;
import com.adrino.renderscript.visual.ViewDialog;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private List<Bitmap> bmpImgList;
    private ViewDialog viewDialog = new ViewDialog(this);
    private Manager hdrManager;
    private Handler handler;
    private Runnable runnableViewLoader = new Runnable() {
        @Override
        public void run() {
            viewDialog.hideDialog();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set the content
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);   // Initial Animation
        setContentView(R.layout.activity_main);
        hdrManager = new Manager(getApplicationContext());
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
            intent.setData(Uri.parse(String.valueOf(R.string.github_uri)));
        } else if (view.getId() == R.id.btnMail) {
            intent.setAction(Intent.ACTION_SENDTO);
            intent.setData(Uri.fromParts("mailto", String.valueOf(R.string.email), null));
        } else if (view.getId() == R.id.btnLink) {
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse(String.valueOf(R.string.web_uri)));
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
    public void showCustomLoadingDialog(View view) {
        // Loader - For Matching the UI for holding processing
        viewDialog.showDialog();
        handler = new Handler();
        handler.post(runnableViewLoader);
    }

    public void updateUI(View view, final CreateHDR.Actions action) {
        // Get selected button id
        final int selectedBtnId = view.getId();
        changeButtonView(selectedBtnId);

        // Load Images
        if (bmpImgList == null) {
            loadImages();
        }

        // Start a thread for Doing Processing + Updating UI
        showCustomLoadingDialog(view);
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
                        ((TextView) findViewById(R.id.status)).setText(action.toString());

                        // Recycler View - Result View
                        RecyclerView rvContacts = findViewById(R.id.rvResult);
                        ArrayList<ImageItem> contacts = ImageItem.createImageItemList(outputImageList, metaList);
                        rvContacts.setAdapter(new ItemsAdapter(contacts));
                        rvContacts.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    handler.removeCallbacks(runnableViewLoader);
                                } catch (final Exception ex) {
                                    Toast.makeText(MainActivity.this, "Something went wrong with loader...", Toast.LENGTH_LONG).show();
                                }

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

    private void changeButtonView(int selectedBtnId) {
        // Change the color of other buttons and
        // keep the selected color button to Green
        int[] btnIds = {R.id.btnContrast, R.id.btnExposure, R.id.btnSaturation, R.id.btnGP, R.id.btnLP, R.id.btnRes, R.id.btnHDR};
        for (int id : btnIds) {
            ((Button) findViewById(id)).setTextColor(Color.BLACK);
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
}