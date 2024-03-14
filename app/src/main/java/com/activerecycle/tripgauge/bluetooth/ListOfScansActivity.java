package com.activerecycle.tripgauge.bluetooth;

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


//    public BluetoothAdapter.LeScanCallback mLeScanCallback =
//            new BluetoothAdapter.LeScanCallback() {
//                @Override
//                public void onLeScan(final BluetoothDevice device, final int rssi,
//                                     byte[] scanRecord) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            adapter.add(new CustomBluetoothDeviceWrapper(device,rssi));
//                            adapter.notifyDataSetChanged();
//                            getModuleListInfo();
//                        }
//                    });
//                }
//            };

    public BleScanServices m_bleScanServices;
    public List<CustomBluetoothDeviceWrapper> devices;
    CustomListViewAdapter adapter;

    static LinearLayout modulelist_layout;
    ArrayList<CustomBluetoothDeviceWrapper> moduleList;
    public static ArrayList<BluetoothDevice> deviceList;

    ImageButton btn_menu, btn_reload;
    Button btn_settings, btn_trip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BluetoothScanner bluetoothScanner = new BluetoothScanner(bluetoothAdapter, getApplicationContext());

        CheckBleHardware();

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        ActivityCompat.requestPermissions(this, permissions, 2);
        // This will pop up the first time the app is run, to give the app permissions not already given.
        m_bleScanServices = new BleScanServices(this);
        m_bleScanServices.checkBluetoothEnabled(this);

        devices = new ArrayList<>();

        setContentView(R.layout.activity_connection); // Set the content to a layout with list view

        //ListView lv = findViewById(R.id.myModuleList); // gets @android:id/list in said activity

//        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                //TODO: Click한 직후가 아니라, 연결이 확인이 되면!!!
//                TextView textView1 = (TextView) view.findViewById(R.id.text1);
//                TextView textView2 = (TextView) view.findViewById(R.id.tv_connected);
//                TextView tv_address = (TextView) view.findViewById(R.id.text2);
//
//                address = tv_address.getText().toString();
//
//                if (textView2.getText().toString().toLowerCase(Locale.ROOT).equals("available to connect")) {
//
//                    view.setBackgroundResource(R.drawable.background_rounding_green);
//                    textView1.setTextColor(Color.WHITE);
//                    textView2.setTextColor(Color.WHITE);
//                    textView2.setText("Connected");
//
//                    ConsumptionActivity.btconnect = true;
//                    tv_ready.setText("Connect");
//                    tv_ready.setTextColor(Color.rgb(146, 208, 80));  //green
//
//                    startThread();
//
//                } else if (textView2.getText().toString().toLowerCase(Locale.ROOT).equals("connected")) {
//                    view.setBackgroundResource(R.drawable.background_rounding_white);
//                    textView1.setTextColor(Color.BLACK);
//                    textView2.setTextColor(Color.rgb(146, 208, 80));  //green
//                    textView2.setText("Available to connect");
//
//                    ConsumptionActivity.btconnect = false;
//                    tv_ready.setText("Ready");
//                    tv_ready.setTextColor(Color.rgb(255, 0, 0));  //red
//
//                }
//
//                //TODO: 디바이스로부터 블루투스로 데이터 받고, 받은 데이터로 처리하는 부분 코딩!!
//
////                final Intent intent = new Intent(ListOfScansActivity.this, HM10CommunicationActivity.class);
////                intent.putExtra(StaticResources.EXTRAS_DEVICE_NAME, devices.get(i).getName()); // locally have the position
////                intent.putExtra(StaticResources.EXTRAS_DEVICE_ADDRESS, devices.get(i).getAddress()); // locally have the position
////                // but use the global variable of the array used for the adapter
////                startActivity(intent);
//
//            }
//        }); // AppCompat needs listview itself to setOnItemClickListener, with the class as a context.
//        // to TextView, not sure why context: this is needed
        //lv.setAdapter(adapter); // set the list view to inflate with the adapter.



//        Toolbar toolbar = findViewById(R.id.app_bar_list_of_scans); // declare the toolbar.
//        setSupportActionBar(toolbar); // Make toolbar visible on activity. Relative layout.
//        getSupportActionBar().setDisplayShowTitleEnabled(false);


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
            BluetoothDevice device;
            String deviceName, address;
            for (int i = 0; i < deviceList.size(); i++) {
                device = deviceList.get(i);
                deviceName = device.getName();
                address = device.getAddress();
                if ( device.getAddress() != null && !device.getAddress().equals("") ) {

                    View customView = layoutInflater.inflate(R.layout.row, null);
                    ((LinearLayout) customView.findViewById(R.id.container)).setTag(deviceName + "#" + address);
                    ((TextView) customView.findViewById(R.id.text1)).setText(deviceName);

                    ((LinearLayout) customView.findViewById(R.id.container)).setBackgroundResource(R.drawable.background_rounding_white);
                    ((TextView) customView.findViewById(R.id.text1)).setTextColor(Color.BLACK);
                    ((TextView) customView.findViewById(R.id.tv_connected)).setTextColor(Color.rgb(34, 177, 77));  //green
                    ((TextView) customView.findViewById(R.id.tv_connected)).setText("Available to connect");

                    modulelist_layout.addView(customView);


                    customView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // 연결하기
                            String tag = (String) customView.findViewById(R.id.container).getTag();
                            String[] tags = tag.split("#");
                            String name = tags[0];
                            String address = tags[1];

                            // 임시, 확인용
                            Toast.makeText(context, "name: " + name + ", address: " + address, Toast.LENGTH_SHORT).show();

                            final Intent intent = new Intent(context, HM10CommunicationActivity.class);
                            intent.putExtra(StaticResources.EXTRAS_DEVICE_NAME, name);
                            intent.putExtra(StaticResources.EXTRAS_DEVICE_ADDRESS, address);
                            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);


                            //-----------------------------------------------------------------------------//
                            // 연결이 확인되면
                            if (((TextView) customView.findViewById(R.id.tv_connected)).getText().toString().toLowerCase(Locale.ROOT).equals("available to connect")) {

                                ((LinearLayout) customView.findViewById(R.id.container)).setBackgroundResource(R.drawable.background_rounding_green);
                                ((TextView) customView.findViewById(R.id.text1)).setTextColor(Color.WHITE);
                                ((TextView) customView.findViewById(R.id.tv_connected)).setTextColor(Color.WHITE);
                                ((TextView) customView.findViewById(R.id.tv_connected)).setText("Connected");

                                ConsumptionActivity.btconnect = true;
                                tv_ready.setText("Connect");
                                tv_ready.setTextColor(Color.rgb(34, 177, 77));  //green

                                startThread();


                            } else if (((TextView) customView.findViewById(R.id.tv_connected)).getText().toString().toLowerCase(Locale.ROOT).equals("connected")) {
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


//    private void getModuleListInfo() {
//        moduleList = new ArrayList<>();
//
//        if (adapter != null && adapter.m_list != null) {
//            moduleList.addAll(adapter.m_list);
//        }
//
//        System.out.println("**** moduleList : " + moduleList);
//
//        setModuleListView();
//    }
//
//    private void setModuleListView() {
//        modulelist_layout.removeAllViews();
//
//        LayoutInflater layoutInflater = LayoutInflater.from(ListOfScansActivity.this);
//        //LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//        if (moduleList != null) {
//            CustomBluetoothDeviceWrapper device;
//            String moduleName, address;
//            for (int i = 0; i < moduleList.size(); i++) {
//                device = moduleList.get(i);
//                moduleName = device.getName();
//                address = device.getAddress();
//                if ( device.getAddress() != null && !device.getAddress().equals("") ) {
//
//                    View customView = layoutInflater.inflate(R.layout.row, null);
//                    ((LinearLayout) customView.findViewById(R.id.container)).setTag(moduleName + ":" + address);
//                    ((TextView) customView.findViewById(R.id.text1)).setText(moduleName);
//
//                    ((LinearLayout) customView.findViewById(R.id.container)).setBackgroundResource(R.drawable.background_rounding_white);
//                    ((TextView) customView.findViewById(R.id.text1)).setTextColor(Color.BLACK);
//                    ((TextView) customView.findViewById(R.id.tv_connected)).setTextColor(Color.rgb(34, 177, 77));  //green
//                    ((TextView) customView.findViewById(R.id.tv_connected)).setText("Available to connect");
//
//                    customView.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            // 연결하기
//                            String tag = (String) customView.findViewById(R.id.container).getTag();
//                            String[] tags = tag.split(":");
//                            String name = tags[0];
//                            String address = tags[1];
//
//                            // 임시, 확인용
//                            Toast.makeText(ListOfScansActivity.this, "name: " + name + ", address: " + address, Toast.LENGTH_SHORT).show();
//
//                            final Intent intent = new Intent(ListOfScansActivity.this, HM10CommunicationActivity.class);
//                            intent.putExtra(StaticResources.EXTRAS_DEVICE_NAME, name);
//                            intent.putExtra(StaticResources.EXTRAS_DEVICE_ADDRESS, address);
//                            startActivity(intent);
//
//
//                            //-----------------------------------------------------------------------------//
//                            // 연결이 확인되면
//                            if (((TextView) customView.findViewById(R.id.tv_connected)).getText().toString().toLowerCase(Locale.ROOT).equals("available to connect")) {
//
//                                ((LinearLayout) customView.findViewById(R.id.container)).setBackgroundResource(R.drawable.background_rounding_green);
//                                ((TextView) customView.findViewById(R.id.text1)).setTextColor(Color.WHITE);
//                                ((TextView) customView.findViewById(R.id.tv_connected)).setTextColor(Color.WHITE);
//                                ((TextView) customView.findViewById(R.id.tv_connected)).setText("Connected");
//
//                                ConsumptionActivity.btconnect = true;
//                                tv_ready.setText("Connect");
//                                tv_ready.setTextColor(Color.rgb(34, 177, 77));  //green
//
//                                startThread();
//
//
//                            } else if (((TextView) customView.findViewById(R.id.tv_connected)).getText().toString().toLowerCase(Locale.ROOT).equals("connected")) {
//                                ((LinearLayout) customView.findViewById(R.id.container)).setBackgroundResource(R.drawable.background_rounding_white);
//                                ((TextView) customView.findViewById(R.id.text1)).setTextColor(Color.BLACK);
//                                ((TextView) customView.findViewById(R.id.tv_connected)).setTextColor(Color.rgb(34, 177, 77));  //green
//                                ((TextView) customView.findViewById(R.id.tv_connected)).setText("Available to connect");
//
//                                ConsumptionActivity.btconnect = false;
//                                tv_ready.setText("Ready");
//                                tv_ready.setTextColor(Color.rgb(255, 0, 0));  //red
//
//                            }
//                        }
//                    });
//
//                    modulelist_layout.addView(customView);
//                }
//            }
//
//        } else {
//            System.out.println("moduleList is null...");
//        }
//    }

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

//    //Preferences에서 꺼내오는 메소드
//    private void getPreferences(){
//        address = preferences.getString("address", "");
//        if (address != null && !address.equals("")) {
//            ListView listView = findViewById(R.id.myModuleList);
//            View connectedView = listView.findViewWithTag(address);
//            TextView textView1 = (TextView) connectedView.findViewById(R.id.text1);
//            TextView textView2 = (TextView) connectedView.findViewById(R.id.tv_connected);
//
//            if (textView2.getText().toString().toLowerCase(Locale.ROOT).equals("available to connect")) {
//
//                connectedView.setBackgroundResource(R.drawable.background_rounding_green);
//                textView1.setTextColor(Color.WHITE);
//                textView2.setTextColor(Color.WHITE);
//                textView2.setText("Connected");
//
//                ConsumptionActivity.btconnect = true;
//                tv_ready.setText("Connect");
//                tv_ready.setTextColor(Color.rgb(34, 177, 77));  //green
//
//                startThread();
//
//            } else if (textView2.getText().toString().toLowerCase(Locale.ROOT).equals("connected")) {
//                connectedView.setBackgroundResource(R.drawable.background_rounding_white);
//                textView1.setTextColor(Color.BLACK);
//                textView2.setTextColor(Color.rgb(34, 177, 77));  //green
//                textView2.setText("Available to connect");
//
//                ConsumptionActivity.btconnect = false;
//                tv_ready.setText("Ready");
//                tv_ready.setTextColor(Color.rgb(255, 0, 0));  //red
//
//            }
//        }
//        else return;
//
//    }

    @Override
    protected void onResume() {
        super.onResume();

        BluetoothScanner bluetoothScanner = new BluetoothScanner(bluetoothAdapter, getApplicationContext());
        bluetoothScanner.startScan(scanPeriod);
//        getPreferences();
        //BleScanServices.scanForDevices(true,mLeScanCallback);
    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        getPreferences();
//    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//
//        SharedPreferences.Editor editor = preferences.edit();  //Editor를 preferences에 쓰겠다고 연결
//        editor.putString("address", address);
//
//        editor.commit();  //항상 commit & apply 를 해주어야 저장이 된다.
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//
//        SharedPreferences.Editor editor = preferences.edit();  //Editor를 preferences에 쓰겠다고 연결
//        editor.putString("address", address);
//
//        editor.commit();  //항상 commit & apply 를 해주어야 저장이 된다.
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//
//        SharedPreferences.Editor editor = preferences.edit();  //Editor를 preferences에 쓰겠다고 연결
//        editor.putString("address", address);
//
//        editor.commit();  //항상 commit & apply 를 해주어야 저장이 된다.
//    }

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
