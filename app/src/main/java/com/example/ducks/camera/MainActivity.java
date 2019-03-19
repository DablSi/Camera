package com.example.ducks.camera;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.TreeMap;

public class MainActivity extends Activity {

    Bitmap bitmap;

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
                    startActivityForResult(new Intent(MainActivity.this, Main2Activity.class), 1);
                } catch (Exception e) {
                    // Camera is not available (in use or does not exist)
                    Toast.makeText(MainActivity.this, "Camera is not available", Toast.LENGTH_LONG).show();
                }
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        NewThread newThread = new NewThread();
        newThread.execute();
    }

    class NewThread extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            File pictures = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File photoFile = new File(pictures, "Screen.jpg");

            if (photoFile.exists()) {
                FileInputStream fileInputStream = null;
                try {
                    fileInputStream = new FileInputStream(photoFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                bitmap = BitmapFactory.decodeStream(fileInputStream);
                bitmap = Bitmap.createScaledBitmap(bitmap, 640, 360, false);
                LinkedList<Point> linkedList = new LinkedList<>();

                for (int i = 0; i < bitmap.getHeight(); i++) {
                    for (int j = 0; j < bitmap.getWidth(); j++) {
                        Color is = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            is = Color.valueOf(bitmap.getPixel(j, i));
                            Color need = Color.valueOf(0xFF303F9F);
                            if (is.red() + 25 >= need.red() && is.red() - 25 <= need.red()
                                    && is.green() + 25 >= need.green() && is.green() - 25 <= need.green()
                                    && is.blue() + 25 >= need.blue() && is.blue() - 25 <= need.blue()) {
                                linkedList.add(new Point(i, j));
                            }
                        }
                    }
                }

                if (linkedList.size() > 0) {
                    TreeMap<Integer, LinkedList<Integer>> treeMap = new TreeMap<>();
                    for (Point i : linkedList) {
                        if (treeMap.containsKey(i.x)) {
                            treeMap.get(i.x).add(i.y);
                        } else {
                            treeMap.put(i.x, new LinkedList<Integer>());
                            treeMap.get(i.x).add(i.y);
                        }
                    }
                    int j = 0, a = 0;
                    for (int i : treeMap.keySet()) {
                        a += treeMap.get(i).size();
                        j++;
                    }
                    for (int i : treeMap.keySet()) {
                        if (treeMap.get(i).size() != a / j)
                            treeMap.remove(i);
                    }
                    Log.e("PHOTO", Collections.min(treeMap.keySet()) + ";" + treeMap.get(Collections.min(treeMap.keySet())).get(0)
                            + " " + Collections.max(treeMap.keySet()) + ";" + treeMap.get(Collections.max(treeMap.keySet())).get(0));
                }
            }
            return null;
        }
    }
}
