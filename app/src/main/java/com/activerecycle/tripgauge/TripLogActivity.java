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

import com.activerecycle.tripgauge.bluetooth.HM10ConnectionService;
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

// TripLog 페이지
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

    public static boolean otherListClicked = false;

    @Override
    public void finish() {
        super.finish();
        // 기본 애니메이션 없애기
        overridePendingTransition(0, 0); //0 for no animation
        //MyAnimation 클래스 이용해 페이지 사라질 때 전체 Layout에 fadeOut 애니매이션 줌
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

        // 블루투스 연결된 상태이면 (현재 주행 중이면) - 날짜 칸에 (Current) 표시
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
                String tag = view.getTag().toString();
                int tripId = Integer.parseInt(tag);
                if (tripId != -1) {  //실시간 트립은 제목을 수정할 수 없다.
                    showTripReviseDialog();
                }
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

        // 트립 제목, 날짜부터 그래프, 그래프 하단 usedW, dist, avrpwr 까지 이미지로 저장해 공유하는 기능
        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 공유 버튼은 이미지에 포함하지 않는다.
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

        setTripInfo(999);  // 999 : 마지막으로 저장된 최신 트립 불러오기 옵션
        getTripListInfo();  // 모든 트립 리스트(목록) 정보 불러오기
    }

    // 실시간 Current 그래프 그리기
    public void showCurrentTrip(DBHelper dbHelper) {
        if (ConsumptionActivity.btconnect) {  // 블루투스가 연결되어있는 동안에는 주행하므로
            graph_log.map = dbHelper.getW(dataMap, -1);

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
                        double ddist = ConsumptionActivity.tripOnceDistance;
                        tv_dist_km.setText(String.format("%.2f", ddist) + "KM");
                        tv_untitled.setText("Untitled");
                    }
                });

            }

        }
    }

    // 트립 정보 불러와서 그래프 띄워주는 함수
    private void setTripInfo(int tableId) {
        if (tableId == 999) {
            if (ConsumptionActivity.btconnect) {
                //마지막으로 저장된 최신 트립 불러오기
                try {
                    int tripLogTableLastId = dbHelper.get_latestTripId();
                    tableId = tripLogTableLastId;
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }

        graph_log.maxW = dbHelper.getMaxW(tableId);
        if (graph_log.maxW != -1 && graph_log.maxW != -2) {
            graph_log.map = dbHelper.getW(dataMap, tableId);
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

    // 트립 리스트를 불러오는 함수
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

    // 불러온 트립 리스트 정보를 토대로 레이아웃을 형성하는 함수
    private void setTripListView() {
        recordlist_layout.removeAllViews();

        LayoutInflater layoutInflater = LayoutInflater.from(TripLogActivity.this);
        //LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (ConsumptionActivity.btconnect && HM10ConnectionService.btStartFlag) {
            // 실시간 그래프 그려지는 중이면
            // 초록색으로 Connect 표시하기
            View customView = layoutInflater.inflate(R.layout.custom_record_info, null);
            ((LinearLayout) customView.findViewById(R.id.container)).setTag(-1 + "");  // 실시간 그래프는 tripId가 -1이다.
            ((TextView) customView.findViewById(R.id.tv_name)).setText("LIVE DATA");
            ((TextView) customView.findViewById(R.id.tv_date)).setText("CURRENT");

            ((LinearLayout) customView.findViewById(R.id.color_contianer)).setBackgroundResource(R.drawable.background_rounding_green_2);
            ((TextView) customView.findViewById(R.id.tv_name)).setTextColor(Color.BLACK);
            ((TextView) customView.findViewById(R.id.tv_date)).setTextColor(Color.BLACK);

            ((LinearLayout) customView.findViewById(R.id.container)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        showCurrentTrip(dbHelper);  //실시간 그래프 보여주기
                        otherListClicked = false;

                        tv_untitled.setTag(view.getTag());
                    } catch (Exception e) {
                        e.printStackTrace();
                        //Toast.makeText(mContext, "아직 주행이 시작되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            recordlist_layout.addView(customView);
        }

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

        tv_untitled.setTag(view.getTag());

        setTripInfo(TABLE_ID);
        otherListClicked = true;
    }

    // 특정 뷰를 비트맵으로 변환해주는 함수
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

    // 트립 제목(name)을 수정하는 다이얼로그를 띄워주는 함수
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
    
    // 현재 시각을 반환하는 함수
    private String getNowTime() {

        SimpleDateFormat mFormat = new SimpleDateFormat("yyyy.MM.dd.hh.mm.ss");
        mFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        return date.toString();
    }
}
