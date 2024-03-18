package com.activerecycle.tripgauge.bluetooth;

import static com.activerecycle.tripgauge.ConsumptionActivity.autoSave;
import static com.activerecycle.tripgauge.ConsumptionActivity.graph_battery;
import static com.activerecycle.tripgauge.ConsumptionActivity.soc;
import static com.activerecycle.tripgauge.ConsumptionActivity.startThread;
import static com.activerecycle.tripgauge.ConsumptionActivity.tv_distance;
import static com.activerecycle.tripgauge.ConsumptionActivity.tv_percent;
import static com.activerecycle.tripgauge.ConsumptionActivity.tv_ready;
import static com.activerecycle.tripgauge.ConsumptionActivity.tv_w;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.activerecycle.tripgauge.BeepService;
import com.activerecycle.tripgauge.ConsumptionActivity;
import com.activerecycle.tripgauge.DBHelper;
import com.activerecycle.tripgauge.SettingsActivity;
import com.activerecycle.tripgauge.TripLogActivity;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * I need to track down where I got this code base from. If from Google Android, it has Apache 2.0 license.
 */

public class HM10CommunicationActivity extends AppCompatActivity {

    private TextView tv_what_do_u_saying;
    private TextView mHasSerial;
    private TextView textSerialConnection;
    private String mDeviceName;
    private String m_deviceAddress;
    private BleConnectionService m_bleConnectionService;
    private boolean m_hasSerial;
    private boolean mConnected = false;
    private BluetoothGattCharacteristic characteristicTX;
    private BluetoothGattCharacteristic characteristicRX;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    static DBHelper dbHelper;
    static int tripId;

    TripLogActivity tripLogActivity;
    
    private int countFlag;
    int volt, amp , soc;

    public static SharedPreferences settings_preferences, device_preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_hm10_communication);
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(StaticResources.EXTRAS_DEVICE_NAME);
        m_deviceAddress = intent.getStringExtra(StaticResources.EXTRAS_DEVICE_ADDRESS);
        m_bleConnectionService = new BleConnectionService(this, m_deviceAddress);

//        Toolbar toolbar = findViewById(R.id.app_bar_hm10_communication); // declare the toolbar.
//        TextView textDeviceName = toolbar.findViewById(R.id.toolbar_device_name);
//        TextView textDeviceAddress = toolbar.findViewById(R.id.toolbar_device_address);

//        textDeviceName.setText(mDeviceName);
//        textDeviceAddress.setText(m_deviceAddress);
//
//        setSupportActionBar(toolbar); // Make toolbar visible on activity. Xml is very minimalistic
//        getSupportActionBar().setDisplayShowTitleEnabled(false);
//
//        textSerialConnection = findViewById(R.id.bluetooth_serial_cxn_value);
//        textSerialConnection.setText(StaticResources.CONNECTION_STATE_CONNECTING);
        m_hasSerial = false;

        IntentFilter filterMaster =new IntentFilter();
        filterMaster.addAction(StaticResources.BROADCAST_NAME_CONNECTION_UPDATE);
        filterMaster.addAction(StaticResources.BROADCAST_NAME_SERVICES_DISCOVERED);
        filterMaster.addAction(StaticResources.BROADCAST_NAME_TX_CHARATERISTIC_CHANGED);
        registerReceiver(m_bleBroadcastReceiver, filterMaster);



        //-------------------------------------------//


        tripLogActivity = new TripLogActivity();

        dbHelper = new DBHelper(HM10CommunicationActivity.this, 1);

//        tv_what_do_u_saying = (TextView) findViewById(R.id.tv_what_do_u_saying);


        settings_preferences = getSharedPreferences("Setting Info", MODE_PRIVATE);
        device_preferences = getSharedPreferences("Device Info", MODE_PRIVATE);

    }


    public void communication_Button_Click(View v)
    {
        m_bleConnectionService.writeToBluetoothSerial(StaticResources.COMMUNICATION_ANDROID_TO_HM10);

    }
    public void connect_Button_Click(View v)
    {
        textSerialConnection.setText(StaticResources.CONNECTION_STATE_CONNECTING);
        m_bleConnectionService.connect(m_deviceAddress);

    }

    private final BroadcastReceiver m_bleBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String broadcastName = intent.getAction();
            Log.i("Broadcast Receiver",
                    "Recieved Broadcast name = " + broadcastName
            );
            switch(broadcastName)
            {
                case StaticResources.BROADCAST_NAME_CONNECTION_UPDATE:
                    final String connection = intent.getStringExtra(StaticResources.EXTRAS_CONNECTION_STATE);
//                    textSerialConnection.setText(connection);
//                    tv_what_do_u_saying.setText(connection);
                    // connection : EXTRAS_CONNECTION_STATE
                    //TODO : 연결되었을 때! connection = CONNECTION_STATE_CONNECTED = "Connected
                    // 또는 연결 중일 때! connection = CONNECTION_STATE_CONNECTING = "Connecting"
                    // 또는 연결이 끊겼을 때! connection = CONNECTION_STATE_DISCONNECTED = "Disconnected"

                    //TODO : 연결되었을 때!
                    if (connection.equals(StaticResources.CONNECTION_STATE_CONNECTED)) {
                        SharedPreferences.Editor editor = device_preferences.edit();  //Editor를 preferences에 쓰겠다고 연결
                        editor.putString("address", m_deviceAddress);
                        editor.putString("name", mDeviceName);
                        editor.commit();

                        LayoutInflater layoutInflater = LayoutInflater.from(context);
                        View customView = layoutInflater.inflate(R.layout.row, null);

                        ((LinearLayout) customView.findViewById(R.id.color_contianer)).setBackgroundResource(R.drawable.background_rounding_green);
                        ((TextView) customView.findViewById(R.id.text1)).setTextColor(Color.WHITE);
                        ((TextView) customView.findViewById(R.id.tv_connected)).setTextColor(Color.WHITE);
                        ((TextView) customView.findViewById(R.id.tv_connected)).setText("Connected");

                        ConsumptionActivity.btconnect = true;
                        tv_ready.setText("Connect");
                        tv_ready.setTextColor(Color.rgb(34, 177, 77));  //green

                        startThread();

                        customView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // 연결 해제하기

                            }
                        });
                    }


                    //TODO : 연결이 끊어졌을 때
                    // 1) DB에 트립 저장
                    // 2) 원호 그래프 깜빡이는 애니메이션, 배터리 00%
                    else if (connection.equals(StaticResources.CONNECTION_STATE_DISCONNECTED)) {
                        tv_percent.setTextColor(Color.WHITE);

                        LocalDate currentDate = LocalDate.now();
                        String nowTime = currentDate.toString();
                        saveTrip(tripId, nowTime);

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
                                while (true) {
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

                                    if (connection.equals(StaticResources.CONNECTION_STATE_CONNECTED)) {
                                        break;
                                    }
                                }
                            }
                        }).start();


                        LayoutInflater layoutInflater = LayoutInflater.from(context);
                        View customView = layoutInflater.inflate(R.layout.row, null);

                        SharedPreferences.Editor editor = device_preferences.edit();  //Editor를 preferences에 쓰겠다고 연결
                        editor.putString("address", "");
                        editor.putString("name", "");
                        editor.commit();

                        ((LinearLayout) customView.findViewById(R.id.color_contianer)).setBackgroundResource(R.drawable.background_rounding_white);
                        ((TextView) customView.findViewById(R.id.text1)).setTextColor(Color.BLACK);
                        ((TextView) customView.findViewById(R.id.tv_connected)).setTextColor(Color.rgb(34, 177, 77));  //green
                        ((TextView) customView.findViewById(R.id.tv_connected)).setText("Available to connect");

                        ConsumptionActivity.btconnect = false;
                        tv_ready.setText("Ready");
                        tv_ready.setTextColor(Color.rgb(255, 0, 0));  //red
                    }

                    break;
                case StaticResources.BROADCAST_NAME_SERVICES_DISCOVERED:
                    final String serial = intent.getStringExtra(StaticResources.EXTRAS_SERVICES_DISCOVERED);
                    textSerialConnection.setText(serial);
                    if (serial == StaticResources.SERVICES_DISCOVERY_CHARACTERISTIC_SUCCESS)
                    {
                        m_hasSerial = true;
                    }
                    // serial : "Communication Characteristic Found"
                    //TODO : 브로드캐스트 서비스를 찾았을 때!
                    ConsumptionActivity.btconnect = true;
                    //TODO : -- DB - TripSTATS 테이블 row 하나 생성
                    tripId = dbHelper.init_TripSTATS();
                    countFlag = 0;

                    volt = 0; amp = 0; soc = 10;

                    tv_ready.setText("Ready");
                    tv_ready.setTextColor(Color.rgb(146, 208, 80));  //green

                    


                    break;
                case StaticResources.BROADCAST_NAME_TX_CHARATERISTIC_CHANGED:
                    final String txData = intent.getStringExtra(StaticResources.EXTRAS_TX_DATA);
//                    final String txData = "Tx data received from HM10";
                    Toast.makeText(context, txData, Toast.LENGTH_SHORT).show();
                    Log.i("Broadcast Received",
                            "TxData = " + txData + ";");
//                    tv_what_do_u_saying.setText(txData);

                    // txData : v=00/a=00/s=00
                    try {
                        String voltStr = txData.split("/")[0];
                        String v = voltStr.split("=")[0];
                        if (v.equals("v")) {
                            volt = Integer.parseInt(voltStr.split("=")[1]);
                        }
                        String ampStr = txData.split("/")[1];
                        String a = ampStr.split("=")[0];
                        if (a.equals("a")) {
                            amp = Integer.parseInt(voltStr.split("=")[1]);
                        }
                        String socStr = txData.split("/")[2];
                        String s = socStr.split("=")[0];
                        if (s.equals("s")) {
                            soc = Integer.parseInt(voltStr.split("=")[1]);
                        }

                        //TODO: 배터리 5% 이하 경고음
                        if (soc <= 5 && settings_preferences.getBoolean("s4", true)) {//SettingsActivity.socFlag &&
                            startService(new Intent(getApplicationContext(), BeepService.class));
                        }
                        tv_w.setText(volt * amp + "W");
                        tv_w.invalidate();

                        if (soc <= 5) {
                            // 배터리가 5% 이하이면 LOW BAT 표시
                            tv_percent.setText("LOW%");
                            tv_percent.setTextColor(Color.RED);
                            tv_ready.setText("LOW BAT");
                            tv_ready.setTextColor(Color.RED);
                            graph_battery.soc = 3;
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
                            graph_battery.soc = soc;
                            graph_battery.invalidate();
                        }
                        
                        if (autoSave) {
                            countFlag++;
                            if (countFlag % 5 == 0) {  //5번마다 (10초 단위로) 로그를 저장함
                                LocalDate currentDate = LocalDate.now();
                                String nowTime = currentDate.toString();
                                dbHelper.insert_TripLog(nowTime, volt, amp);
                                //------------------확인을 위한 출력 코드-------------//
                                String allLog = dbHelper.getLog();
                                System.out.println(allLog);
                                //------------------확인을 위한 출력 코드-------------//

                                tripLogActivity.showCurrentTrip(dbHelper);  //실시간 그래프 보여주기
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("printStackTrace - txData: " + txData);
                    }
                    break;
            }
        }
    };


    private static void saveTrip(int tripId, String nowTime) {

//        if (tripName == null) { tripName = "Untitled"; }
        if (dbHelper.getAvgPwrW(tripId) == -999 || dbHelper.getUsedW(tripId) == -999 || dbHelper.getMaxW(tripId) == -2) return;
        dbHelper.update_TripSTATS(tripId, nowTime, dbHelper.getMaxW(tripId), dbHelper.getUsedW(tripId), (int)(ConsumptionActivity.totalDistance * 1000), dbHelper.getAvgPwrW(tripId));
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






}
