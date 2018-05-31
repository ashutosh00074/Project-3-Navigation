package com.tech.gigabyte.navigation;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

/*
 * Created by GIGABYTE on 7/26/2017.
 *
 */

public class ShowActivity extends AppCompatActivity {
    File mFile;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        imageView = (ImageView) findViewById(R.id.imageview_show);
        Intent intent = getIntent();
        String message = intent.getStringExtra(MapsActivity.EXTRA_MESSAGE);
        showImageFromSD(message);
    }

    private void showImageFromSD(String imageUrl) {
        mFile = new File(imageUrl);
        if (mFile.exists()) {
            imageView.setImageBitmap(BitmapFactory.decodeFile(mFile.getAbsolutePath()));
        } else {
            Log.e("showImg ", "FILE NOT EXIST");
            Toast.makeText(getApplicationContext(), "File Not Exist", Toast.LENGTH_LONG).show();
        }

    }
}
