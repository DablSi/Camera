package com.example.ducks.camera;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.*;
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
import java.util.*;

public class MainActivity extends Activity {

    Bitmap bitmap;
    private final int xs = 640, ys = 360;

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

    /*int normalize() {
        int sz = 32;
        if (xs < sz) return 0;
        if (ys < sz) return 0;
        int x, y, c, c0, c1, c00, c01, c10, c11, cavg;

        // compute average intensity in corners
        for (c00 = 0, y = 0; y < sz; y++) for (x = 0; x < sz; x++) c00 += p[y][x].dd;
        c00 /= sz * sz;
        for (c01 = 0, y = 0; y < sz; y++) for (x = xs - sz; x < xs; x++) c01 += p[y][x].dd;
        c01 /= sz * sz;
        for (c10 = 0, y = ys - sz; y < ys; y++) for (x = 0; x < sz; x++) c10 += p[y][x].dd;
        c10 /= sz * sz;
        for (c11 = 0, y = ys - sz; y < ys; y++) for (x = xs - sz; x < xs; x++) c11 += p[y][x].dd;
        c11 /= sz * sz;
        cavg = (c00 + c01 + c10 + c11) / 4;

        // normalize lighting conditions
        for (y = 0; y < ys; y++)
            for (x = 0; x < xs; x++) {
                // avg color = bilinear interpolation of corners colors
                c0 = c00 + (((c01 - c00) * x) / xs);
                c1 = c10 + (((c11 - c10) * x) / xs);
                c = c0 + (((c1 - c0) * y) / ys);
                // scale to avg color
                if (c) p[y][x].dd = (p[y][x].dd * cavg) / c;
            }
        // compute min max intensities
        for (c0 = 0, c1 = 0, y = 0; y < ys; y++)
            for (x = 0; x < xs; x++) {
                c = p[y][x].dd;
                if (c0 > c) c0 = c;
                if (c1 < c) c1 = c;
            }
        // maximize dynamic range <0,765>
        for (y = 0; y < ys; y++)
            for (x = 0; x < xs; x++)
                c = ((p[y][x].dd - c0) * 765) / (c1 - c0);
        return cavg;
    }*/

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
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                LinkedList<Point> linkedList = new LinkedList<>();

                for (int i = 0; i < bitmap.getHeight(); i++) {
                    for (int j = 0; j < bitmap.getWidth(); j++) {
                        int is = bitmap.getPixel(j, i);
                        int need = 0xFF303F9F;
                        // r - 48  g - 63 b - 15
                        int d = (Math.abs(Color.red(is) - Color.red(need)) + Math.abs(Color.green(is) - Color.green(need)) + Math.abs(Color.blue(is) - Color.blue(need)));
                        if (d <= 150) {
                            linkedList.add(new Point(i, j));
                            bitmap.setPixel(j, i, Color.RED);
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
                    Iterator it = treeMap.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<Integer, LinkedList<Integer>> item = (Map.Entry<Integer, LinkedList<Integer>>) it.next();
                        if (item.getValue().size() != a / j)
                            it.remove();
                    }
                    Log.e("PHOTO", Collections.min(treeMap.keySet()) + ";" + treeMap.get(Collections.min(treeMap.keySet())).get(0)
                            + " " + Collections.max(treeMap.keySet()) + ";" + treeMap.get(Collections.max(treeMap.keySet())).get(0));
                }
            }
            return null;
        }
    }
}
