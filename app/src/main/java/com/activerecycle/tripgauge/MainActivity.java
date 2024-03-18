package com.activerecycle.tripgauge;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.widget.Toast;

import com.activerecycle.tripgauge.bluetooth.R;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 권한을 체크하고 없는 경우 권한을 요청
        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        String[] permissions = new String[0];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_MEDIA_IMAGES,
            };
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissions = new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                };
        }
        else {
            permissions = new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                };
        }

        // 권한을 이미 허용한 경우 바로 실행
        if (arePermissionsGranted(permissions)) {
            // 권한이 허용되어 있음
            // 필요한 작업을 수행
            performRequiredTasks();
        } else {
            // 권한을 요청
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    private boolean arePermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // 권한 요청 결과 처리
            if (areAllPermissionsGranted(grantResults)) {
                // 모든 권한이 허용된 경우
                // 필요한 작업을 수행
                performRequiredTasks();
            } else {
                // 하나 이상의 권한이 거부된 경우
                // 사용자에게 알림
                Toast.makeText(this, "모든 권한을 허용해야 앱을 정상적으로 사용할 수 있습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean areAllPermissionsGranted(int[] grantResults) {
        for (int result : grantResults) {
            System.out.println("result : " + result);
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void performRequiredTasks() {
        // 필요한 작업을 수행
        // 이 메서드 내에서만 권한이 허용된 것을 가정하고 작업을 수행
        // 이 메서드를 호출하기 전에는 권한을 허용하는 대화상자가 표시되었을 것임
        Intent intent = new Intent(MainActivity.this, ConsumptionActivity.class);
        startActivity(intent);
    }
}
