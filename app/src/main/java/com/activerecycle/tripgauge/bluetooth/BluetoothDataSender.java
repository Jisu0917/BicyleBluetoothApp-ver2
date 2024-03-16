package com.activerecycle.tripgauge.bluetooth;

import android.bluetooth.BluetoothSocket;
import java.io.IOException;
import java.io.OutputStream;

public class BluetoothDataSender {

    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;

    public BluetoothDataSender(BluetoothSocket socket) {
        bluetoothSocket = socket;
        try {
            outputStream = bluetoothSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendData(String data) {
        try {
            outputStream.write(data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

