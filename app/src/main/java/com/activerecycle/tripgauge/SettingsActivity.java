package com.activerecycle.tripgauge;

import static com.activerecycle.tripgauge.bluetooth.HM10ConnectionService.saveTrip;
import static com.activerecycle.tripgauge.bluetooth.HM10ConnectionService.tripId;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.activerecycle.tripgauge.bluetooth.BleConnectionService;
import com.activerecycle.tripgauge.bluetooth.HM10ConnectionService;
import com.activerecycle.tripgauge.bluetooth.R;

import java.time.LocalDate;

// Settings 페이지
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
    public void finish() {
        super.finish();

        // 기본 애니메이션 없애기
        overridePendingTransition(0, 0); //0 for no animation
    }

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
                // Connection 페이지로 돌아감
                finish();
            }
        });

        tv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Connection 페이지로 돌아감
                finish();
            }
        });

        btn_mph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // KPH <-> MPH 토글 구현
                if (btn_mph.getText().equals("MPH")) {
                    distFlag = "Km";
                    btn_mph.setText("KPH");
                    ConsumptionActivity.tv_KPH.setText("KPH");
                    ConsumptionActivity.tv_distFlag.setText("Km");
                    if (!ConsumptionActivity.btconnect) {
                        ConsumptionActivity.tv_distance.setText("00.00");
                    }
                } else {
                    distFlag = "Mi";
                    btn_mph.setText("MPH");
                    ConsumptionActivity.tv_KPH.setText("MPH");
                    ConsumptionActivity.tv_distFlag.setText("Mi");
                    if (!ConsumptionActivity.btconnect) {
                        ConsumptionActivity.tv_distance.setText("00.00");
                    }
                }
            }
        });

        // 로그 리셋 버튼 - 모든 트립 삭제하기
        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showResetLogDialog();
            }
        });

        // 주행 거리 리셋 버튼 - ODO 초기화하기
        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showClearOdoDialog();
            }
        });

        // Warning Sound
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
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
        // 저장된 설정 정보들을 가져와서 셋팅해준다.
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
        // 모든 설정 정보를 저장한다.
        SharedPreferences.Editor editor = preferences.edit();  //Editor를 preferences에 쓰겠다고 연결
        editor.putBoolean("s1", speedFlag);  //putString(KEY,VALUE)
        editor.putBoolean("s2", b2);  //putString(KEY,VALUE)
        editor.putBoolean("s3", b3);  //putString(KEY,VALUE)
        editor.putBoolean("s4", socFlag);  //putString(KEY,VALUE)
        editor.putString("distFlag", distFlag);  //putString(KEY,VALUE)

        editor.commit();  //항상 commit & apply 를 해주어야 저장이 된다.
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 모든 설정 정보를 가져와서 셋팅한다.
        getPreferences();
    }

    // 다이얼로그를 화면에 띄워주는 함수 - 주행기록 로그를 모두 삭제하는지 묻는 데에 쓰인다.
    public void showResetLogDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("RESET LOG")
                .setMessage("주행기록을 모두 삭제 하시겠습니까?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // 삭제 작업을 수행하는 코드
                        dbHelper.deleteAllTrip();
                        if (TripLogActivity.graph_log != null) {
                            TripLogActivity.graph_log.maxW = 0;
                            TripLogActivity.graph_log.invalidate();
                            TripLogActivity.tv_untitled.setText("Untitled");
                            TripLogActivity.tv_date.setText("----.--.--");
                            TripLogActivity.tv_used_wh.setText("--Wh");
                            TripLogActivity.tv_dist_km.setText("--KM");
                            TripLogActivity.tv_avrpwr_w.setText("--W");
                        }

                        Toast.makeText(SettingsActivity.this, "모든 트립을 삭제했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
        builder.setNegativeButton("아니오", null);  // 아니오를 누르면 아무 작업도 하지 않음.
        android.app.AlertDialog dialog = builder.show();

        // 다이얼로그 메시지의 글꼴 지정
        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        Typeface typeface = Typeface.createFromAsset(getResources().getAssets(), "gmarket_sans_medium.ttf");
        textView.setTypeface(typeface);
    }

    // 다이얼로그를 화면에 띄워주는 함수 - 총주행거리(ODO)를 삭제하는지 묻는 데에 쓰인다.
    public void showClearOdoDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("CLEAR ODO")
                .setMessage("총주행거리(ODO)를 삭제 하시겠습니까?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // ODO를 삭제하는 코드
                        ConsumptionActivity.totalDistance = 0.0;
                        if (ConsumptionActivity.tv_odo.getText().toString().equals("ODO")) {
                            ConsumptionActivity.tv_distance.setText("00.00 " + distFlag);
                        }
                        Toast.makeText(SettingsActivity.this, "총주행거리(ODO)를 삭제했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
        builder.setNegativeButton("아니오", null);  // 아니오를 누르면 아무 작업도 하지 않음.
        android.app.AlertDialog dialog = builder.show();

        // 다이얼로그 메시지의 글꼴 지정
        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
        Typeface typeface = Typeface.createFromAsset(getResources().getAssets(), "gmarket_sans_medium.ttf");
        textView.setTypeface(typeface);
    }
}
