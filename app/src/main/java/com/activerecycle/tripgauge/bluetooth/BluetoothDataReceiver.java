package com.activerecycle.tripgauge.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import java.io.IOException;
import java.io.InputStream;

public class BluetoothDataReceiver {

    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private Handler handler;
    private StringBuilder dataBuffer = new StringBuilder();

    public BluetoothDataReceiver(BluetoothSocket socket, Handler handler) {
        bluetoothSocket = socket;
        this.handler = handler;
        try {
            inputStream = bluetoothSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[1024];
                int bytes;

                while (true) {
                    try {
                        bytes = inputStream.read(buffer);
                        String receivedData = new String(buffer, 0, bytes);
                        dataBuffer.append(receivedData);

                        // 데이터를 받을 때마다 줄 바꿈 문자를 기준으로 데이터를 처리
                        int newlineIndex;
                        while ((newlineIndex = dataBuffer.indexOf("\n")) != -1) {
                            String data = dataBuffer.substring(0, newlineIndex);
                            dataBuffer.delete(0, newlineIndex + 1);
                            // 데이터를 UI 스레드로 전달하여 처리
                            handler.obtainMessage(0, data).sendToTarget();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        });
        thread.start();
    }
}

