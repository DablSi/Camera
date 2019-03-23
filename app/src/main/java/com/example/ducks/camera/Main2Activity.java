package com.example.ducks.camera;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.*;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

public class Main2Activity extends Activity {

    SurfaceView surfaceView;
    Camera camera;
    private final int xs = 640, ys = 360;
    public File photoFile, photoFile2;
    FileOutputStream fos;
    Bitmap bitmap;
    long t;

    public static void setCameraDisplayOrientation(Activity activity, android.hardware.Camera camera) {

        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 270;
                break;
            case Surface.ROTATION_90:
                degrees = 360;
                break;
            case Surface.ROTATION_180:
                degrees = 90;
                break;
            case Surface.ROTATION_270:
                degrees = 180;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        File pictures = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        photoFile = new File(pictures, "Screen.jpg");
        photoFile2 = new File(pictures, "Screen2.jpg");


        surfaceView = findViewById(R.id.surfaceView);

        SurfaceHolder holder = surfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    setCameraDisplayOrientation(Main2Activity.this, camera);
                    camera.setPreviewDisplay(holder);
                    camera.startPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format,
                                       int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });

        Button button = findViewById(R.id.btnTakePicture);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                t = System.currentTimeMillis();
                camera.takePicture(null, null, new PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        try {
                            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                            NewThread newThread = new NewThread();
                            newThread.execute();
                            if (camera != null)
                                camera.release();
                            camera = Camera.open();
                            camera.startPreview();
                            Camera.Parameters cameraParameters = camera.getParameters();
                            //set color efects to none
                            cameraParameters.setColorEffect(Camera.Parameters.EFFECT_NONE);

                            //set antibanding to none
                            if (cameraParameters.getAntibanding() != null) {
                                cameraParameters.setAntibanding(Camera.Parameters.ANTIBANDING_OFF);
                            }

                            // set white ballance
                            if (cameraParameters.getWhiteBalance() != null) {
                                cameraParameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT);
                            }

                            //set flash
                            if (cameraParameters.getFlashMode() != null) {
                                cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                            }

                            //set zoom
                            if (cameraParameters.isZoomSupported()) {
                                cameraParameters.setZoom(0);
                            }

                            //set focus mode
                            cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);

                            camera.setParameters(cameraParameters);

                            camera.takePicture(null, null, new PictureCallback() {
                                @Override
                                public void onPictureTaken(byte[] data, Camera camera) {
                                    try {
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                                        t = System.currentTimeMillis() - t;
                                        FileOutputStream fos = new FileOutputStream(photoFile2);
                                        int orientation = Main2Activity.this.getResources().getConfiguration().orientation;
                                        Matrix matrix = new Matrix();
                                        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                                            matrix.postRotate(90);
                                        }
                                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                                        bitmap = Bitmap.createScaledBitmap(bitmap, xs, ys, false);
                                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                        byte[] arr = stream.toByteArray();
                                        fos.write(arr);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    } finally {
                                        try {
                                            fos.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        setResult(1);
                                        finish();
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        camera = Camera.open();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camera != null)
            camera.release();
        camera = null;
    }

    class NewThread extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                fos = new FileOutputStream(photoFile);
                int orientation = Main2Activity.this.getResources().getConfiguration().orientation;
                Matrix matrix = new Matrix();
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    matrix.postRotate(90);
                }
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                bitmap = Bitmap.createScaledBitmap(bitmap, xs, ys, false);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] arr = stream.toByteArray();
                fos.write(arr);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}