package com.adrino.renderscript;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.adrino.hdr.camera.CameraCapture;
import com.adrino.hdr.camera.Constants;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener {

    private CameraCapture cameraCapture;
    private CameraCapture.CameraAction cameraAction;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // If first time activity is loaded
        if (null == savedInstanceState) {
            cameraAction = CameraCapture.CameraAction.FRONT;
            cameraCapture = CameraCapture.newInstance(cameraAction);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, cameraCapture)
                    .commit();
        }

        // Set on click listener
        findViewById(R.id.process).setOnClickListener(this);
        findViewById(R.id.imgPicker).setOnClickListener(this);
        findViewById(R.id.changeCamera).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.process) {
            Intent i = new Intent(CameraActivity.this, MainActivity.class);
            i.putExtra("location", this.getExternalFilesDir(null).toString());
            startActivity(i);
        }

        if (view.getId() == R.id.imgPicker) {
            openImageChooser();
        }

        if (view.getId() == R.id.changeCamera){
            cameraCapture.onDestroy();
            cameraAction = cameraAction == CameraCapture.CameraAction.FRONT ?
                                            CameraCapture.CameraAction.BACK :
                                            CameraCapture.CameraAction.FRONT;
            cameraCapture = CameraCapture.newInstance(cameraAction);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, cameraCapture)
                    .commit();
        }
    }

    void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("pickerLocation1", getPathFormUri(data.getData()));
        startActivity(i);
    }

    private String getPathFormUri(Uri uri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
        assert cursor != null;
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }
}
