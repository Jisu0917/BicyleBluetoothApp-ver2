package com.activerecycle.tripgauge.bluetooth;

import static com.activerecycle.tripgauge.ConsumptionActivity.autoSave;
import static com.activerecycle.tripgauge.ConsumptionActivity.blinkThread;
import static com.activerecycle.tripgauge.ConsumptionActivity.btconnect;
import static com.activerecycle.tripgauge.ConsumptionActivity.graph_battery;
import static com.activerecycle.tripgauge.ConsumptionActivity.speed;
import static com.activerecycle.tripgauge.ConsumptionActivity.tv_KPH;
import static com.activerecycle.tripgauge.ConsumptionActivity.tv_distance;
import static com.activerecycle.tripgauge.ConsumptionActivity.tv_percent;
import static com.activerecycle.tripgauge.ConsumptionActivity.tv_ready;
import static com.activerecycle.tripgauge.ConsumptionActivity.tv_speed;
import static com.activerecycle.tripgauge.ConsumptionActivity.tv_w;
import static com.activerecycle.tripgauge.bluetooth.ListOfScansActivity.bluetoothScanner;
import static com.activerecycle.tripgauge.bluetooth.ListOfScansActivity.mContext;
import static com.activerecycle.tripgauge.bluetooth.ListOfScansActivity.scanPeriod;
import static com.activerecycle.tripgauge.bluetooth.ListOfScansActivity.showDialog;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.activerecycle.tripgauge.BeepService;
import com.activerecycle.tripgauge.ConsumptionActivity;
import com.activerecycle.tripgauge.DBHelper;
import com.activerecycle.tripgauge.TripLogActivity;

import java.time.LocalDate;

public class HM10ConnectionService extends Service {

    private static final String TAG = "HM10ConnectionService";

    private String mDeviceName;
    private String m_deviceAddress;
    public static BleConnectionService m_bleConnectionService;
    private boolean m_hasSerial;

    static DBHelper dbHelper;
    public static int tripId;

    TripLogActivity tripLogActivity;

    public static boolean btStartFlag;
    private int countFlag;
    int volt, amp , soc;

    public static SharedPreferences settings_preferences, device_preferences;

    final int STOP_COUNT_DOWN = 10; //600000 나누기 2000 = 300
    int stopCountDown = STOP_COUNT_DOWN;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        // 여기에서 필요한 초기화 작업을 수행합니다.
        tripLogActivity = new TripLogActivity();

        dbHelper = new DBHelper(HM10ConnectionService.this, 1);

        settings_preferences = getSharedPreferences("Setting Info", MODE_PRIVATE);
        device_preferences = getSharedPreferences("Device Info", MODE_PRIVATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        // 여기에서 백그라운드 작업을 수행합니다.
        mDeviceName = intent.getStringExtra(StaticResources.EXTRAS_DEVICE_NAME);
        m_deviceAddress = intent.getStringExtra(StaticResources.EXTRAS_DEVICE_ADDRESS);
        System.out.println("mDeviceName: " + mDeviceName + ", m_deviceAddress: " + m_deviceAddress);
        m_bleConnectionService = new BleConnectionService(this, m_deviceAddress);

        m_hasSerial = false;

        IntentFilter filterMaster =new IntentFilter();
        filterMaster.addAction(StaticResources.BROADCAST_NAME_CONNECTION_UPDATE);
        filterMaster.addAction(StaticResources.BROADCAST_NAME_SERVICES_DISCOVERED);
        filterMaster.addAction(StaticResources.BROADCAST_NAME_TX_CHARATERISTIC_CHANGED);
        registerReceiver(m_bleBroadcastReceiver, filterMaster);

        // 작업이 완료되면 stopSelf()를 호출하여 서비스를 종료할 수 있습니다.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
        // 리시버 해제
        if (m_bleBroadcastReceiver != null) {
            unregisterReceiver(m_bleBroadcastReceiver);
            m_bleBroadcastReceiver = null;
            System.out.println("m_bleBroadcastReceiver 해제");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    private BroadcastReceiver m_bleBroadcastReceiver = new BroadcastReceiver() {
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

                    }


                    //TODO : 연결이 끊어졌을 때
                    // 1) DB에 트립 저장
                    // 2) 원호 그래프 깜빡이는 애니메이션, 배터리 00%
                    else if (connection.equals(StaticResources.CONNECTION_STATE_DISCONNECTED)) {
                        tv_percent.setTextColor(Color.WHITE);

                        LocalDate currentDate = LocalDate.now();
                        String now = currentDate.toString();
                        String nowTime = now.replaceAll("-", ".");
                        saveTrip(tripId, nowTime);

                        // 블루투스 연결 안 된 상태이면
                        tv_ready.setText("Connect");
                        tv_ready.setTextColor(Color.rgb(255, 0, 0));  //red

                        tv_w.setText("0W");
                        tv_distance.setText("00.00");

                        // 배터리
                        soc = 0;
                        graph_battery.soc = soc;
                        graph_battery.invalidate();
                        tv_percent.setText("00%");

                        tv_speed.setText("0");
                        tv_speed.setTextColor(Color.WHITE);
                        tv_KPH.setTextColor(Color.WHITE);
                        tv_distance.setText("00.00");


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
                        if (settings_preferences.getBoolean("s3", true)) {
                            editor.putString("last_address", m_deviceAddress);
                            editor.putString("last_name", mDeviceName);
                        }
                        editor.putString("address", "");
                        editor.putString("name", "");
                        editor.commit();



                        ListOfScansActivity.setDeviceListView(context);

                        ((LinearLayout) customView.findViewById(R.id.color_contianer)).setBackgroundResource(R.drawable.background_rounding_white);
                        ((TextView) customView.findViewById(R.id.text1)).setTextColor(Color.BLACK);
                        ((TextView) customView.findViewById(R.id.tv_connected)).setTextColor(Color.rgb(34, 177, 77));  //green
                        ((TextView) customView.findViewById(R.id.tv_connected)).setText("Available to connect");

                        ConsumptionActivity.btconnect = false;
                        tv_ready.setText("Connect");
                        tv_ready.setTextColor(Color.rgb(255, 0, 0));  //red

                        btStartFlag = false;

                        //stopSelf();
                    }
                    break;

                case StaticResources.BROADCAST_NAME_SERVICES_DISCOVERED:
                    final String serial = intent.getStringExtra(StaticResources.EXTRAS_SERVICES_DISCOVERED);
//                    textSerialConnection.setText(serial);
                    if (serial == StaticResources.SERVICES_DISCOVERY_CHARACTERISTIC_SUCCESS)
                    {
                        m_hasSerial = true;
                    }
                    // serial : "Communication Characteristic Found"
                    //TODO : 브로드캐스트 서비스를 찾았을 때!
                    ConsumptionActivity.btconnect = true;
                    blinkThread.interrupt();

                    // "A" 문자 보내기 - 연결 확인!
                    m_bleConnectionService.writeToBluetoothSerial(StaticResources.COMMUNICATION_ANDROID_TO_HM10);

                    SharedPreferences.Editor editor = device_preferences.edit();  //Editor를 preferences에 쓰겠다고 연결
                    editor.putString("address", m_deviceAddress);
                    editor.putString("name", mDeviceName);
                    editor.commit();

                    tv_ready.setText("Ready");
                    tv_ready.setTextColor(Color.rgb(146, 208, 80));  //green

                    try {
                        ListOfScansActivity.setDeviceListView(context);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    //TODO : -- DB - TripSTATS 테이블 row 하나 생성
                    tripId = dbHelper.init_TripSTATS();
                    countFlag = 0;

                    volt = 0;
                    amp = 0;
                    soc = 10;
                    stopCountDown = STOP_COUNT_DOWN;


                    break;
                case StaticResources.BROADCAST_NAME_TX_CHARATERISTIC_CHANGED:
                    final String txData = intent.getStringExtra(StaticResources.EXTRAS_TX_DATA);
//                    final String txData = "Tx data received from HM10";
//                    Toast.makeText(context, txData, Toast.LENGTH_SHORT).show();
                    Log.i("Broadcast Received",
                            "TxData = " + txData + ";");
//                    tv_what_do_u_saying.setText(txData);

                    // txData : v=00/a=00/s=00
                    if (btStartFlag) {
                        try {
                            String voltStr = txData.split("/")[0];
                            String v = voltStr.split("=")[0];
                            if (v.equals("v")) {
                                volt = Integer.parseInt(voltStr.split("=")[1]);
                            }
                            String ampStr = txData.split("/")[1];
                            String a = ampStr.split("=")[0];
                            if (a.equals("a")) {
                                amp = Integer.parseInt(ampStr.split("=")[1]);
                            }
                            String socStr = txData.split("/")[2];
                            String s = socStr.split("=")[0];
                            if (s.equals("s")) {
                                soc = Integer.parseInt(socStr.split("=")[1]);
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
                                    String now = currentDate.toString();
                                    String nowTime = now.replaceAll("-", ".");
                                    dbHelper.insert_TripLog(nowTime, volt, amp);
                                    //------------------확인을 위한 출력 코드-------------//
                                    String allLog = dbHelper.getLog();
                                    System.out.println(allLog);
                                    //------------------확인을 위한 출력 코드-------------//


                                    tripLogActivity.showCurrentTrip(dbHelper);  //실시간 그래프 보여주기
                                }
                                if (speed > 0) {
                                    stopCountDown = STOP_COUNT_DOWN;
                                } else if (speed == 0) {
                                    /**
                                     *  속도값이 0인 상태로 10분 이상 유지되면 블루투스 연결 해제
                                     * */
                                    stopCountDown--;
                                    System.out.println("stopCountDown: " + stopCountDown);
                                    if (stopCountDown < 0) {
                                        //TODO: 블루투스 연결 해제
                                        Toast.makeText(context, "10분 이상 주행이 없어 블루투스 연결이 해제되었습니다.", Toast.LENGTH_SHORT).show();
                                        System.out.println("stopCountDown == 0 !!!!!!");

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
                                        tv_distance.setText("00.00");

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


                                        editor = device_preferences.edit();  //Editor를 preferences에 쓰겠다고 연결
                                        if (settings_preferences.getBoolean("s3", true)) {
                                            editor.putString("last_address", device_preferences.getString("address", ""));
                                            editor.putString("last_name", device_preferences.getString("name", ""));
                                        }
                                        editor.putString("address", "");
                                        editor.putString("name", "");
                                        editor.commit();

                                        bluetoothScanner.startScan(scanPeriod);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("printStackTrace - txData: " + txData);
                        }
                    }
                    break;
            }
        }
    };


    public static void saveTrip(int tripId, String nowTime) {

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