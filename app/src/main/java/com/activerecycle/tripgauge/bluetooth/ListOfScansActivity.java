package com.activerecycle.tripgauge.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.activerecycle.tripgauge.ConnectionActivity;
import com.activerecycle.tripgauge.SettingsActivity;
import com.activerecycle.tripgauge.TripLogActivity;

import java.util.ArrayList;
import java.util.List;


public class ListOfScansActivity extends AppCompatActivity {


    public BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.add(new CustomBluetoothDeviceWrapper(device,rssi));
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            };

    public BleScanServices m_bleScanServices;
    public List<CustomBluetoothDeviceWrapper> devices;
    CustomListViewAdapter adapter;


    ImageButton btn_menu, btn_reload;
    Button btn_settings, btn_trip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CheckBleHardware();

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        ActivityCompat.requestPermissions(this, permissions, 2);
        // This will pop up the first time the app is run, to give the app permissions not already given.
        m_bleScanServices = new BleScanServices(this);
        m_bleScanServices.checkBluetoothEnabled(this);

        devices = new ArrayList<>();

        setContentView(R.layout.activity_connection); // Set the content to a layout with list view
        ListView lv = findViewById(R.id.myModuleList); // gets @android:id/list in said activity
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                view.setBackgroundResource(R.drawable.background_rounding_green);
                TextView textView1 = (TextView) view.findViewById(R.id.text1);
                TextView textView2 = (TextView) view.findViewById(R.id.tv_connected);
                textView1.setTextColor(Color.WHITE);
                textView2.setTextColor(Color.WHITE);


                final Intent intent = new Intent(ListOfScansActivity.this, HM10CommunicationActivity.class);
                intent.putExtra(StaticResources.EXTRAS_DEVICE_NAME, devices.get(i).getName()); // locally have the position
                intent.putExtra(StaticResources.EXTRAS_DEVICE_ADDRESS, devices.get(i).getAddress()); // locally have the position
                // but use the global variable of the array used for the adapter
                startActivity(intent);

            }
        }); // AppCompat needs listview itself to setOnItemClickListener, with the class as a context.
        adapter = new CustomListViewAdapter(this, R.layout.row, devices);
        // to TextView, not sure why context: this is needed
        lv.setAdapter(adapter); // set the list view to inflate with the adapter.


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
                BleScanServices.scanForDevices(true,mLeScanCallback);
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

    }// end of OnCreate.

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

//
//
//    public void refresh_Button_Click(View v) {
//
//    }
}
