package com.activerecycle.tripgauge.bluetooth;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.activerecycle.tripgauge.ConsumptionActivity.btconnect;
import static com.activerecycle.tripgauge.ConsumptionActivity.graph_battery;
import static com.activerecycle.tripgauge.ConsumptionActivity.graph_speed;
import static com.activerecycle.tripgauge.ConsumptionActivity.totalDistance;
import static com.activerecycle.tripgauge.ConsumptionActivity.tripADistance;
import static com.activerecycle.tripgauge.ConsumptionActivity.tripBDistance;
import static com.activerecycle.tripgauge.ConsumptionActivity.tripOnceDistance;
import static com.activerecycle.tripgauge.ConsumptionActivity.tv_distance;
import static com.activerecycle.tripgauge.ConsumptionActivity.tv_percent;
import static com.activerecycle.tripgauge.ConsumptionActivity.tv_ready;
import static com.activerecycle.tripgauge.ConsumptionActivity.tv_w;
import static com.activerecycle.tripgauge.bluetooth.HM10ConnectionService.dbHelper;
import static com.activerecycle.tripgauge.bluetooth.HM10ConnectionService.odo_preferences;
import static com.activerecycle.tripgauge.bluetooth.HM10ConnectionService.saveTrip;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.activerecycle.tripgauge.ConsumptionActivity;
import com.activerecycle.tripgauge.MyAnimation;
import com.activerecycle.tripgauge.SettingsActivity;
import com.activerecycle.tripgauge.SpeedGraph;
import com.activerecycle.tripgauge.TripLogActivity;

import java.time.LocalDate;
import java.util.ArrayList;


// Connection 페이지
public class ListOfScansActivity extends AppCompatActivity {

    //----------------------------------------------//

    // BluetoothScanner 인스턴스 생성
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    static BluetoothScanner bluetoothScanner;

    // Bluetooth LE 스캔 시작 및 지정된 시간 후 중지
    static long scanPeriod = 2000; // 2초 동안 스캔

    //---------------------------------------------//

    public BleScanServices m_bleScanServices;

    static LinearLayout modulelist_layout;
    public static ArrayList<BluetoothDevice> deviceList;

    ImageButton btn_menu, btn_reload;
    Button btn_settings, btn_trip;

    static SharedPreferences preferences, settings_preferences;

    static Context mContext;


    @Override
    public void finish() {
        super.finish();
        // 기본 애니메이션 없애기
        overridePendingTransition(0, 0); //0 for no animation
        //MyAnimation 클래스 이용해 페이지 사라질 때 전체 Layout에 fadeOut 애니매이션 줌
        MyAnimation.fadeOut(findViewById(R.id.content), 500);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection); // Set the content to a layout with list view

        //MyAnimation 클래스 이용해 페이지 나타날 때 전체 Layout에 fadeIn 애니매이션 줌
        MyAnimation.fadeIn(findViewById(R.id.content), 500);

        mContext = ListOfScansActivity.this;

        preferences = getSharedPreferences("Device Info", MODE_PRIVATE);

        settings_preferences = getSharedPreferences("Setting Info", MODE_PRIVATE);

        bluetoothScanner = new BluetoothScanner(bluetoothAdapter, getApplicationContext());

        CheckBleHardware();

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        ActivityCompat.requestPermissions(this, permissions, 2);
        // This will pop up the first time the app is run, to give the app permissions not already given.
        m_bleScanServices = new BleScanServices(this);
        m_bleScanServices.checkBluetoothEnabled(this);


        btn_menu = (ImageButton) findViewById(R.id.btn_menu);
        btn_reload = (ImageButton) findViewById(R.id.btn_reload);
        btn_settings = (Button) findViewById(R.id.btn_settings);
        btn_trip = (Button) findViewById(R.id.btn_trip);

        btn_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        // 새로고침 이미지 버튼에 회전 애니매이션 추가
        final Animation rotation =
                AnimationUtils.loadAnimation(ListOfScansActivity.this, R.anim.rotate);

        btn_reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothScanner.startScan(scanPeriod);
                view.startAnimation(rotation);
            }
        });

        btn_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListOfScansActivity.this, SettingsActivity.class);
                startActivity(intent);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);  // 액티비티를 띄울 때 애니메이션 없애기
                overridePendingTransition(0, 0); //0 for no animation
            }
        });

        btn_trip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListOfScansActivity.this, TripLogActivity.class);
                startActivity(intent);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);  // 액티비티를 띄울 때 애니메이션 없애기
                overridePendingTransition(0, 0); //0 for no animation
            }
        });

        // 블루투스 디바이스 스캔
        bluetoothScanner.startScan(scanPeriod);

        modulelist_layout = (LinearLayout) findViewById(R.id.modulelist_layout);

    }// end of OnCreate.

    // 디바이스 정보를 가져오는 함수
    public static void getDeviceListInfo(Context context) {
        if (deviceList != null) {
            System.out.println("deviceList : " + deviceList);

            setDeviceListView(context);
        }
    }

    // 가져온 디바이스 정보를 토대로 레이아웃을 형성하는 함수
    @SuppressLint("MissingPermission")
    public static void setDeviceListView(Context context) {
        modulelist_layout.removeAllViews();

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        //LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (deviceList != null) {
            // Conneted 된 디바이스 상단 초록색 고정
            if (!preferences.getString("address", "").equals("") && !preferences.getString("name", "").equals("")) {
                String connectedAddress = preferences.getString("address", "");
                String connectedName = preferences.getString("name", "");
                //글꼴 설정
                Typeface typeface = Typeface.createFromAsset(context.getResources().getAssets(), "gmarket_sans_bold.ttf");

                View customView = layoutInflater.inflate(R.layout.row, null);
                //((LinearLayout) customView.findViewById(R.id.container)).setTag(connectedName + "#" + connectedAddress);
                ((TextView) customView.findViewById(R.id.text1)).setText(connectedName);
                ((TextView) customView.findViewById(R.id.text1)).setTypeface(typeface);

                // background_rounding_green
                ((LinearLayout) customView.findViewById(R.id.color_contianer)).setBackgroundResource(R.drawable.background_rounding_green);
                ((TextView) customView.findViewById(R.id.text1)).setTextColor(Color.WHITE);
                ((TextView) customView.findViewById(R.id.tv_connected)).setTextColor(Color.WHITE);
                ((TextView) customView.findViewById(R.id.tv_connected)).setText("Connected");
                ((TextView) customView.findViewById(R.id.tv_connected)).setTypeface(typeface);


                modulelist_layout.addView(customView);

                customView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showDialog(mContext, "Are you sure want to Disconnect?", "확인을 누르면 블루투스 연결이 해제됩니다.");
                    }
                });
            }

            // 나머지 스캔된 디바이스 목록 표시
            String deviceName, deviceAddress;
            for (BluetoothDevice device : deviceList) {
                deviceName = device.getName();
                deviceAddress = device.getAddress();
                if (device.getAddress() != null && !device.getAddress().equals("")
                        && !device.getAddress().equals(preferences.getString("address", ""))) {
                    // 글꼴 설정
                    Typeface typeface = Typeface.createFromAsset(context.getResources().getAssets(), "gmarket_sans_bold.ttf");

                    View customView = layoutInflater.inflate(R.layout.row, null);
                    //((LinearLayout) customView.findViewById(R.id.container)).setTag(deviceName + "#" + deviceAddress);
                    ((TextView) customView.findViewById(R.id.text1)).setText(deviceName);
                    ((TextView) customView.findViewById(R.id.text1)).setTypeface(typeface);

                    // background_rounding_white
                    ((LinearLayout) customView.findViewById(R.id.color_contianer)).setBackgroundResource(R.drawable.background_rounding_white);
                    ((TextView) customView.findViewById(R.id.text1)).setTextColor(Color.BLACK);
                    ((TextView) customView.findViewById(R.id.tv_connected)).setTextColor(Color.parseColor("#4CAF50"));  //green
                    ((TextView) customView.findViewById(R.id.tv_connected)).setText("Available to connect");
                    ((TextView) customView.findViewById(R.id.tv_connected)).setTypeface(typeface);

                    modulelist_layout.addView(customView);

                    // 클릭 이벤트
                    customView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // 스캔된 디바이스 목록에서 클릭시 HM10ConnectionService 서비스가 백그라운드에서 실행되며
                            // 해당 기기의 이름, 주소가 전달되고
                            // 블루투스 연결을 시도한다.
                            final Intent intent = new Intent(context, HM10ConnectionService.class);
                            intent.putExtra(StaticResources.EXTRAS_DEVICE_NAME, device.getName());
                            intent.putExtra(StaticResources.EXTRAS_DEVICE_ADDRESS, device.getAddress());
                            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                            context.startService(intent);
                        }
                    });
                }
            }

        } else {
            System.out.println("deviceList is null...");
        }
    }

    // 다이얼로그를 화면에 띄우는 함수 - 블루투스 연결 해제할지 확인할 때 쓰인다.
    public static void showDialog(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // 확인을 누르면 블루투스 연결이 해제된다.
                // 블루투스 연결 해제 작업 ...

                ConsumptionActivity.btconnect = false;

                try {
                    //디바이스 목록 새로고침
                    bluetoothScanner.startScan(scanPeriod);

                    SharedPreferences.Editor editor = preferences.edit();  //Editor를 preferences에 쓰겠다고 연결
                    if (settings_preferences.getBoolean("s3", true)) {
                        editor.putString("last_address", preferences.getString("address", ""));
                        editor.putString("last_name", preferences.getString("name", ""));
                    }
                    editor.putString("address", "");
                    editor.putString("name", "");
                    editor.commit();

                    // - Exception
                    // 세 가지 주행 거리 정보를 저장하고 이번 주행에 대한 거리 변수를 0으로 초기화한다.
                    SharedPreferences.Editor editor1 = odo_preferences.edit();
                    editor1.putFloat("ODO", (float) totalDistance);
                    editor1.putFloat("TRIPA", (float) tripADistance);
                    editor1.putFloat("TRIPB", (float) tripBDistance);
                    editor1.commit();
                    tripOnceDistance = 0;

                    //disconnect() - Exception
                    HM10ConnectionService.m_bleConnectionService.disconnect();
                    Intent intent1 = new Intent(context, HM10ConnectionService.class);
                    context.stopService(intent1);
                    Intent intent2 = new Intent(context, BleConnectionService.class);
                    context.stopService(intent2);

                } catch (Exception e) {
                    e.printStackTrace();

                    return;
                }
                //------------------------------------------------------------//

                tv_percent.setTextColor(Color.WHITE);

                if (settings_preferences.getBoolean("s2", true)) {
                    // 트립 저장
                    LocalDate currentDate = LocalDate.now();
                    String now = currentDate.toString();
                    String nowTime = now.replaceAll("-", ".");
                    saveTrip(HM10ConnectionService.tripId, nowTime);
                } else {
                    // 트립 제목 #init 으로 되어있는 트립 삭제
                    dbHelper.deleteGarbage();
                }

                // 블루투스 연결 안 된 상태이면
                tv_ready.setText("Connect");
                tv_ready.setTextColor(Color.rgb(255, 0, 0));  //red

                tv_w.setText("0W");
                tv_distance.setText("00.00");

                // 배터리
                //soc = 0;
                graph_battery.soc = 0;
                graph_battery.invalidate();
                tv_percent.setText("00%");


                // 그래프 깜빡깜빡 거리는 애니메이션!
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        graph_speed.cancelAnimation();
                        while (!btconnect) {
                            SpeedGraph.previousSpeed = 0;
                            SpeedGraph.speed = 99;
                            graph_speed.invalidate();

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            SpeedGraph.previousSpeed = 99;
                            SpeedGraph.speed = 0;
                            graph_speed.invalidate();

                            // 1초에 한 번 깜빡임
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }
        });

        builder.setNegativeButton("취소", null);  // 취소 누르면 아무 작업도 안 함.
        AlertDialog dialog = builder.show();

        // 다이얼로그 메시지에 글꼴 지정
        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        Typeface typeface = Typeface.createFromAsset(context.getResources().getAssets(), "gmarket_sans_medium.ttf");
        textView.setTypeface(typeface);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main, menu); // Fills three dots with items.
        return true;
    }

    private void CheckBleHardware() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bluetoothScanner.startScan(scanPeriod);
        //BleScanServices.scanForDevices(true,mLeScanCallback);

        //MyAnimation 클래스 이용해 페이지 나타날 때 전체 Layout에 fadeIn 애니매이션 줌
        MyAnimation.fadeIn(findViewById(R.id.content), 500);
    }
}
