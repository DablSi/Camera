package com.example.ducks.camera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.*;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    Bitmap bitmap, bitmap2;
    private final int xs = 640, ys = 360;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                //ask for authorisation
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 50);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                //ask for authorisation
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 50);

        Button btn = findViewById(R.id.btnTakePicture);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Camera c = null;
                try {
                    c = Camera.open(); // attempt to get a Camera instance
                    c.release();
                    //startActivityForResult(new Intent(MainActivity.this, Main2Activity.class), 1);
                    startActivityForResult(new Intent(MainActivity.this, Camera2.class), 1);
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
            File photoFile2 = new File(pictures, "Screen2.jpg");

            if (photoFile.exists() && photoFile2.exists()) {
                FileInputStream fileInputStream = null, fileInputStream2 = null;
                try {
                    fileInputStream = new FileInputStream(photoFile);
                    fileInputStream2 = new FileInputStream(photoFile2);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                bitmap = BitmapFactory.decodeStream(fileInputStream);
                int orientation = MainActivity.this.getResources().getConfiguration().orientation;
                Matrix matrix = new Matrix();
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    matrix.postRotate(-90);
                }
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                bitmap = Bitmap.createScaledBitmap(bitmap, xs, ys, false);
                bitmap2 = BitmapFactory.decodeStream(fileInputStream2);
                bitmap2 = bitmap2.copy(Bitmap.Config.ARGB_8888, true);
                matrix = new Matrix();
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    matrix.postRotate(-90);
                }
                bitmap2 = Bitmap.createBitmap(bitmap2, 0, 0, bitmap2.getWidth(), bitmap2.getHeight(), matrix, true);
                bitmap2 = Bitmap.createScaledBitmap(bitmap2, xs, ys, false);

                LinkedList<Point> linkedList = new LinkedList<>();

                for (int i = 0; i < bitmap.getHeight(); i++) {
                    for (int j = 0; j < bitmap.getWidth(); j++) {
                        if (bitmap.getPixel(j, i) != bitmap2.getPixel(j, i)) {
                            int is = bitmap2.getPixel(j, i);
                            float[] hsv = new float[3];
                            Color.RGBToHSV(Color.red(is), Color.green(is), Color.blue(is), hsv);
                            int need = 0xff303f10;
                            float[] hsv2 = new float[3];
                            Color.RGBToHSV(Color.red(need), Color.green(need), Color.blue(need), hsv2);
                            if (Math.abs(hsv[0] - hsv2[0]) <= 16 && Math.abs(hsv[1] - hsv2[1]) <= 0.35 && Math.abs(hsv[2] - hsv2[2]) <= 0.35
                                    && Math.abs(Color.red(is) - Color.red(need)) <= 95
                                    && Math.abs(Color.blue(is) - Color.blue(need)) <= 95
                                    && Math.abs(Color.green(is) - Color.green(need)) <= 95) {
                                linkedList.add(new Point(i, j));
                                bitmap2.setPixel(j, i, Color.RED);
                            }

                        int need2 = 0xff303f9f;
                        float[] hsv3 = new float[3];
                        Color.RGBToHSV(Color.red(need2), Color.green(need2), Color.blue(need2), hsv3);
                        if (Math.abs(hsv[0] - hsv3[0]) <= 14 && Math.abs(hsv[1] - hsv3[1]) <= 0.3 && Math.abs(hsv[2] - hsv3[2]) <= 0.3
                                && Math.abs(Color.red(is) - Color.red(need2)) <= 95
                                && Math.abs(Color.blue(is) - Color.blue(need2)) <= 95
                                && Math.abs(Color.green(is) - Color.green(need2)) <= 95) {
                            bitmap2.setPixel(j, i, Color.WHITE);
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
