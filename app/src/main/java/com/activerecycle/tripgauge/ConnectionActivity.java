package com.activerecycle.tripgauge;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.activerecycle.tripgauge.bluetooth.R;

import java.util.HashMap;
import java.util.Map;

public class ConnectionActivity extends AppCompatActivity {

    ImageButton btn_menu, btn_reload;
    Button btn_settings, btn_trip;

    /*
    * Log 그래프 확인 위한 코드 - 지우기!
    * */
    static Map dataMap = new HashMap();
    static LogGraph graph_log;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
//        /*
//         * Log 그래프 확인 위한 코드 - 지우기!
//         * */
//        setContentView(R.layout.tmplayout);
//        dbHelper = new DBHelper(ConnectionActivity.this, 1);
//        graph_log.map = dbHelper.getTripLogW(dataMap, 3);

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
                // 숙피치 앱 freindlist activity 참고
            }
        });

        btn_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ConnectionActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        btn_trip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ConnectionActivity.this, TripLogActivity.class);
                startActivity(intent);
            }
        });
    }
}
