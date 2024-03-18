package com.activerecycle.tripgauge.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

public class BleScanServices {

    public static BluetoothAdapter bleAdapter;

    BleScanServices(final Context context){
        final BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bleAdapter = bluetoothManager.getAdapter();

        boolean permissionGranted = ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

            if(permissionGranted){
            Toast.makeText(context, "Location Permssions Granted", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(context, "Location Permssions Required", Toast.LENGTH_LONG).show();
        }

    }

    public void checkBluetoothEnabled(Context context){
        if (bleAdapter == null || !bleAdapter.isEnabled()) {
            // TO DO: Figure out how to use the below two lines to prompt user to turn on their bluetooth
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Toast.makeText(context, "Ble Not Enabled", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(context, "Ble Enabled", Toast.LENGTH_SHORT).show();
        }
    }




}
