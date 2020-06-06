package com.adrino.renderscript;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set the content
        super.onCreate(savedInstanceState);
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

    /**
     * ========================================================================
     * Helper Functions
     * ========================================================================
     */
    Runnable runnableViewLoader = new Runnable() {
        @Override
        public void run() {
            viewDialog.hideDialog();
        }
    };

    public void showCustomLoadingDialog(View view) {
        // Loader - For Matching the UI for holding processing
        viewDialog.showDialog();

        handler = new Handler();
        handler.post(runnableViewLoader);
    }

    public void updateUI(View view, final CreateHDR.Actions action) {
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

                        // Stop loader
                        try {
                            handler.removeCallbacks(runnableViewLoader);
                        } catch (final Exception ex) {
                            Toast.makeText(MainActivity.this, "Something went wrong with loader...", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }).start();
    }

    void loadImages() {
        // Load Images from Storage
        bmpImgList = hdrManager.getBmpImageList(getExternalFilesDir(null));

        // Create the name for each of the item
        ArrayList<String> descList = new ArrayList<String>();
        descList.add("Original1");
        descList.add("Original2");
        descList.add("Original3");

        // RecyclerView - Showing Original Image
        RecyclerView rvContacts = findViewById(R.id.rvOrgImage);
        ArrayList<ImageItem> contacts = ImageItem.createImageItemList(RsUtils.resizeBmp(bmpImgList), descList);
        rvContacts.setAdapter(new ItemsAdapter(contacts));
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
    }
}