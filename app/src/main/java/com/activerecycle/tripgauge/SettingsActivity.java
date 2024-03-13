package com.activerecycle.tripgauge;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.activerecycle.tripgauge.bluetooth.R;

public class SettingsActivity extends AppCompatActivity {

    ImageButton imgbtn_back;
    TextView tv_back;
    Switch switch1, switch2, switch3, switch4;
    Button btn_mph, btn_reset, btn_clear;

    DBHelper dbHelper;

    boolean s1, s2, s3, s4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        s1 = true;
        s2 = true;
        s3 = false;
        s4 = true;

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

        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbHelper.deleteAllTrip();
                Toast.makeText(SettingsActivity.this, "모든 트립을 삭제했습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        switch1.setChecked(s1);
        switch2.setChecked(s2);
        switch3.setChecked(s3);
        switch4.setChecked(s4);


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
                    s1 = true;
                    //TODO: Mediaplayer 경고음 재생

                } else {
                    s1 = false;

                }
            }
        });

        // Auto Save Trip
        switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) { // 오른쪽
                    s2 = true;
                    ConsumptionActivity.autoSave = true;
                    Toast.makeText(getApplicationContext(), "트립을 자동 저장합니다.", Toast.LENGTH_SHORT).show();
                } else { // 왼쪽
                    s2 = false;
                    ConsumptionActivity.autoSave = false;
                    Toast.makeText(getApplicationContext(), "트립 자동저장을 해제합니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Auto Connect
        switch3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) { // 오른쪽
                    s3 = true;
                    ConsumptionActivity.autoConnect = true;
                    Toast.makeText(getApplicationContext(), "블루투스를 자동 연결합니다.", Toast.LENGTH_SHORT).show();
                } else { // 왼쪽
                    s3 = false;
                    ConsumptionActivity.autoConnect = false;
                    Toast.makeText(getApplicationContext(), "블루투스 자동연결을 해제합니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Warning BATT
        switch4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) { // 오른쪽
                    s4 = true;
                } else { // 왼쪽
                    s4 = false;
                }
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        switch1.setChecked(s1);
        switch2.setChecked(s2);
        switch3.setChecked(s3);
        switch4.setChecked(s4);
    }
}
