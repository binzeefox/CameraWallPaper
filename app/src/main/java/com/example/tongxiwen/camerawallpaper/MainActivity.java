package com.example.tongxiwen.camerawallpaper;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button_reopen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                jumpIn();
            }
        });

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M) {
            checkCameraPermission();
        }else {
            jumpIn();
        }
    }

    private void checkCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                jumpIn();
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    jumpIn();
                } else {
                    Toast.makeText(this, "该应用需要摄像头权限", Toast.LENGTH_LONG).show();
                    recreate();
                }
                break;
            default:
                break;
        }
    }

    private void jumpIn() {
        startActivity(new Intent(MainActivity.this, Camera2Activity.class));
    }
}
