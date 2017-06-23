package com.infinitestack.shotanywhere;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;

/**
 * ShotsService.java
 * Description :
 * <p>
 * Created by MixtureDD on 2017/6/21 19:16.
 * Copyright © 2017 MixtureDD. All rights reserved.
 */

public class ShotsService extends Service {
    public static final String TAG = ShotsService.class.getName();
    public static final String ACTION_SHOT = "shotanywhere.intent.action.shot";
    private String nameImage = null;

    private MediaProjection mMediaProjection = null;
    private VirtualDisplay mVirtualDisplay = null;
    private NotificationManager mNManager;
    private static final int NOTIFICATION_1 = 1;

    public static int mResultCode;
    public static Intent mResultData;
    public static MediaProjectionManager mMediaProjectionManager1 = null;

    private ImageReader mImageReader;

    private int windowWidth;
    private int windowHeight;
    private int mScreenDensity;

    @Override
    public void onCreate() {
        super.onCreate();
        createVirtualEnvironment();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if (mMediaProjection != null) {
            Log.i(TAG, "exec startShot()/setUpVirtualDisplay()");
            setUpVirtualDisplay();
        } else {
            Log.i(TAG, "exec startShot()/setUpVirtualDisplay()/setUpMediaProjection()");
            setUpMediaProjection();
            setUpVirtualDisplay();
        }
        startToShot();
        return super.onStartCommand(intent, flags, startId);
    }

    private void startToShot() {
        mNManager.cancel(NOTIFICATION_1);
        Handler start = new Handler();
        start.postDelayed(new Runnable() {
            public void run() {
                startCapture();
                stopScreenCapture();
            }
        }, 800);

    }

    private void setUpMediaProjection() {
        mResultData = ((ShotApplication) getApplication()).getIntent();
        mResultCode = ((ShotApplication) getApplication()).getResult();
        Log.i(TAG, "mResultData=" + mResultData);
        Log.i(TAG, "mResultCode=" + mResultCode);
        mMediaProjectionManager1 = ((ShotApplication) getApplication()).getMediaProjectionManager();
        mMediaProjection = mMediaProjectionManager1.getMediaProjection(mResultCode, mResultData);
        Log.i(TAG, "mMediaProjection defined");
    }

    private void setUpVirtualDisplay() {
        Log.i(TAG, "Setting up a VirtualDisplay: " +
                windowWidth + "x" + windowHeight +
                " (" + mScreenDensity + ")");
        mImageReader = ImageReader.newInstance(windowWidth, windowHeight, 0x1, 2); //ImageFormat.RGB_565
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("ScreenCapture",
                windowWidth, windowHeight, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);
    }

    private void createVirtualEnvironment() {
        windowWidth = ScreenHelper.getScreenWidth(this);
        windowHeight = ScreenHelper.getScreenHeight(this);
        mScreenDensity = ScreenHelper.getScreenDensity(this);
        mMediaProjectionManager1 = (MediaProjectionManager) getApplication().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mNManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private void startCapture() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-hhmmss");
        String strDate = "Screenshot_" + dateFormat.format(new java.util.Date());
        String pathImage = Environment.getExternalStorageDirectory().getPath() + "/Pictures/Screenshots/";
        nameImage = pathImage + strDate + ".png";
        Log.i(TAG, "image name is : " + nameImage);
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
        Toast.makeText(this.getApplicationContext(), "正在保存截图", Toast.LENGTH_SHORT).show();
        if (bitmap != null) {
            Log.e(TAG, "bitmap  create success ");
            try {
                File fileFolder = new File(pathImage);
                if (!fileFolder.exists())
                    fileFolder.mkdirs();
                File file = new File(nameImage);
                if (!file.exists()) {
                    Log.e(TAG, "file create success ");
                    file.createNewFile();
                }
                FileOutputStream out = new FileOutputStream(file);
                if (out != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();
                    Intent media = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    Uri contentUri = Uri.fromFile(file);
                    media.setData(contentUri);
                    this.sendBroadcast(media);
                    Log.i(TAG, "screen image saved");
                    Toast.makeText(this.getApplicationContext(), "截图保存成功", Toast.LENGTH_SHORT).show();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
        tearDownMediaProjection();
    }

    private void tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
        Log.i(TAG, "mMediaProjection undefined");
    }

    private void stopScreenCapture() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;
        Log.i(TAG, "virtual display stopped");
    }

}
