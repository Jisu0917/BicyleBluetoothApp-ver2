package com.activerecycle.tripgauge.bluetooth;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.activerecycle.tripgauge.ConsumptionActivity.btconnect;
import static com.activerecycle.tripgauge.ConsumptionActivity.graph_battery;
import static com.activerecycle.tripgauge.ConsumptionActivity.tv_distance;
import static com.activerecycle.tripgauge.ConsumptionActivity.tv_percent;
import static com.activerecycle.tripgauge.ConsumptionActivity.tv_ready;
import static com.activerecycle.tripgauge.ConsumptionActivity.tv_w;
import static com.activerecycle.tripgauge.bluetooth.HM10ConnectionService.dbHelper;
import static com.activerecycle.tripgauge.bluetooth.HM10ConnectionService.saveTrip;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.activerecycle.tripgauge.ConsumptionActivity;
import com.activerecycle.tripgauge.SettingsActivity;
import com.activerecycle.tripgauge.TripLogActivity;

import java.time.LocalDate;
import java.util.ArrayList;


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
    static String pref_address = "";

    static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        setContentView(R.layout.activity_connection); // Set the content to a layout with list view

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

        btn_reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //BleScanServices.scanForDevices(true,mLeScanCallback);

                bluetoothScanner.startScan(scanPeriod);
            }
        });

        btn_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListOfScansActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        btn_trip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListOfScansActivity.this, TripLogActivity.class);
                startActivity(intent);
            }
        });

        // 블루투스 디바이스 스캔
        //BleScanServices.scanForDevices(true,mLeScanCallback);
        bluetoothScanner.startScan(scanPeriod);

        modulelist_layout = (LinearLayout) findViewById(R.id.modulelist_layout);



    }// end of OnCreate.

    public static void getDeviceListInfo(Context context) {
        if (deviceList != null) {
            System.out.println("deviceList : " + deviceList);

            setDeviceListView(context);
        }
    }

    @SuppressLint("MissingPermission")
    public static void setDeviceListView(Context context) {
        modulelist_layout.removeAllViews();

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        //LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (deviceList != null) {
            //TODO: Conneted 된 디바이스 상단 초록색 고정
            if (!preferences.getString("address", "").equals("") && !preferences.getString("name", "").equals("")) {
                String connectedAddress = preferences.getString("address", "");
                String connectedName = preferences.getString("name", "");
                Typeface typeface = Typeface.createFromAsset(context.getResources().getAssets(), "gmarket_sans_bold.ttf");

                View customView = layoutInflater.inflate(R.layout.row, null);
                ((LinearLayout) customView.findViewById(R.id.container)).setTag(connectedName + "#" + connectedAddress);
                ((TextView) customView.findViewById(R.id.text1)).setText(connectedName);
                ((TextView) customView.findViewById(R.id.text1)).setTypeface(typeface);

                //TODO: background_rounding_green
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

            //TODO: 나머지 스캔된 디바이스 목록 표시
            String deviceName, deviceAddress;
            for (BluetoothDevice device : deviceList) {
                deviceName = device.getName();
                deviceAddress = device.getAddress();
                if ( device.getAddress() != null && !device.getAddress().equals("")
                        && !device.getAddress().equals(preferences.getString("address", "")) ) {

                    Typeface typeface = Typeface.createFromAsset(context.getResources().getAssets(), "gmarket_sans_bold.ttf");

                    View customView = layoutInflater.inflate(R.layout.row, null);
                    ((LinearLayout) customView.findViewById(R.id.container)).setTag(deviceName + "#" + deviceAddress);
                    ((TextView) customView.findViewById(R.id.text1)).setText(deviceName);
                    ((TextView) customView.findViewById(R.id.text1)).setTypeface(typeface);

                    //TODO: background_rounding_white
                    ((LinearLayout) customView.findViewById(R.id.color_contianer)).setBackgroundResource(R.drawable.background_rounding_white);
                    ((TextView) customView.findViewById(R.id.text1)).setTextColor(Color.BLACK);
                    ((TextView) customView.findViewById(R.id.tv_connected)).setTextColor(Color.rgb(34, 177, 77));  //green
                    ((TextView) customView.findViewById(R.id.tv_connected)).setText("Available to connect");
                    ((TextView) customView.findViewById(R.id.tv_connected)).setTypeface(typeface);

                    modulelist_layout.addView(customView);


                    customView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

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

    public static void showDialog(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        ConsumptionActivity.btconnect = false;

                        HM10ConnectionService.m_bleConnectionService.disconnect();
                        Intent intent1 = new Intent(context, HM10ConnectionService.class);
                        context.stopService(intent1);
                        Intent intent2 = new Intent(context, BleConnectionService.class);
                        context.stopService(intent2);

                        LayoutInflater layoutInflater = LayoutInflater.from(context);
                        View customView = layoutInflater.inflate(R.layout.row, null);

                        ((LinearLayout) customView.findViewById(R.id.color_contianer)).setBackgroundResource(R.drawable.background_rounding_white);
                        ((TextView) customView.findViewById(R.id.text1)).setTextColor(Color.BLACK);
                        ((TextView) customView.findViewById(R.id.tv_connected)).setTextColor(Color.rgb(34, 177, 77));  //green
                        ((TextView) customView.findViewById(R.id.tv_connected)).setText("Available to connect");

                        //------------------------------------------------------------//

                        tv_percent.setTextColor(Color.WHITE);

                        if (settings_preferences.getBoolean("s2", true)) {
                            //TODO: 트립 저장
                            LocalDate currentDate = LocalDate.now();
                            String now = currentDate.toString();
                            String nowTime = now.replaceAll("-", ".");
                            saveTrip(HM10ConnectionService.tripId, nowTime);
                        } else {
                            //TODO: #init 으로 되어있는 트립 삭제
                            dbHelper.deleteGarbage();
                        }

                        // 블루투스 연결 안 된 상태이면
                        tv_ready.setText("Connect");
                        tv_ready.setTextColor(Color.rgb(255, 0, 0));  //red

                        tv_w.setText("0W");
                        tv_distance.setText("00.00 Km");

                        // 배터리
                        //soc = 0;
                        graph_battery.soc = 0;
                        graph_battery.invalidate();
                        tv_percent.setText("00%");


                        //TODO: 그래프 깜빡깜빡 거리는 애니메이션!
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (!btconnect) {
                                    ConsumptionActivity.graph_speed.speed = 99;
                                    ConsumptionActivity.graph_speed.invalidate();

                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    ConsumptionActivity.graph_speed.speed = 0;
                                    ConsumptionActivity.graph_speed.invalidate();

                                    // 1초에 한 번 깜빡임
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }).start();


                        SharedPreferences.Editor editor = preferences.edit();  //Editor를 preferences에 쓰겠다고 연결
                        if (settings_preferences.getBoolean("s3", true)) {
                            editor.putString("last_address", preferences.getString("address", ""));
                            editor.putString("last_name", preferences.getString("name", ""));
                        }
                        editor.putString("address", "");
                        editor.putString("name", "");
                        editor.commit();

                        bluetoothScanner.startScan(scanPeriod);
                        //getDeviceListInfo(context);
                        //setDeviceListView(context);
                    }
                });

        builder.setNegativeButton("취소", null);
        AlertDialog dialog = builder.show();

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
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//
//        Intent intent = new Intent(this, BleConnectionService.class);
//        stopService(intent);
//
//        SharedPreferences.Editor editor = preferences.edit();  //Editor를 preferences에 쓰겠다고 연결
//        editor.putString("address", "");
//        editor.putString("name", "");
//
//        editor.commit();  //항상 commit & apply 를 해주어야 저장이 된다.
//    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        SharedPreferences.Editor editor = preferences.edit();  //Editor를 preferences에 쓰겠다고 연결
//        editor.putString("address", pref_address);
//
//        editor.commit();  //항상 commit & apply 를 해주어야 저장이 된다.
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//
//        SharedPreferences.Editor editor = preferences.edit();  //Editor를 preferences에 쓰겠다고 연결
//        editor.putString("address", pref_address);
//
//        editor.commit();  //항상 commit & apply 를 해주어야 저장이 된다.
//    }
//


//    private void onClickDevice(View view) {
//
//        // 연결하기
//        LinearLayout container = (LinearLayout) view.findViewById(R.id.container);
//        String tag = (String) container.getTag();
//        String[] tags = tag.split(":");
//        String name = tags[0];
//        String address = tags[1];
//
//        // 임시, 확인용
//        Toast.makeText(context, "name: " + name + ", address: " + address, Toast.LENGTH_SHORT).show();
//
//        final Intent intent = new Intent(context, HM10CommunicationActivity.class);
//        intent.putExtra(StaticResources.EXTRAS_DEVICE_NAME, name);
//        intent.putExtra(StaticResources.EXTRAS_DEVICE_ADDRESS, address);
//        startActivity(intent);
//
//
//        //-----------------------------------------------------------------------------//
//        // 연결이 확인되면
//        TextView textView1 = (TextView) view.findViewById(R.id.text1);
//        TextView connected = (TextView) view.findViewById(R.id.tv_connected);
//        if (connected.getText().toString().toLowerCase(Locale.ROOT).equals("available to connect")) {
//
//            container.setBackgroundResource(R.drawable.background_rounding_green);
//            textView1.setTextColor(Color.WHITE);
//            connected.setTextColor(Color.WHITE);
//            connected.setText("Connected");
//
//            ConsumptionActivity.btconnect = true;
//            tv_ready.setText("Connect");
//            tv_ready.setTextColor(Color.rgb(34, 177, 77));  //green
//
//            startThread();
//
//
//        } else if (connected.getText().toString().toLowerCase(Locale.ROOT).equals("connected")) {
//            container.setBackgroundResource(R.drawable.background_rounding_white);
//            textView1.setTextColor(Color.BLACK);
//            connected.setTextColor(Color.rgb(34, 177, 77));  //green
//            connected.setText("Available to connect");
//
//            ConsumptionActivity.btconnect = false;
//            tv_ready.setText("Ready");
//            tv_ready.setTextColor(Color.rgb(255, 0, 0));  //red
//
//        }
//    }
//
//
//    public void refresh_Button_Click(View v) {
//
//    }
}
