package com.adrino.renderscript;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

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

    public void updateUI(View view, final CreateHDR.Actions action) {
        // Load Images
        if (bmpImgList == null) {
            loadImages();
        }

        // Start a thread for Doing Processing + Updating UI
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<Bitmap> outputImageList = hdrManager.perform(bmpImgList, action);
                final List<String> metaList = new ArrayList<>(outputImageList.size());
                for (int i = 1; i <= outputImageList.size(); i++) {
                    metaList.add(action + " " + i);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView)findViewById(R.id.status)).setText(action.toString());
                        RecyclerView rvContacts = (RecyclerView) findViewById(R.id.rvResult);
                        ArrayList<ImageItem> contacts = ImageItem.createImageItemList(outputImageList, metaList);
                        ItemsAdapter adapter = new ItemsAdapter(contacts);
                        rvContacts.setAdapter(adapter);
                        rvContacts.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    }
                });
            }
        }).start();
    }

    void loadImages() {
        bmpImgList = hdrManager.getBmpImageList(getExternalFilesDir(null));
        ArrayList<String> descList = new ArrayList<String>();
        descList.add("Original1");
        descList.add("Original2");
        descList.add("Original3");
        RecyclerView rvContacts = findViewById(R.id.rvOrgImage);
        ArrayList<ImageItem> contacts = ImageItem.createImageItemList(RsUtils.resizeBmp(bmpImgList), descList);
        ItemsAdapter adapter = new ItemsAdapter(contacts);
        rvContacts.setAdapter(adapter);
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
    }

    public void setHDR(View view) {
        updateUI(view, CreateHDR.Actions.HDR);
    }

    public void setResultantPyr(View view) {
        updateUI(view, CreateHDR.Actions.RESULTANT);
    }

    public void captureImage(View view) {
        // Show the Camera Activity
        hdrManager.perform(this);
    }
}