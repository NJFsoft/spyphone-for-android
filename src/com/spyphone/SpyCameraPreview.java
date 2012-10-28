package com.spyphone;


import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/** A basic Camera preview class */
public class SpyCameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera spyCamera;

    public SpyCameraPreview(Context context, Camera camera) {
        super(context);
        spyCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            spyCamera = Camera.open();
            spyCamera.setPreviewDisplay(holder);
            spyCamera.startPreview();
        } catch (Exception e) {
            System.out.println("Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
        if (spyCamera != null) {
            try {
                spyCamera.stopPreview();
                spyCamera.setPreviewCallback(null);
                spyCamera.release();
                spyCamera = null;
            }   catch (Exception e)   {

            }

        }

    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            spyCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            spyCamera.setPreviewDisplay(mHolder);
            spyCamera.startPreview();

        } catch (Exception e){
            System.out.println("Error starting camera preview: " + e.getMessage());
        }
    }




}

