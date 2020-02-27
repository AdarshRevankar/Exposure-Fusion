package com.adrino.renderscript;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import com.adrino.hdr.corehdr.CreateHDR;

public class hdr extends AppCompatActivity {

    CreateHDR createHDR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hdr);
        createHDR = new CreateHDR(getApplicationContext());
    }

    @Override
    protected void onStart() {
        super.onStart();
        List<Bitmap> inputBitmapsList = new ArrayList<>();
        inputBitmapsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.sarvesh_iphon1));
        inputBitmapsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.sarvesh_iphone3));
        inputBitmapsList.add(BitmapFactory.decodeResource(getResources(), R.drawable.sarvesh_iphone2));

        List<Bitmap> hdrOutputList = createHDR.perform(inputBitmapsList, CreateHDR.Actions.HDR);

        if(hdrOutputList != null && hdrOutputList.get(0) != null){
            ((ImageView)findViewById(R.id.outHDR)).setImageBitmap(hdrOutputList.get(0));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        createHDR.destroy();
    }
}
