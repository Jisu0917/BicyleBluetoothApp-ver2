package com.activerecycle.tripgauge.bluetooth;

import static com.activerecycle.tripgauge.bluetooth.ListOfScansActivity.deviceList;
import static com.activerecycle.tripgauge.bluetooth.ListOfScansActivity.getDeviceListInfo;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class BluetoothScanner {
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private ScanCallback scanCallback;
    private Handler handler;
    private Context mContext;

    // BluetoothScanner 생성자
    public BluetoothScanner(BluetoothAdapter adapter, Context context) {
        mContext = context;
        bluetoothAdapter = adapter;
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        handler = new Handler(Looper.getMainLooper());
        scanCallback = new ScanCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                // 스캔 결과를 처리하는 로직을 여기에 추가
                BluetoothDevice device = result.getDevice();
                // 찾은 디바이스를 처리하거나 저장하는 등의 작업 수행
                if (device != null && device.getName() != null &&
                        device.getName().toLowerCase(Locale.ROOT).contains("jdy")
                ) { deviceList.add(device); }

                // deviceList 중복제거
                Set<BluetoothDevice> deviceSet = new HashSet<BluetoothDevice>(deviceList);
                deviceList = new ArrayList<BluetoothDevice>(deviceSet);
            }

            @Override
            public void onScanFailed(int errorCode) {
                // 스캔 실패 시 처리하는 로직을 여기에 추가
                // errorCode를 사용하여 실패 원인을 확인할 수 있음
            }
        };
    }

    // Bluetooth LE 스캔 시작 메소드
    @SuppressLint("MissingPermission")
    public void startScan(long scanPeriod) {
        if (bluetoothLeScanner != null) {
            deviceList = new ArrayList<>();
            bluetoothLeScanner.startScan(scanCallback);
            // 지정된 시간 후에 스캔 중지
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopScan();
                }
            }, scanPeriod);
        }
    }

    // Bluetooth LE 스캔 중지 메소드
    @SuppressLint("MissingPermission")
    public void stopScan() {
        if (bluetoothLeScanner != null) {
            bluetoothLeScanner.stopScan(scanCallback);
            getDeviceListInfo(mContext);
        }
    }
}
