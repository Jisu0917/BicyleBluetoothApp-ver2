package com.activerecycle.tripgauge;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.activerecycle.tripgauge.bluetooth.R;

public class SettingsActivity extends AppCompatActivity {

    ImageButton imgbtn_back;
    TextView tv_back;
    Switch switch1, switch2, switch3, switch4;
    Button btn_mph, btn_reset, btn_clear;

    DBHelper dbHelper;

    public static SharedPreferences preferences;
    boolean b1, b2, b3, b4;
    public static boolean speedFlag, socFlag;
    public static String distFlag = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = getSharedPreferences("Setting Info", MODE_PRIVATE);

        dbHelper = new DBHelper(SettingsActivity.this, 1);

        imgbtn_back = (ImageButton) findViewById(R.id.imgbtn_back);
        tv_back = (TextView) findViewById(R.id.tv_back);
        switch1 = (Switch) findViewById(R.id.switch1);
        switch2 = (Switch) findViewById(R.id.switch2);
        switch3 = (Switch) findViewById(R.id.switch3);
        switch4 = (Switch) findViewById(R.id.switch4);
        btn_mph = (Button) findViewById(R.id.btn_mph);
        btn_reset = (Button) findViewById(R.id.btn_reset);
        btn_clear = (Button) findViewById(R.id.btn_clear);

        imgbtn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        tv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btn_mph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btn_mph.getText().equals("MPH")) {
                    distFlag = "Km";
                    btn_mph.setText("KPH");
                    ConsumptionActivity.tv_KPH.setText("KPH");
                    ConsumptionActivity.tv_distFlag.setText("Km");
                    if (!ConsumptionActivity.btconnect) {
                        ConsumptionActivity.tv_distance.setText("00.00 Km");
                    }
                } else {
                    distFlag = "Mi";
                    btn_mph.setText("MPH");
                    ConsumptionActivity.tv_KPH.setText("MPH");
                    ConsumptionActivity.tv_distFlag.setText("Mi");
                    if (!ConsumptionActivity.btconnect) {
                        ConsumptionActivity.tv_distance.setText("00.00 Mi");
                    }
                }
            }
        });

        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbHelper.deleteAllTrip();
                TripLogActivity.graph_log.maxW = 0;
                TripLogActivity.graph_log.invalidate();
                TripLogActivity.tv_untitled.setText("Untitled");
                TripLogActivity.tv_date.setText("----.--.--");
                TripLogActivity.tv_used_wh.setText("--Wh");
                TripLogActivity.tv_dist_km.setText("--KM");
                TripLogActivity.tv_avrpwr_w.setText("--W");

                Toast.makeText(SettingsActivity.this, "모든 트립을 삭제했습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConsumptionActivity.totalDistance = 0.0;
                if (ConsumptionActivity.tv_odo.getText().toString().equals("ODO")) {
                    ConsumptionActivity.tv_distance.setText("00.00 " + distFlag);
                }
            }
        });

        // Warning Sound
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                if (b) { //왼쪽일 때 회색, 오른쪽일 때 lightgreen
//                    compoundButton.setButtonDrawable(R.drawable.switch_thumb_gray);
//                } else {
//                    compoundButton.setButtonDrawable(R.drawable.switch_thumb);
//                }

                if (b) {
                    //b1 = true;
                    speedFlag = true;
                } else {
                    //b1 = false;
                    speedFlag = false;
                }
            }
        });

        // Auto Save Trip
        switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) { // 오른쪽
                    b2 = true;

                    ConsumptionActivity.autoSave = true;
                } else { // 왼쪽
                    b2 = false;

                    ConsumptionActivity.autoSave = false;
                }
            }
        });

        // Auto Connect
        switch3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) { // 오른쪽
                    b3 = true;

                    ConsumptionActivity.autoConnect = true;
                } else { // 왼쪽
                    b3 = false;

                    ConsumptionActivity.autoConnect = false;
                }
            }
        });

        // Warning BATT
        switch4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) { // 오른쪽
                    //TODO: 경고음 재생
                    //b4 = true;
                    socFlag = true;
                } else { // 왼쪽
                    //b4 = false;
                    socFlag = false;
                }
            }
        });


    }

    //Preferences에서 꺼내오는 메소드
    private void getPreferences(){
        switch1.setChecked(preferences.getBoolean("s1", true));
        switch2.setChecked(preferences.getBoolean("s2", false));
        switch3.setChecked(preferences.getBoolean("s3", true));
        switch4.setChecked(preferences.getBoolean("s4", true));
        if (preferences.getString("distFlag", "").equals("Mi")) {
            btn_mph.setText("MPH");
            ConsumptionActivity.tv_KPH.setText("MPH");
            ConsumptionActivity.tv_distFlag.setText("Mi");
            if (!ConsumptionActivity.btconnect) {
                ConsumptionActivity.tv_distance.setText("00.00");
            }
        } else {
            btn_mph.setText("KPH");
            ConsumptionActivity.tv_KPH.setText("KPH");
            ConsumptionActivity.tv_distFlag.setText("Km");
            if (!ConsumptionActivity.btconnect) {
                ConsumptionActivity.tv_distance.setText("00.00");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences.Editor editor = preferences.edit();  //Editor를 preferences에 쓰겠다고 연결
        editor.putBoolean("s1", speedFlag);  //putString(KEY,VALUE)
        editor.putBoolean("s2", b2);  //putString(KEY,VALUE)
        editor.putBoolean("s3", b3);  //putString(KEY,VALUE)
        editor.putBoolean("s4", socFlag);  //putString(KEY,VALUE)
        editor.putString("distFlag", distFlag);

        editor.commit();  //항상 commit & apply 를 해주어야 저장이 된다.
    }

    @Override
    protected void onResume() {
        super.onResume();

        getPreferences();
    }
}
