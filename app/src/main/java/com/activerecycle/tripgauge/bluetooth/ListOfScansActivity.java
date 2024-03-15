package com.activerecycle.tripgauge.bluetooth;

import static android.bluetooth.BluetoothDevice.BOND_BONDED;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.activerecycle.tripgauge.ConsumptionActivity.startThread;
import static com.activerecycle.tripgauge.ConsumptionActivity.tv_ready;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.activerecycle.tripgauge.ConnectionActivity;
import com.activerecycle.tripgauge.ConsumptionActivity;
import com.activerecycle.tripgauge.SettingsActivity;
import com.activerecycle.tripgauge.TripLogActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;


public class ListOfScansActivity extends AppCompatActivity {

    //----------------------------------------------//

    // BluetoothScanner 인스턴스 생성
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothScanner bluetoothScanner;

    // Bluetooth LE 스캔 시작 및 지정된 시간 후 중지
    long scanPeriod = 5000; // 5초 동안 스캔

    //---------------------------------------------//

    public BleScanServices m_bleScanServices;
    public List<CustomBluetoothDeviceWrapper> devices;
    CustomListViewAdapter adapter;

    static LinearLayout modulelist_layout;
    ArrayList<CustomBluetoothDeviceWrapper> moduleList;
    public static ArrayList<BluetoothDevice> deviceList;

    ImageButton btn_menu, btn_reload;
    Button btn_settings, btn_trip;

    static SharedPreferences preferences;
    static String pref_address = "";

    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getApplicationContext();

        preferences = getSharedPreferences("Device Info", MODE_PRIVATE);

        bluetoothScanner = new BluetoothScanner(bluetoothAdapter, getApplicationContext());

        CheckBleHardware();

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        ActivityCompat.requestPermissions(this, permissions, 2);
        // This will pop up the first time the app is run, to give the app permissions not already given.
        m_bleScanServices = new BleScanServices(this);
        m_bleScanServices.checkBluetoothEnabled(this);

        devices = new ArrayList<>();

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

        adapter = new CustomListViewAdapter(this, R.layout.row, devices);
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
            String deviceName, deviceAddress;
            for (BluetoothDevice device : deviceList) {
                deviceName = device.getName();
                deviceAddress = device.getAddress();
                if ( device.getAddress() != null && !device.getAddress().equals("") ) {

                    Typeface typeface = Typeface.createFromAsset(context.getResources().getAssets(), "gmarket_sans_bold.ttf");

                    View customView = layoutInflater.inflate(R.layout.row, null);
                    ((LinearLayout) customView.findViewById(R.id.container)).setTag(deviceName + "#" + deviceAddress);
                    ((TextView) customView.findViewById(R.id.text1)).setText(deviceName);
                    ((TextView) customView.findViewById(R.id.text1)).setTypeface(typeface);

                    if (device.getAddress().equals(pref_address)) {
                        //TODO: background_rounding_green
                        ((LinearLayout) customView.findViewById(R.id.container)).setBackgroundResource(R.drawable.background_rounding_green);
                        ((TextView) customView.findViewById(R.id.text1)).setTextColor(Color.WHITE);
                        ((TextView) customView.findViewById(R.id.tv_connected)).setTextColor(Color.WHITE);  //green
                        ((TextView) customView.findViewById(R.id.tv_connected)).setText("Connected");
                        ((TextView) customView.findViewById(R.id.tv_connected)).setTypeface(typeface);

                        if (device.getBondState() == BOND_BONDED) {
                        /*
                         * BondState 정보 :
                         * https://developer.android.com/reference/android/bluetooth/BluetoothDevice */
                        }
                    }
                    else {
                        //TODO: background_rounding_white
                        ((LinearLayout) customView.findViewById(R.id.container)).setBackgroundResource(R.drawable.background_rounding_white);
                        ((TextView) customView.findViewById(R.id.text1)).setTextColor(Color.BLACK);
                        ((TextView) customView.findViewById(R.id.tv_connected)).setTextColor(Color.rgb(34, 177, 77));  //green
                        ((TextView) customView.findViewById(R.id.tv_connected)).setText("Available to connect");
                        ((TextView) customView.findViewById(R.id.tv_connected)).setTypeface(typeface);
                    }
                    modulelist_layout.addView(customView);


                    customView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // 연결하기
                            String tag = (String) customView.findViewById(R.id.container).getTag();
                            String[] tags = tag.split("#");
                            String name = tags[0];
                            String addr = tags[1];

                            // 임시, 확인용
                            Toast.makeText(context, "name: " + name + ", address: " + addr, Toast.LENGTH_SHORT).show();

                            final Intent intent = new Intent(context, HM10CommunicationActivity.class);
                            intent.putExtra(StaticResources.EXTRAS_DEVICE_NAME, name);
                            intent.putExtra(StaticResources.EXTRAS_DEVICE_ADDRESS, addr);
                            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);


                            //-----------------------------------------------------------------------------//
                            // 연결이 확인되면


                            if (((TextView) customView.findViewById(R.id.tv_connected)).getText().toString().toLowerCase(Locale.ROOT).equals("available to connect")) {

                                pref_address = addr;
                                System.out.println("OnClick - pref_address : " + pref_address);

                                ((LinearLayout) customView.findViewById(R.id.container)).setBackgroundResource(R.drawable.background_rounding_green);
                                ((TextView) customView.findViewById(R.id.text1)).setTextColor(Color.WHITE);
                                ((TextView) customView.findViewById(R.id.tv_connected)).setTextColor(Color.WHITE);
                                ((TextView) customView.findViewById(R.id.tv_connected)).setText("Connected");

                                ConsumptionActivity.btconnect = true;
                                tv_ready.setText("Connect");
                                tv_ready.setTextColor(Color.rgb(34, 177, 77));  //green

                                startThread();


                            } else if (((TextView) customView.findViewById(R.id.tv_connected)).getText().toString().toLowerCase(Locale.ROOT).equals("connected")) {

                                pref_address = "";
                                System.out.println("OnClick - pref_address : " + pref_address);

                                ((LinearLayout) customView.findViewById(R.id.container)).setBackgroundResource(R.drawable.background_rounding_white);
                                ((TextView) customView.findViewById(R.id.text1)).setTextColor(Color.BLACK);
                                ((TextView) customView.findViewById(R.id.tv_connected)).setTextColor(Color.rgb(34, 177, 77));  //green
                                ((TextView) customView.findViewById(R.id.tv_connected)).setText("Available to connect");

                                ConsumptionActivity.btconnect = false;
                                tv_ready.setText("Ready");
                                tv_ready.setTextColor(Color.rgb(255, 0, 0));  //red

                            }
                        }
                    });
                }
            }

        } else {
            System.out.println("deviceList is null...");
        }
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


    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences.Editor editor = preferences.edit();  //Editor를 preferences에 쓰겠다고 연결
        editor.putString("address", pref_address);

        editor.commit();  //항상 commit & apply 를 해주어야 저장이 된다.
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences.Editor editor = preferences.edit();  //Editor를 preferences에 쓰겠다고 연결
        editor.putString("address", pref_address);

        editor.commit();  //항상 commit & apply 를 해주어야 저장이 된다.
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences.Editor editor = preferences.edit();  //Editor를 preferences에 쓰겠다고 연결
        editor.putString("address", pref_address);

        editor.commit();  //항상 commit & apply 를 해주어야 저장이 된다.
    }

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
