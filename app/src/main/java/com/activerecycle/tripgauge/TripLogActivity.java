package com.activerecycle.tripgauge;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;

import com.activerecycle.tripgauge.bluetooth.R;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class TripLogActivity extends AppCompatActivity {

    final String APP_NAME = "RECYCLE-Trip_gauge";

    ConstraintLayout container;
    LinearLayout recordlist_layout;
    ImageButton imgbtn_back, btn_share;
    static TextView tv_back;
    static TextView tv_untitled;
    public static TextView tv_date;
    static TextView tv_used_wh;
    static TextView tv_dist_km;
    static TextView tv_avrpwr_w;
    static LogGraph graph_log;

    DBHelper dbHelper;
    static Map dataMap = new HashMap();
    ArrayList<Map> statList;

    int TABLE_ID = -1;
    String tripName;

    @Override
    public void finish() {
        super.finish();

        overridePendingTransition(0, 0); //0 for no animation
        MyAnimation.fadeOut(findViewById(R.id.content), 500);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triplog);

        MyAnimation.fadeIn(findViewById(R.id.content), 500);

        dbHelper = new DBHelper(TripLogActivity.this, 1);

        container = (ConstraintLayout) findViewById(R.id.container);
        recordlist_layout = (LinearLayout) findViewById(R.id.recordlist_layout);

        imgbtn_back = (ImageButton) findViewById(R.id.imgbtn_back);
        btn_share = (ImageButton) findViewById(R.id.btn_share);

        tv_back = (TextView) findViewById(R.id.tv_back);
        tv_untitled = (TextView) findViewById(R.id.tv_untitled);
        tv_date = (TextView) findViewById(R.id.tv_date);
        tv_used_wh = (TextView) findViewById(R.id.tv_used_wh);
        tv_dist_km = (TextView) findViewById(R.id.tv_dist_km);
        tv_avrpwr_w = (TextView) findViewById(R.id.tv_avrpwr_w);

        if (ConsumptionActivity.btconnect) {
            LocalDate currentDate = LocalDate.now();
            String now = currentDate.toString();
            String nowTime = now.replaceAll("-", ".");
            tv_date.setText(nowTime + " (CURRENT)");
        }

        graph_log = (LogGraph) findViewById(R.id.graph_log);

        tv_untitled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTripReviseDialog();
            }
        });


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

        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                btn_share.setVisibility(View.INVISIBLE);
                Bitmap bitmap = getBitmapFromView(container, container.getHeight(), container.getWidth());
                btn_share.setVisibility(View.VISIBLE);
                try {

                    File directory;
                    if (Build.VERSION.SDK_INT >= 30){

                        if (!Environment.isExternalStorageEmulated()){
                            Intent getpermission = new Intent();
                            getpermission.setAction(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                            startActivity(getpermission);
                        }


                        File destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                        if (!destination.exists()) { // 원하는 경로에 폴더가 있는지 확인
                            destination.mkdirs();
                            Log.d("TripLogActivity", "destination Created");
                        }

                        directory = new File(destination + File.separator + APP_NAME);
                        if (!directory.exists()) { // 원하는 경로에 폴더가 있는지 확인
                            directory.mkdirs();
                            Log.d("TripLogActivity", "Directory Created");
                        }
                    } else{
                        directory = new File(Environment.getExternalStorageDirectory() + File.separator + APP_NAME);
                        if (!directory.exists()) { // 원하는 경로에 폴더가 있는지 확인
                            directory.mkdirs();
                            Log.d("TripLogActivity", "Directory Created");
                        }
                    }

                    String filename = APP_NAME + "_" + TABLE_ID + "_" + getNowTime() +".jpg";
                    filename = filename.replaceAll(" ", "_");
                    filename = filename.replaceAll(":", "");

                    File file = new File(directory, filename);
                    if (file.exists()) {
                        file.delete();
                        file = new File(directory,filename);
                    }

                    FileOutputStream output = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
                    output.flush();
                    output.close();

                    Uri fileUri = FileProvider.getUriForFile(TripLogActivity.this, "com.activerecycle.tripgauge.fileprovider", file);

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    //Uri uri = Uri.parse(file.getPath());
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_STREAM, fileUri);
                    startActivity(Intent.createChooser(intent, "Share img"));

                    //Toast.makeText(TripLogActivity.this, "이미지를 공유했습니다.", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(TripLogActivity.this, "이미지를 공유하는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        setTripInfo(999);
        getTripListInfo();

//        if (dbHelper.getProfileCount("TripLogTable") > 0) {
//            setTripInfo(999);
//            getTripListInfo();
//        }
    }

    public void showCurrentTrip(DBHelper dbHelper) {
        //TODO: 실시간 Current 그래프 그리기!! time.sleep(1000)으로 invalidate();
        if (ConsumptionActivity.btconnect) {  // 블루투스가 연결되어있는 동안에는 주행하므로
            // 수행할 작업
            graph_log.map = dbHelper.getTripLogW(dataMap, -1);

            if (graph_log != null) {
                graph_log.invalidate();
                graph_log.setVisibility(View.VISIBLE);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_used_wh.setText(LogGraph.usedW +"Wh");
                        tv_avrpwr_w.setText(LogGraph.avrW + "W");
                        LocalDate currentDate = LocalDate.now();
                        String now = currentDate.toString();
                        String nowTime = now.replaceAll("-", ".");
                        tv_date.setText(nowTime + " (CURRENT)");
                        double ddist = ConsumptionActivity.tripADistance;
                        tv_dist_km.setText(String.format("%.2f", ddist) + "KM");
                        tv_untitled.setText("Untitled");
                    }
                });

            }

        }
    }

    private void setTripInfo(int tableId) {
        if (tableId == 999) {
            if (ConsumptionActivity.btconnect) {
                //TODO: 마지막으로 저장된 최신 트립 불러오기
                //마지막 트립 불러오기
                try {
                    int tripLogTableLastId = dbHelper.get_latestTripId();
                    tableId = tripLogTableLastId;
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
//            else {
//                // 이게 자꾸 호출됨!!!!
//                long mNow = System.currentTimeMillis();
//                Date mDate = new Date(mNow);
//                SimpleDateFormat mFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
//                mFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
//                String nowTime = mFormat.format(mDate);
//                //showSaveTripDialog(dbHelper.getTripLogLastId() + 1, nowTime);
//                saveTrip(nowTime);
//                return; }
        }

        graph_log.maxW = dbHelper.getMaxW(tableId);
        if (graph_log.maxW != -1 && graph_log.maxW != -2) {
            graph_log.map = dbHelper.getTripLogW(dataMap, tableId);
            graph_log.invalidate();
            graph_log.setVisibility(View.VISIBLE);

            Map tripSTATSmap = dbHelper.getTripSTATSbyID(tableId);
            String tripName = (String) tripSTATSmap.get("NAME");
            String tripDateTime = (String) tripSTATSmap.get("DATE");
            try {
                String[] s = tripDateTime.split(" ");
                String tripDate = s[0];

                //if (isCurrent) { tripDate += " (CURRENT)"; }
                int usedWh = (int) tripSTATSmap.get("USED");
                int dist = (int) tripSTATSmap.get("DIST");
                double ddist = dist * 0.01;
                int avrpwr = (int) tripSTATSmap.get("AVRPWR");

                tv_untitled.setText(tripName);
                tv_date.setText(tripDate);
                tv_used_wh.setText(usedWh + "Wh");
                tv_dist_km.setText(String.format("%.2f", ddist) + "KM");
                tv_avrpwr_w.setText(avrpwr + "W");
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        } else if (graph_log.maxW == -1) {
            graph_log.setVisibility(View.INVISIBLE);

            tv_untitled.setText("Untitled");
            tv_date.setText("----.--.--");
            tv_used_wh.setText("--Wh");
            tv_dist_km.setText("--KM");
            tv_avrpwr_w.setText("--W");
        } else if (graph_log.maxW == -2) {
            // bt not connected
            // == 실시간 그래프 그릴 수 없음.
            graph_log.setVisibility(View.VISIBLE);

            tv_untitled.setText("Untitled");
            tv_date.setText("----.--.--");
            tv_used_wh.setText("--Wh");
            tv_dist_km.setText("--KM");
            tv_avrpwr_w.setText("--W");
        }
    }

    private void getTripListInfo() {
        statList = new ArrayList<>();
        int tripLogTableLastId = (int) dbHelper.get_latestTripId();
        if (tripLogTableLastId == -1) { // 테이블이 비어있다.
            return;
        }

        for (int i = tripLogTableLastId; i >= 1; i--) {
            statList.add(dbHelper.getTripSTATSbyID(i));
        }

        System.out.println("**** statList : " + statList);

        setTripListView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!ConsumptionActivity.btconnect) {
            graph_log.setVisibility(View.INVISIBLE);
        } else {
            graph_log.setVisibility(View.VISIBLE);

            LocalDate currentDate = LocalDate.now();
            String now = currentDate.toString();
            String nowTime = now.replaceAll("-", ".");
            tv_date.setText(nowTime + " (CURRENT)");
        }
    }

    private void setTripListView() {
        recordlist_layout.removeAllViews();

        LayoutInflater layoutInflater = LayoutInflater.from(TripLogActivity.this);
        //LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (statList != null) {
            Map map;
            int tripID;
            String tripName, tripDateTime, tripDate;
            for (int i = 0; i < statList.size(); i++) {
                map = statList.get(i);
                if ( map.size() !=0 ) {
                    tripID = (int) map.get("ID");
                    tripName = (String) map.get("NAME");
                    tripDateTime = (String) map.get("DATE");
                    try {
                        String[] s = tripDateTime.split(" ");
                        tripDate = s[0];
                        if (tripDate.equals("null")) continue;
                    } catch (Exception e) {
                        //e.printStackTrace();
                        continue;
                    }


                    View customView = layoutInflater.inflate(R.layout.custom_record_info, null);
                    ((LinearLayout) customView.findViewById(R.id.container)).setTag(tripID + "");
                    ((TextView) customView.findViewById(R.id.tv_name)).setText(tripName);
                    ((TextView) customView.findViewById(R.id.tv_date)).setText(tripDate);

                    recordlist_layout.addView(customView);
                }
            }

        } else {
            System.out.println("statList is null...");
        }
    }

    // Trip 목록에서 특정 Trip을 클릭했을 때
    public void onClickRecord(View view) {
        TABLE_ID = Integer.parseInt(view.getTag().toString());

        setTripInfo(TABLE_ID);
    }

    private Bitmap getBitmapFromView(View view, int height, int width) {
        Bitmap bitmap = Bitmap.createBitmap(width+190, height,Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return bitmap;
    }

    private void showTripReviseDialog() {
        View dialogView = (View) View.inflate(
                TripLogActivity.this, R.layout.dialog_savetrip, null);
        AlertDialog.Builder dig = new AlertDialog.Builder(TripLogActivity.this, R.style.Theme_Dialog);
        dig.setView(dialogView);
        //dig.setTitle("Revise this trip title!");

        Toast.makeText(TripLogActivity.this, "한글, 영문, 숫자만 입력 가능합니다.", Toast.LENGTH_LONG).show();

        final EditText editText = (EditText) dialogView.findViewById(R.id.editText_tripTitle);
        editText.setFilters(new InputFilter[]{new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                Pattern ps = Pattern.compile("^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ \\u318D\\u119E\\u11A2\\u2022\\u2025a\\u00B7\\uFE55]+$");
                if (source.equals("") || ps.matcher(source).matches()) {
                    return source;
                }
                return "";
            }
        }});

        editText.setText(tv_untitled.getText().toString());

        dig.setNegativeButton("Cancel", null);
        dig.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                tripName = String.valueOf(editText.getText());

                if (tripName != null) {
                    dbHelper.update_TripName(TABLE_ID, tripName);

                    tv_untitled.setText(tripName);
                    getTripListInfo();
                    setTripListView();
                }
            }
        });
        dig.setCancelable(false);
        dig.show();

    }

//    private void showSaveTripDialog(final String nowTime) {
//        View dialogView = (View) View.inflate(
//                TripLogActivity.this, R.layout.dialog_savetrip, null);
//        android.app.AlertDialog.Builder dig = new android.app.AlertDialog.Builder(TripLogActivity.this, R.style.Theme_Dialog);
//        dig.setView(dialogView);
//        dig.setTitle("Save this trip!");
//
//        if ( getApplicationContext().equals(TripLogActivity.this) ) {
//            Toast.makeText(getApplicationContext(), "한글, 영문, 숫자만 입력 가능합니다.", Toast.LENGTH_SHORT).show();
//        }
//        final EditText editText = (EditText) dialogView.findViewById(R.id.editText_tripTitle);
//        editText.setFilters(new InputFilter[]{new InputFilter() {
//            @Override
//            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
//                Pattern ps = Pattern.compile("^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ :()_+]+$");
//                if (source.equals("") || ps.matcher(source).matches()) {
//                    return source;
//                }
//                return "";
//            }
//        }});
//
//        dig.setNegativeButton("Cancel", null);
//        dig.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//
//                tripName = String.valueOf(editText.getText());
//
//                saveTrip(nowTime);
//
//            }
//        });
//
//        dig.setCancelable(false);
//        dig.show();
//    }

    private void saveTrip(String nowTime) {

        int tripId = dbHelper.get_latestTripId();

//        if (tripName == null) { tripName = "Untitled"; }
        if (dbHelper.getAvgPwrW(tripId) == -999 || dbHelper.getUsedW(tripId) == -999 || dbHelper.getMaxW(tripId) == -2) return;
        dbHelper.update_TripSTATS(tripId, nowTime, dbHelper.getMaxW(tripId), dbHelper.getUsedW(tripId), (int)(ConsumptionActivity.tripADistance * 1000), dbHelper.getAvgPwrW(tripId));
        dbHelper.update_TripName(tripId, "Untitled");

        Toast.makeText(getApplicationContext(), "트립이 저장되었습니다.", Toast.LENGTH_SHORT).show();

        String allTrip = dbHelper.getTripSTATS();
        System.out.println(allTrip);


        // Trip 기록 개수 20개 넘으면 자동 삭제
        dbHelper.deleteGarbage();  //일단 찌꺼기 로그부터 삭제하고나서 개수 세기
        if (dbHelper.getProfileCount("TripSTATS") + 1 > 20) {
            dbHelper.deleteTrip();
        }
    }

    private String getNowTime() {

        SimpleDateFormat mFormat = new SimpleDateFormat("yyyy.MM.dd.hh.mm.ss");
        mFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        return date.toString();
    }
}
