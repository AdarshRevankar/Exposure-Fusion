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

import com.adrino.hdr.camera.CameraViewer;
import com.adrino.hdr.corehdr.CreateHDR;
import com.adrino.renderscript.visual.ViewDialog;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    CameraViewer cameraViewer;

    private static final String TAG = "MainActivity";
    CreateHDR expFusion;
    List<Bitmap> bmpImgList, saturation, contrast, exposed, norm;
    static int SOURCE1, SOURCE2, SOURCE3;
    private ViewDialog viewDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        expFusion = new CreateHDR(this);
        setContentView(R.layout.activity_main);
        viewDialog = new ViewDialog(this);

//        /* - - - - - - - - Get images - - - - - - - */
//        SOURCE1 = R.drawable.sarvesh_iphon1;
//        SOURCE2 = R.drawable.sarvesh_iphone2;
//        SOURCE3 = R.drawable.sarvesh_iphone3;
//
//        Intent intent = getIntent();
//        bmpImgList = new ArrayList<>(Constants.INPUT_IMAGE_SIZE);
//        String path = intent.getStringExtra("location");
//
//
//        if(path != null && new File(path, "pic1.jpg").exists()) {
//            bmpImgList.add(BitmapFactory.decodeFile(new File(path, "pic" + 1 + ".jpg").getAbsolutePath()));
//            bmpImgList.add(BitmapFactory.decodeFile(new File(path, "pic" + 2 + ".jpg").getAbsolutePath()));
//            bmpImgList.add(BitmapFactory.decodeFile(new File(path, "pic" + 3 + ".jpg").getAbsolutePath()));
//
//            /*---------------------- Scale Images ----------------------*/
//            bmpImgList = RsUtils.resizeBmp(bmpImgList);
//
//            /*---------------------- Set Images ----------------------*/
//            ((ImageView) findViewById(R.id.pic1)).setImageBitmap(bmpImgList.get(0));
//            ((ImageView) findViewById(R.id.pic2)).setImageBitmap(bmpImgList.get(1));
//            ((ImageView) findViewById(R.id.pic3)).setImageBitmap(bmpImgList.get(2));
//        } else {
//            Toast.makeText(this, "Please Capture image and Try to process . . .", Toast.LENGTH_LONG).show();
//            this.finish();
//        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(cameraViewer == null) {
            cameraViewer = new CameraViewer();
            cameraViewer.startCameraActivity(this);
        }
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
                    contrast = expFusion.perform(bmpImgList, CreateHDR.Actions.CONTRAST);
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
                    saturation = expFusion.perform(bmpImgList, CreateHDR.Actions.SATURATION);
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
                    exposed = expFusion.perform(bmpImgList, CreateHDR.Actions.EXPOSED);
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
                    norm = expFusion.perform(bmpImgList, CreateHDR.Actions.NORMAL);
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

    @Override
    protected void onDestroy() {
        this.expFusion.destroy();
        expFusion = null;
        super.onDestroy();
    }
}