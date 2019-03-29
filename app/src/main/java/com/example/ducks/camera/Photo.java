package com.example.ducks.camera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Photo extends Service {
    //Camera variables
    //a surface holder
    private SurfaceHolder sHolder;
    //a variable to control the camera
    private Camera mCamera;
    //the camera parameters
    private Parameters parameters;

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        mCamera = Camera.open();
        SurfaceView sv = new SurfaceView(getApplicationContext());


        try {
            mCamera.setPreviewDisplay(sv.getHolder());
            parameters = mCamera.getParameters();

            //set camera parameters
            mCamera.setParameters(parameters);
            mCamera.startPreview();
            long t = System.currentTimeMillis();
            mCamera.takePicture(null, null, mCall);
            t = System.currentTimeMillis() - t;
            mCamera.takePicture(null, null, mCall);

        } catch (IOException e) {
            e.printStackTrace();
        }


        //Get a surface
        sHolder = sv.getHolder();
        //tells Android that this surface will have its data constantly replaced
        sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }


    Camera.PictureCallback mCall = new Camera.PictureCallback() {

        public void onPictureTaken(byte[] data, Camera camera) {
            //decode the data obtained by the camera into a Bitmap

//            FileOutputStream outStream = null;
//            try {
////                File pictures = Environment
////                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
////                File photoFile = new File(pictures, "Screen.jpg");
////                outStream = new FileOutputStream(photoFile);
////                outStream.write(data);
////                outStream.close();
////                stopSelf();
//            } catch (FileNotFoundException e) {
//                Log.d("CAMERA", e.getMessage());
//            } catch (IOException e) {
//                Log.d("CAMERA", e.getMessage());
//            }

        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCamera != null)
            mCamera.release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
