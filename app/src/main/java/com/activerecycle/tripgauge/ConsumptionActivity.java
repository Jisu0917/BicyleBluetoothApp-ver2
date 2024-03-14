package com.activerecycle.tripgauge;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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

import com.activerecycle.tripgauge.bluetooth.ListOfScansActivity;
import com.activerecycle.tripgauge.bluetooth.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class ConsumptionActivity extends AppCompatActivity {
    static String EXTERNAL_STORAGE_PATH = "";
    static int saveFlagOfLog = 0;
    public static boolean autoSave = true;
    public static boolean autoConnect = false;


    // 앱에서 디바이스에게 주는 데이터
    int speed = 0;
    double previousLat = 0.0; // 이전 위도
    double previousLon = 0.0; // 이전 경도
    static double totalDistance = 0.0; // 총 이동 거리
    double tripADistance = 0.0;
    double tripBDistance = 0.0;
    double bef_lat, bef_long;
    public static boolean btconnect = false;

    // 디바이스로부터 받는 데이터
    static int volt = 0; // 전압값 (0~25)
    static int amp = 0; // 전류값 (0~30)
    static int soc = 0;  // 배터리 잔량

    // w = volt * amp;

    public static TextView tv_title, tv_w, tv_ready, tv_speed, tv_KPH, tv_percent, tv_soc, tv_odo, tv_distance;
    ImageButton btn_menu;
    SpeedGraph graph_speed;
    static BatteryGraph graph_battery;

    // DBHelper
    static DBHelper dbHelper;
    static String tripName;
    static int tripId;

    static Map dataMap = new HashMap();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumption);

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

        if (autoConnect) {
            btconnect = true;
        }

        //TODO: 임시로 만든 버튼!!
        tv_ready.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btconnect = !btconnect;  //Toggle
                if (btconnect) {
                    tv_ready.setText("Ready");
                    tv_ready.setTextColor(Color.rgb(146, 208, 80));  //green
                    startThread();


                } else {
                    tv_ready.setText("Connect");
                    tv_ready.setTextColor(Color.rgb(255, 0, 0));  //red

                    tv_w.setText("0W");
                    tv_distance.setText("00.00 Km");

                    // 배터리
                    soc = 0;
                    graph_battery.soc = soc;
                    graph_battery.invalidate();
                    tv_percent.setText("00%");

                    //TODO: 그래프 깜빡깜빡 거리는 애니메이션!
                    new Thread(new Runnable() {
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
                    }).start();
                }
                Toast.makeText(ConsumptionActivity.this, "BT connect Toggle Activate...", Toast.LENGTH_SHORT).show();
            }
        });

        // Connect 여부 표시
        if (btconnect) {
            // 블루투스 연결된 상태이면
            tv_ready.setText("Ready");
            tv_ready.setTextColor(Color.rgb(146, 208, 80));  //green


            //TODO: 배터리 값 디바이스에서 블루투스로 받아오기!!!!!!!!
            // 받아오는 배터리 값 달라질 때마다 graph_battery.invalidate();
            soc = 10;
            graph_battery.soc = soc;
            graph_battery.invalidate();
            tv_percent.setText(soc+"%");

        } else {
            // 블루투스 연결 안 된 상태이면
            tv_ready.setText("Connect");
            tv_ready.setTextColor(Color.rgb(255, 0, 0));  //red

            tv_w.setText("0W");
            tv_distance.setText("00.00 Km");

            // 배터리
            soc = 0;
            graph_battery.soc = soc;
            graph_battery.invalidate();
            tv_percent.setText("00%");


            //TODO: 그래프 깜빡깜빡 거리는 애니메이션!
            new Thread(new Runnable() {
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
            }).start();
        }

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

                // 총 이동 거리 화면에 반영
                if (tv_odo.getText().equals("ODO")) {
                    tv_distance.setText(String.format("%.2f", totalDistance) + " Km");
                } else if (tv_odo.getText().equals("TRIPA")) {
                    tv_distance.setText(String.format("%.2f", tripADistance) + " Km");
                } else if (tv_odo.getText().equals("TRIPB")) {
                    tv_distance.setText(String.format("%.2f", tripBDistance) + " Km");
                }

                // 주행 속도
                speed = (int) location.getSpeed();

                // 주행 속도 화면에 반영
                graph_speed.speed = speed;
                graph_speed.invalidate();  //그래프 화면 갱신

                tv_speed.setText(speed+"");

                if (speed > 25) {
                    tv_speed.setTextColor(Color.rgb(255, 192, 0));
                    tv_KPH.setTextColor(Color.rgb(255, 192, 0));
                } else {
                    tv_speed.setTextColor(Color.WHITE);
                    tv_KPH.setTextColor(Color.WHITE);
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

        startThread();

    }

    public static void startThread() {

        final TripLogActivity tripLogActivity = new TripLogActivity();

        // 로그 db에 기록
        final long[] mNow = new long[1];
        final Date[] mDate = new Date[1];
        final SimpleDateFormat mFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
        mFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));

        saveFlagOfLog = 0;
        tripId = dbHelper.init_TripSTATS();
        // 2초마다 실행
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (btconnect) {
                    // 수행할 작업
                    mNow[0] = System.currentTimeMillis();
                    mDate[0] = new Date(mNow[0]);
                    final String nowTime = mFormat.format(mDate[0]);

                    // W 표시
                    /*
                     * random() 난수 발생 코드는 확인용 코드임.
                     * - 추후 삭제 요망
                     * */
                    //TODO: 블루투스로 디바이스로부터 값을 받아와야함!!!
                    volt = (int) (Math.random() * 25);
                    amp = (int) (Math.random() * 30);


                    //TODO: 배터리 값 디바이스에서 블루투스로 받아오기!!!!!!!!
                    soc = (int) (Math.random() * 100);
                    graph_battery.soc = soc;

                    Handler mHandler1 = new Handler(Looper.getMainLooper());
                    mHandler1.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tv_w.setText(volt * amp + "W");
                            tv_w.invalidate();


                            if (soc <= 5) {
                                // 배터리가 5% 이하이면 LOW BAT 표시
                                tv_percent.setText("LOW%");
                                tv_percent.setTextColor(Color.RED);
                                tv_ready.setText("LOW BAT");
                                tv_ready.setTextColor(Color.RED);
                                graph_battery.soc = 2;
                                graph_battery.invalidate();
                            } else {
                                tv_percent.setText(soc + "%");
                                if (soc > 10) {
                                    tv_percent.setTextColor(Color.rgb(146, 208, 80));
                                } else {
                                    tv_percent.setTextColor(Color.RED);
                                }
                                tv_ready.setText("Ready");
                                tv_ready.setTextColor(Color.rgb(146, 208, 80));
                                graph_battery.invalidate();
                            }
                        }
                    }, 0);


                    try {
                        Thread.sleep(2000);  //2초에 한 번씩 화면에 W를 업데이트
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // 10초 단위로 로그를 저장함
                    if (saveFlagOfLog % 5 == 0) {  //TODO: Trip이 끝나는 기준 ? 디바이스에서 정보 받아오나?
                        Handler mHandler2 = new Handler(Looper.getMainLooper());
                        mHandler2.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //showSaveTripDialog(tripLogId, nowTime);
                                if (autoSave) {

                                    dbHelper.insert_TripLog(nowTime, volt, amp);
                                    String allLog = dbHelper.getLog();
                                    System.out.println(allLog);

                                    tripLogActivity.showCurrentTrip(dbHelper);
                                } // else : 저장하지 않고 넘어감
                            }
                        }, 0);
                    }
                    saveFlagOfLog += 1;

                    if (!btconnect) {

                        // 블루투스 연결이 끊어지면 트립을 저장하고 스레드를 끝냄.
                        saveTrip(tripId, nowTime);
                        break;
                    }
                }
            }
        }).start();
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

                saveTrip(tripId, nowTime);

            }
        });

        dig.setCancelable(false);
        dig.show();
    }


    private static void saveTrip(int tripId, String nowTime) {

//        if (tripName == null) { tripName = "Untitled"; }
        if (dbHelper.getAvgPwrW(tripId) == -999 || dbHelper.getUsedW(tripId) == -999 || dbHelper.getMaxW(tripId) == -2) return;
        dbHelper.update_TripSTATS(tripId, nowTime, dbHelper.getMaxW(tripId), dbHelper.getUsedW(tripId), (int)(totalDistance * 1000), dbHelper.getAvgPwrW(tripId));
        dbHelper.update_TripName(tripId, "Untitled");

        //Toast.makeText(ConsumptionActivity.this, "트립이 저장되었습니다.", Toast.LENGTH_SHORT).show();

        String allTrip = dbHelper.getTripSTATS();
        System.out.println(allTrip);


        // Trip 기록 개수 20개 넘으면 자동 삭제
        dbHelper.deleteGarbage();  //일단 찌꺼기 로그부터 삭제하고나서 개수 세기
        if (dbHelper.getProfileCount("TripSTATS") + 1 > 20) {
            dbHelper.deleteTrip();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (autoConnect) {
            btconnect = true;

            tv_ready.setText("Connect");
            tv_ready.setTextColor(Color.rgb(146, 208, 80));  //green

            startThread();
        }

        if (btconnect) {
            startThread();
        }
    }
}