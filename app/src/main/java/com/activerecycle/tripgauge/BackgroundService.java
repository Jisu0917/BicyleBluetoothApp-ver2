package com.activerecycle.tripgauge;

import static com.activerecycle.tripgauge.ConsumptionActivity.dbHelper;
import static com.activerecycle.tripgauge.ConsumptionActivity.device_preferences;
import static com.activerecycle.tripgauge.ConsumptionActivity.odo_preferences;
import static com.activerecycle.tripgauge.ConsumptionActivity.settings_preferences;
import static com.activerecycle.tripgauge.ConsumptionActivity.showDialog;
import static com.activerecycle.tripgauge.ConsumptionActivity.totalDistance;
import static com.activerecycle.tripgauge.ConsumptionActivity.tripADistance;
import static com.activerecycle.tripgauge.ConsumptionActivity.tripBDistance;
import static com.activerecycle.tripgauge.ConsumptionActivity.tripOnceDistance;
import static com.activerecycle.tripgauge.bluetooth.HM10ConnectionService.saveTrip;
import static com.activerecycle.tripgauge.bluetooth.HM10ConnectionService.tripId;

import android.app.Service;
        import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
        import android.util.Log;
import android.widget.Toast;

import com.activerecycle.tripgauge.bluetooth.BleConnectionService;
import com.activerecycle.tripgauge.bluetooth.HM10ConnectionService;

import java.time.LocalDate;

public class BackgroundService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        // 서비스가 생성될 때 실행되는 코드
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 서비스가 시작될 때 실행되는 코드
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 서비스가 종료될 때 실행되는 코드
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        // 앱 프로세스가 강제로 종료되거나 시스템이 메모리를 확보하기 위해 백그라운드에서 앱 프로세스를 종료할 때 실행되는 코드
        Log.d("BackgroundService", "App is removed from recent apps!");

        if (ConsumptionActivity.btconnect) {
            Toast.makeText(BackgroundService.this, "블루투스 연결을 해제합니다.", Toast.LENGTH_SHORT).show();
            HM10ConnectionService.m_bleConnectionService.disconnect();
            Intent intent1 = new Intent(BackgroundService.this, HM10ConnectionService.class);
            stopService(intent1);
            Intent intent2 = new Intent(BackgroundService.this, BleConnectionService.class);
            stopService(intent2);


            if (settings_preferences.getBoolean("s2", true)) { // Auto Save Trip

                //TODO: 트립 저장
                LocalDate currentDate = LocalDate.now();
                String now = currentDate.toString();
                String nowTime = now.replaceAll("-", ".");
                saveTrip(tripId, nowTime);

                //TODO: 마지막으로 연결한 디바이스 정보 저장
                SharedPreferences.Editor editor = device_preferences.edit();  //Editor를 preferences에 쓰겠다고 연결
                if (settings_preferences.getBoolean("s3", true)) {// Auto Connect
                    editor.putString("last_address", device_preferences.getString("address", ""));
                    editor.putString("last_name", device_preferences.getString("name", ""));
                }
                editor.putString("address", "");
                editor.putString("name", "");
                editor.commit();  //항상 commit & apply 를 해주어야 저장이 된다.

                SharedPreferences.Editor editor1 = odo_preferences.edit();
                editor1.putFloat("ODO", (float) totalDistance);
                editor1.putFloat("TRIPA", (float) tripADistance);
                editor1.putFloat("TRIPB", (float) tripBDistance);
                editor1.commit();
                tripOnceDistance = 0;

            } else {
                //TODO: #init 으로 되어있는 트립 삭제
                dbHelper.deleteGarbage();

                //TODO: 마지막으로 연결한 디바이스 정보 저장
                SharedPreferences.Editor editor = device_preferences.edit();  //Editor를 preferences에 쓰겠다고 연결
                if (settings_preferences.getBoolean("s3", true)) {// Auto Connect
                    editor.putString("last_address", device_preferences.getString("address", ""));
                    editor.putString("last_name", device_preferences.getString("name", ""));
                }
                editor.putString("address", "");
                editor.putString("name", "");
                editor.commit();  //항상 commit & apply 를 해주어야 저장이 된다.

                SharedPreferences.Editor editor1 = odo_preferences.edit();
                editor1.putFloat("ODO", (float) totalDistance);
                editor1.putFloat("TRIPA", (float) tripADistance);
                editor1.putFloat("TRIPB", (float) tripBDistance);
                editor1.commit();
                tripOnceDistance = 0;
            }

        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
