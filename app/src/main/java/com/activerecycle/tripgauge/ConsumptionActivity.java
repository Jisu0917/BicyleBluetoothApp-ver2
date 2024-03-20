package com.activerecycle.tripgauge;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import static com.activerecycle.tripgauge.bluetooth.HM10ConnectionService.saveTrip;
import static com.activerecycle.tripgauge.bluetooth.HM10ConnectionService.tripId;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.activerecycle.tripgauge.bluetooth.BleConnectionService;
import com.activerecycle.tripgauge.bluetooth.BluetoothScanner;
import com.activerecycle.tripgauge.bluetooth.HM10ConnectionService;
import com.activerecycle.tripgauge.bluetooth.ListOfScansActivity;
import com.activerecycle.tripgauge.bluetooth.R;
import com.activerecycle.tripgauge.bluetooth.StaticResources;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class ConsumptionActivity extends AppCompatActivity {
    static String EXTERNAL_STORAGE_PATH = "";
    static int saveFlagOfLog = 0;
    public static boolean autoSave = true;
    public static boolean autoConnect;


    // 앱에서 디바이스에게 주는 데이터
    public static int speed = 0;
    double previousLat = 0.0; // 이전 위도
    double previousLon = 0.0; // 이전 경도
    public static double totalDistance; // 총 이동 거리
    public static double tripADistance = 0.0;
    double tripBDistance = 0.0;
    double bef_lat, bef_long;
    public static boolean btconnect = false;

    public static Thread blinkThread;

    // 디바이스로부터 받는 데이터
    static int volt = 0; // 전압값 (0~25)
    static int amp = 0; // 전류값 (0~30)
    public static int soc = 0;  // 배터리 잔량

    // w = volt * amp;

    public static TextView tv_title, tv_w, tv_ready, tv_speed, tv_KPH, tv_percent, tv_soc, tv_odo, tv_distance, tv_distFlag;
    ImageButton btn_menu;
    public static SpeedGraph graph_speed;
    public static BatteryGraph graph_battery;

    // DBHelper
    static DBHelper dbHelper;
    static String tripName;
//    static int tripId;

    static Map dataMap = new HashMap();

    public static Context mContext;

    static SharedPreferences odo_preferences, device_preferences, settings_preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumption);

        mContext = getApplicationContext();

        //SettingsActivity.socFlag = SettingsActivity.preferences.getBoolean("s4", true);

        // For Record Activity
        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(getApplicationContext(), "외장 메모리가 마운트 되지 않았습니다.", Toast.LENGTH_LONG).show();
        } else {
            EXTERNAL_STORAGE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
        }


        dbHelper = new DBHelper(ConsumptionActivity.this, 1);


        //tv_title = (TextView) findViewById(R.id.tv_title);
        tv_w = (TextView) findViewById(R.id.tv_w);
        tv_ready = (TextView) findViewById(R.id.tv_ready);
        tv_speed = (TextView) findViewById(R.id.tv_speed);
        tv_KPH = (TextView) findViewById(R.id.tv_KPH);
        tv_percent = (TextView) findViewById(R.id.tv_percent);
        //tv_soc = (TextView) findViewById(R.id.tv_soc);
        tv_odo = (TextView) findViewById(R.id.tv_odo);
        tv_distance = (TextView) findViewById(R.id.tv_distance);
        tv_distFlag = (TextView) findViewById(R.id.tv_distFlag);

        btn_menu = (ImageButton) findViewById(R.id.btn_menu);

        graph_speed = (SpeedGraph) findViewById(R.id.graph_speed);
        graph_battery = (BatteryGraph) findViewById(R.id.graph_battery);

        btn_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ConsumptionActivity.this, ListOfScansActivity.class);
                startActivity(intent);
            }
        });

        odo_preferences = getSharedPreferences("ODO Info", MODE_PRIVATE);
        totalDistance = odo_preferences.getFloat("ODO", 0.0f);

        settings_preferences = getSharedPreferences("Setting Info", MODE_PRIVATE);
        device_preferences = getSharedPreferences("Device Info", MODE_PRIVATE);
        String connectedAddress = device_preferences.getString("last_address", "");
        String connectedName = device_preferences.getString("last_name", "");
        autoConnect = !connectedAddress.equals("") && settings_preferences.getBoolean("s3", true) && MainActivity.isAppRunning;
        System.out.println("connectedAddress: " + connectedAddress + ", autoConnect: " + autoConnect);

        if (autoConnect) {
            Toast.makeText(mContext, "AutoConnect Mode...", Toast.LENGTH_SHORT).show();
//            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//            BluetoothScanner bluetoothScanner = new BluetoothScanner(bluetoothAdapter, getApplicationContext());
//            bluetoothScanner.startScan(2000);
            try {
                final Intent intent = new Intent(getApplicationContext(), HM10ConnectionService.class);
                intent.putExtra(StaticResources.EXTRAS_DEVICE_NAME, connectedName);
                intent.putExtra(StaticResources.EXTRAS_DEVICE_ADDRESS, connectedAddress);
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                startService(intent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(mContext, "Failed to AutoConnect...", Toast.LENGTH_SHORT).show();
            }
        }


        //TODO: 블루투스 연결 되기 전, 초기 상태일 때!
        // Connect 여부 표시
        tv_ready.setText("Connect");
        tv_ready.setTextColor(Color.rgb(255, 0, 0));  //red

        tv_w.setText("0W");
        tv_distance.setText("00.00");
        if (settings_preferences.getString("distFlag", "").equals("Mi")) {
            tv_distFlag.setText("Mi");
        } else {
            tv_distFlag.setText("Km");
        }
        // 배터리
        soc = 0;
        graph_battery.soc = soc;
        graph_battery.invalidate();
        tv_percent.setText("00%");


        //TODO: 그래프 깜빡깜빡 거리는 애니메이션!
        blinkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!btconnect) {
                    graph_speed.speed = 99;
                    graph_speed.invalidate();

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    graph_speed.speed = 0;
                    graph_speed.invalidate();

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        blinkThread.start();



        // gps 주행 속도 측정
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final LocationListener gpsLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {

                double currentLat = location.getLatitude(); // 현재 위도
                double currentLon = location.getLongitude(); // 현재 경도

                // 이전 위치가 있을 때만 거리를 계산하고, 이동 거리를 누적
                if (previousLat != 0.0 && previousLon != 0.0) {
                    double distance = GpsUtils.calculateDistance(previousLat, previousLon, currentLat, currentLon);
                    totalDistance += distance;
                    tripADistance += distance;
                    tripBDistance += distance;
                }

                // 현재 위치를 이전 위치로 설정
                previousLat = currentLat;
                previousLon = currentLon;

                // 주행 속도
                speed = (int) location.getSpeed();// 테스트 : * 10;
                tv_speed.setText(speed + "");
                // 주행 속도 화면에 반영
                graph_speed.speed = speed;
//                graph_speed.invalidate();  //그래프 화면 갱신
                graph_speed.startAnimation();

                if (btconnect && speed > 0)  {
                    HM10ConnectionService.btStartFlag = true;

                    // 총 이동 거리 화면에 반영
                    if (tv_odo.getText().equals("ODO")) {
                        if (settings_preferences.getString("distFlag", "").equals("Mi")) {
                            tv_distance.setText(String.format("%.2f", totalDistance));
                            tv_distFlag.setText("Mi");
                        } else {
                            tv_distance.setText(String.format("%.2f", KPHtoMPH(totalDistance)));
                            tv_distFlag.setText("Km");
                        }
                    } else if (tv_odo.getText().equals("TRIPA")) {
                        if (settings_preferences.getString("distFlag", "").equals("Mi")) {
                            tv_distance.setText(String.format("%.2f", tripADistance));
                            tv_distFlag.setText("Mi");
                        } else {
                            tv_distance.setText(String.format("%.2f", KPHtoMPH(tripADistance)));
                            tv_distFlag.setText("Km");
                        }
                    } else if (tv_odo.getText().equals("TRIPB")) {
                        if (settings_preferences.getString("distFlag", "").equals("Mi")) {
                            tv_distance.setText(String.format("%.2f", tripBDistance));
                            tv_distFlag.setText("Mi");
                        } else {
                            tv_distance.setText(String.format("%.2f", KPHtoMPH(tripBDistance)));
                            tv_distFlag.setText("Km");
                        }
                    }


                    //TODO: 속도 MAX 이상 경고음
                    if (SettingsActivity.speedFlag && ConsumptionActivity.speed >= 25) {
                        //BeepPlayer.playBeep(getApplicationContext());
                        startService(new Intent(getApplicationContext(), BeepService.class));
                    }

                    if (speed > 25) {
                        tv_speed.setTextColor(Color.rgb(255, 192, 0));
                        tv_KPH.setTextColor(Color.rgb(255, 192, 0));
                    } else {
                        tv_speed.setTextColor(Color.WHITE);
                        tv_KPH.setTextColor(Color.WHITE);
                    }
                } else if (!btconnect) {
                    HM10ConnectionService.btStartFlag = false;
                    tv_speed.setText("0");
                    tv_speed.setTextColor(Color.WHITE);
                    graph_speed.speed = 0;
//                    graph_speed.invalidate();
                    graph_speed.startAnimation();
                    tv_KPH.setTextColor(Color.WHITE);
                    tv_distance.setText("00.00");
                }

            }
        };

        if (Build.VERSION.SDK_INT >= 23
                && ContextCompat.checkSelfPermission(
                        getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ConsumptionActivity.this, new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            // 위치 정보를 원하는 시간, 거리마다 갱신해준다.
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000,
                    0,
                    gpsLocationListener);
        } else {
            // 위치 정보를 원하는 시간, 거리마다 갱신해준다.
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000,
                    0,
                    gpsLocationListener);
        }



        // ODO
        tv_odo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tv_odo.getText().equals("ODO")) {
                    tv_odo.setText("TRIPA");
                } else if (tv_odo.getText().equals("TRIPA")) {
                    tv_odo.setText("TRIPB");
                } else if (tv_odo.getText().equals("TRIPB")) {
                    tv_odo.setText("ODO");
                }
            }
        });

        tv_odo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (tv_odo.getText().equals("ODO")) {
                    totalDistance = 0.0;
                    Toast.makeText(ConsumptionActivity.this, "ODO를 초기화 합니다.", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (tv_odo.getText().equals("TRIPA")) {
                    tripADistance = 0.0;
                    Toast.makeText(ConsumptionActivity.this, "TRIP A를 초기화 합니다.", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (tv_odo.getText().equals("TRIPB")) {
                    tripBDistance = 0.0;
                    Toast.makeText(ConsumptionActivity.this, "TRIP B를 초기화 합니다.", Toast.LENGTH_SHORT).show();
                    return true;
                }

                return false;
            }
        });

        if (settings_preferences.getString("distFlag", "").equals("Mi")) {
            tv_KPH.setText("MPH");
        } else {
            tv_KPH.setText("KPH");
        }

    }//end Of Create


    private double KPHtoMPH(double KPH) {
        double MPH = KPH / 1.609;

        return MPH;
    }


    private void showSaveTripDialog(final String nowTime) {
        View dialogView = (View) View.inflate(
                ConsumptionActivity.this, R.layout.dialog_savetrip, null);
        AlertDialog.Builder dig = new AlertDialog.Builder(ConsumptionActivity.this, R.style.Theme_Dialog);
        dig.setView(dialogView);
        dig.setTitle("Save this trip!");

        if ( getApplicationContext().equals(ConsumptionActivity.this) ) {
            Toast.makeText(getApplicationContext(), "한글, 영문, 숫자만 입력 가능합니다.", Toast.LENGTH_SHORT).show();
        }
        final EditText editText = (EditText) dialogView.findViewById(R.id.editText_tripTitle);
        editText.setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                Pattern ps = Pattern.compile("^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ :()_+]+$");
                if (source.equals("") || ps.matcher(source).matches()) {
                    return source;
                }
                return "";
            }
        }});

        dig.setNegativeButton("Cancel", null);
        dig.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                tripName = String.valueOf(editText.getText());

                //saveTrip(tripId, nowTime);

            }
        });

        dig.setCancelable(false);
        dig.show();
    }


//    private static void saveTrip(int tripId, String nowTime) {
//
////        if (tripName == null) { tripName = "Untitled"; }
//        if (dbHelper.getAvgPwrW(tripId) == -999 || dbHelper.getUsedW(tripId) == -999 || dbHelper.getMaxW(tripId) == -2) return;
//        dbHelper.update_TripSTATS(tripId, nowTime, dbHelper.getMaxW(tripId), dbHelper.getUsedW(tripId), (int)(totalDistance * 1000), dbHelper.getAvgPwrW(tripId));
//        dbHelper.update_TripName(tripId, "Untitled");
//
//        //Toast.makeText(ConsumptionActivity.this, "트립이 저장되었습니다.", Toast.LENGTH_SHORT).show();
//
//        String allTrip = dbHelper.getTripSTATS();
//        System.out.println(allTrip);
//
//
//        // Trip 기록 개수 20개 넘으면 자동 삭제
//        dbHelper.deleteGarbage();  //일단 찌꺼기 로그부터 삭제하고나서 개수 세기
//        if (dbHelper.getProfileCount("TripSTATS") + 1 > 20) {
//            dbHelper.deleteTrip();
//        }
//    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        //TODO: 뒤로가기 버튼 눌렀을 때!
        if (btconnect) {
            showDialog(ConsumptionActivity.this, "Are you sure want to exit?", "확인을 누르면 앱과 함께 블루투스 연결이 종료됩니다.");
        } else {
            //앱 종료시키기
            moveTaskToBack(true);
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


    }

    public static void showDialog(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Toast.makeText(context, "블루투스 연결을 해제합니다.", Toast.LENGTH_SHORT).show();
                        HM10ConnectionService.m_bleConnectionService.disconnect();
                        Intent intent1 = new Intent(context, HM10ConnectionService.class);
                        context.stopService(intent1);
                        Intent intent2 = new Intent(context, BleConnectionService.class);
                        context.stopService(intent2);


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
                            editor1.commit();
                            ConsumptionActivity.tripADistance = 0;

                            //메인 액티비티로 !!
                            Intent intent = new Intent(context, MainActivity.class);
                            intent.putExtra("CONTEXT", "CONSUMPTION");
                            context.startActivity(intent);

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
                            editor1.commit();
                            ConsumptionActivity.tripADistance = 0;

                            //메인 액티비티로 !!
                            Intent intent = new Intent(context, MainActivity.class);
                            intent.putExtra("CONTEXT", "CONSUMPTION");
                            context.startActivity(intent);
                        }
                    }
                });
        builder.setNegativeButton("취소", null);
        AlertDialog dialog = builder.show();

        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        Typeface typeface = Typeface.createFromAsset(context.getResources().getAssets(), "gmarket_sans_medium.ttf");
        textView.setTypeface(typeface);
    }


}