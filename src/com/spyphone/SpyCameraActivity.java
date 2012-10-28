package com.spyphone;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.FrameLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


public class SpyCameraActivity extends SpyPhone {

    private Camera spyCamera;
    private SpyCameraPreview spyPreview;
    public int intPicTaken;

    public void drawGui() {
        setContentView(R.layout.spycam_activity);
        spyCamera = getCameraInstance();
        spyPreview = new SpyCameraPreview(this, spyCamera);
        spyCamera.setPreviewCallback(prevCallBack);
        FrameLayout preview = (FrameLayout) findViewById(R.id.spycam_preview);
        preview.addView(spyPreview);


    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        drawGui();

    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            System.out.println("Camera error:" + e.toString());
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }


    public void doTakePicture() {
        try {
            AudioManager mgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            int streamType = AudioManager.STREAM_SYSTEM;
            mgr.setStreamSolo(streamType, true);
            mgr.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            mgr.setStreamMute(streamType, true);
            spyCamera.stopPreview();
            spyCamera.takePicture(null, null, mPicture, mPicture);
        } catch(Exception e){
            System.out.println("doTakePicture: " + e.toString());
            finish();
        }
    }



    public Camera.PreviewCallback prevCallBack = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            intPicTaken++;
            System.out.println("PreviewCallback onPreviewFrame");
            OutputStream fOut = null;

            try {
                if(intPicTaken == 10) {
                doTakePicture();
                }
            } catch (Exception e) {
                System.out.println("onPreviewFrame: " + e.toString());
            }
        }
    };


    public Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            System.out.println("PictureCallback onPictureTaken");
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 1;
                Bitmap picture = BitmapFactory.decodeByteArray(data, 0, data.length, options);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                picture.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                baos.close();
                System.out.println("PictureCallback onPictureTaken done");
                spyCamera.release();
                saveFile(picture);
            } catch (Exception e) {
                System.out.println("onPictureTaken: " + e.toString());
                finish();
            }
        }
    };



    // saving the file to Gallery
    public void saveFile(Bitmap bitmap) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaStorageDir = Environment.getExternalStorageDirectory();
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                System.out.println("saveFile: failed to create directory");
                return;
            }
        }
        try {
            String saved = MediaStore.Images.Media.insertImage(this.getContentResolver(), bitmap, "title", "description");
            Uri sdCardUri = Uri.parse("file://" + Environment.getExternalStorageDirectory());
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, sdCardUri));
            finish();
            System.out.println("SpyPhone saveFile: ");
        } catch (Exception e) {
            System.out.println("saveFile: " + e.toString());
            e.printStackTrace();
            finish();
        }
    }


}
