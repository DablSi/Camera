package com.example.ducks.camera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = findViewById(R.id.btnTakePicture);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Camera c = null;
                try {
                    c = Camera.open(); // attempt to get a Camera instance
                    c.release();
                    Start start = new Start();
                    start.execute().get();
                    NewThread newThread = new NewThread();
                    newThread.execute();
                } catch (Exception e) {
                    // Camera is not available (in use or does not exist)
                    Toast.makeText(MainActivity.this, "Camera is not available", Toast.LENGTH_LONG).show();
                }
            }
        });


    }

    class NewThread extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            File pictures = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File photoFile = new File("/storage/emulated/legacy/Pictures/Screen.jpg");

            if (photoFile.exists()) {
                FileInputStream fileInputStream = null;
                try {
                    fileInputStream = new FileInputStream(photoFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);
                LinkedList<Point> linkedList = new LinkedList<>();

                for (int i = 0; i < bitmap.getHeight(); i++) {
                    for (int j = 0; j < bitmap.getWidth(); j++) {
                        int n = bitmap.getPixel(j, i);
                        int num = 0xffffb900;
                        if (n < num + 100 && n > num - 100) {
                            linkedList.add(new Point(i, j));
                        }
                    }
                }

                Log.e("PHOTO", linkedList.get(0).x + ";" + linkedList.get(0).y
                        + " " + linkedList.get(linkedList.size() - 1).x + ";" + linkedList.get(linkedList.size() - 1).y);
            }
            return null;
        }
    }

    class Start extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            startService(new Intent(MainActivity.this, Photo.class));
            return null;
        }
    }
}
