package com.majesty.snakehelper.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class ScreenCap {

    final String TAG = "MAJEST_TAG";
    
    
    MediaProjectionManager mMediaProjectionManager=null;
    
    
    
    public ScreenCap(Context ct){
        this.mMediaProjectionManager=(MediaProjectionManager) ct.getSystemService(Context.MEDIA_PROJECTION_SERVICE)
    }

    private void startCapture() {

        String mImageName = System.currentTimeMillis() + ".png";
        String mImagePath= "/sdcard";

        Log.e(TAG, "image name is : " + mImageName);
        
        mMediaProjectionManager.createScreenCaptureIntent
        
        
        
        Image image = mImageReader.acquireLatestImage();

        int width = image.getWidth();

        int height = image.getHeight();

        final Image.Plane[] planes = image.getPlanes();

        final ByteBuffer buffer = planes[0].getBuffer();

        int pixelStride = planes[0].getPixelStride();

        int rowStride = planes[0].getRowStride();

        int rowPadding = rowStride - pixelStride * width;

        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);

        bitmap.copyPixelsFromBuffer(buffer);

        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);

        image.close();

        if (bitmap != null) {

            Log.e(TAG, "bitmap  create success ");

            try {

                File fileFolder = new File(mImagePath);

                if (!fileFolder.exists())

                    fileFolder.mkdirs();

                File file = new File(mImagePath, mImageName);

                if (!file.exists()) {

                    Log.e(TAG, "file create success ");

                    file.createNewFile();

                }

                FileOutputStream out = new FileOutputStream(file);

                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

                out.flush();

                out.close();

                Log.e(TAG, "file save success ");

                //Toast.makeText(this.getApplicationContext(), "截图成功", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {

                Log.e(TAG, e.toString());

                e.printStackTrace();

            }

        }

    }
}
