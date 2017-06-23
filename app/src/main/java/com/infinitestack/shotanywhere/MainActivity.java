package com.infinitestack.shotanywhere;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import static com.infinitestack.shotanywhere.ShotsService.ACTION_SHOT;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private static final int PERMISSIONS_REQUEST_STORAGE = 110;
    private static final int REQUEST_MEDIA_PROJECTION = 111;

    private static final String STATE_RESULT_CODE = "RESULT_CODE";
    private static final String STATE_RESULT_DATA = "RESULT_DATA";

    private int mResultCode;
    private Button mButton;
    private MediaProjectionManager mMediaProjectionManager;
    private Intent mResultData;
    private Intent shotIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();
        createScreenCaptureEnvironment();
        requirePermission();
    }

    private void initView() {
        mButton = (Button) findViewById(R.id.bt_main_shot);
    }

    private void initEvent() {
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prepareToShot();
            }
        });
        shotIntent = new Intent(this, ShotsService.class);
        shotIntent.setAction(ACTION_SHOT);

    }

    /**
     * 获取SD卡读写权限
     *
     */
    private void requirePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showDialog(
                        getString(R.string.permission_notice),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        PERMISSIONS_REQUEST_STORAGE);
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setResult(Activity.RESULT_OK);
                                finish();
                            }
                        });
                return;
            }
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_STORAGE);
        }
    }

    private void showDialog(String message, DialogInterface.OnClickListener apply,
                            DialogInterface.OnClickListener denial) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("Apply", apply)
                .setNegativeButton("Denial", denial)
                .create()
                .show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mResultData != null) {
            Log.i(TAG, "onSaveInstanceState()");
            outState.putInt(STATE_RESULT_CODE, mResultCode);
            outState.putParcelable(STATE_RESULT_DATA, mResultData);
        }
    }

    private void createScreenCaptureEnvironment() {
        Log.i(TAG, "createScreenCaptureEnvironment()");
        mMediaProjectionManager = (MediaProjectionManager) getApplication().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    private void prepareToShot() {
        if(mResultData != null && mResultCode != 0){
            ((ShotApplication)getApplication()).setResult(mResultCode);
            ((ShotApplication)getApplication()).setIntent(mResultData);
            createNotify();
//            onClickCustomNotification();
        }else{
            startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
            ((ShotApplication)getApplication()).setMediaProjectionManager(mMediaProjectionManager);
        }
    }

    private void createNotify(){
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent piShot = PendingIntent.getService(this, 0, shotIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        Icon icon = Icon.createWithResource(this, R.drawable.ic_camera);
        Notification.Action shotAction = new Notification.Action.Builder(icon, getString(R.string.notify_title_shot), piShot).build();

        NotifyUtil.notifyShot(this, resultIntent, 1, shotAction);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (null == data) {
            return;
        }
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                Log.i(TAG, "User cancelled");
                Snackbar.make(mButton, getString(R.string.permission_media_error), Snackbar.LENGTH_INDEFINITE)
                        .setAction("重新授权", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                prepareToShot();
                            }
                        })
                        .show();
                return;
            } else {
                createNotify();
            }
            mResultCode = resultCode;
            mResultData = data;
            ((ShotApplication) getApplication()).setResult(resultCode);
            ((ShotApplication) getApplication()).setIntent(data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_STORAGE) {
            for (int r : grantResults){
                if (r == PackageManager.PERMISSION_GRANTED) {
//                    createScreenCaptureEnvironment();
                }
            }

            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != shotIntent) stopService(shotIntent);
        Log.i(TAG, "onDestroy()");
    }
}
