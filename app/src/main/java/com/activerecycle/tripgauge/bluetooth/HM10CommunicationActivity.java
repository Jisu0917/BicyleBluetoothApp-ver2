package com.activerecycle.tripgauge.bluetooth;

import static com.activerecycle.tripgauge.ConsumptionActivity.autoSave;
import static com.activerecycle.tripgauge.ConsumptionActivity.graph_battery;
import static com.activerecycle.tripgauge.ConsumptionActivity.tv_percent;
import static com.activerecycle.tripgauge.ConsumptionActivity.tv_ready;
import static com.activerecycle.tripgauge.ConsumptionActivity.tv_w;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.util.Log;
import android.view.View;
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

    DBHelper dbHelper;
    TripLogActivity tripLogActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hm10_communication);
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(StaticResources.EXTRAS_DEVICE_NAME);
        m_deviceAddress = intent.getStringExtra(StaticResources.EXTRAS_DEVICE_ADDRESS);
        m_bleConnectionService = new BleConnectionService(this, m_deviceAddress);

        Toolbar toolbar = findViewById(R.id.app_bar_hm10_communication); // declare the toolbar.
        TextView textDeviceName = toolbar.findViewById(R.id.toolbar_device_name);
        TextView textDeviceAddress = toolbar.findViewById(R.id.toolbar_device_address);

        textDeviceName.setText(mDeviceName);
        textDeviceAddress.setText(m_deviceAddress);

        setSupportActionBar(toolbar); // Make toolbar visible on activity. Xml is very minimalistic
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        textSerialConnection = findViewById(R.id.bluetooth_serial_cxn_value);
        textSerialConnection.setText(StaticResources.CONNECTION_STATE_CONNECTING);
        m_hasSerial = false;

        IntentFilter filterMaster =new IntentFilter();
        filterMaster.addAction(StaticResources.BROADCAST_NAME_CONNECTION_UPDATE);
        filterMaster.addAction(StaticResources.BROADCAST_NAME_SERVICES_DISCOVERED);
        filterMaster.addAction(StaticResources.BROADCAST_NAME_TX_CHARATERISTIC_CHANGED);
        registerReceiver(m_bleBroadcastReceiver, filterMaster);



        //-------------------------------------------//


        tripLogActivity = new TripLogActivity();

        dbHelper = new DBHelper(HM10CommunicationActivity.this, 1);

        tv_what_do_u_saying = (TextView) findViewById(R.id.tv_what_do_u_saying);
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
                    tv_what_do_u_saying.setText(connection);
                    // connection : Connected
                    //TODO : 연결되었을 때!


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

                    break;
                case StaticResources.BROADCAST_NAME_TX_CHARATERISTIC_CHANGED:
                    final String txData = intent.getStringExtra(StaticResources.EXTRAS_TX_DATA);
//                    final String txData = "Tx data received from HM10";
                    Toast.makeText(context, txData, Toast.LENGTH_SHORT).show();
                    Log.i("Broadcast Received",
                            "TxData = " + txData + ";");
                    tv_what_do_u_saying.setText(txData);

                    // txData : voltvalue=00/ampvalue=00/socvalue=00
                    try {
                        String voltStr = txData.split("/")[0].split("=")[1];
                        int volt = Integer.parseInt(voltStr);
                        String ampStr = txData.split("/")[1].split("=")[1];
                        int amp = Integer.parseInt(ampStr);
                        String socStr = txData.split("/")[2].split("=")[1];
                        int soc = Integer.parseInt(socStr);

                        //TODO: 배터리 5% 이하 경고음 ------ 소리가 안 남 !
                        // --- SettingsActivity.socFlag && 때문이었음 ! 빼니까 소리 잘 남.
                        // ------ 근데 그럼 플래그 처리를 어떻게 하지? preference? 이미 perference에서 가져온건데?
                        if (soc <= 5) {//SettingsActivity.socFlag && 
                            //BeepPlayer.playBeep(getApplicationContext());
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
                            LocalDate currentDate = LocalDate.now();
                            String nowTime = currentDate.toString();
                            dbHelper.insert_TripLog(nowTime, volt, amp);
                            //------------------확인을 위한 출력 코드-------------//
                            String allLog = dbHelper.getLog();
                            System.out.println(allLog);
                            //------------------확인을 위한 출력 코드-------------//

                            tripLogActivity.showCurrentTrip(dbHelper);  //실시간 그래프 보여주기
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("printStackTrace - txData: " + txData);
                    }
                    break;
            }
        }
    };








}
