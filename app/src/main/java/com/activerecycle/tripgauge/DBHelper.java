package com.activerecycle.tripgauge;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DBHelper extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "cycle.db";

    public DBHelper(@Nullable Context context, int version) {
        super(context, DATABASE_NAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE TripLog( logId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, tripId INTEGER not null, time TEXT, volt INTEGER, amp INTEGER, w INTEGER )");
        db.execSQL("CREATE TABLE TripSTATS( tripId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT , date DATE , max_w INTEGER, used INTEGER, dist INTEGER, avrpwr INTEGER )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS TripLog");
        db.execSQL("DROP TABLE IF EXISTS TripSTATS");
    }

    // TripLog Table 데이터 입력
    public void insert_TripLog(String time, int volt, int amp) {
        int w = volt * amp;
        SQLiteDatabase db = getWritableDatabase();
        //db.execSQL("INSERT INTO TripLog VALUES("+ id +", '" + time + "', " + volt + ", " + amp + ", " + w + ")");
        db.execSQL("INSERT INTO TripLog (tripId, time, volt, amp, w) VALUES(" + get_latestTripId() +", '" + time + "', " + volt + ", " + amp + ", " + w + ")");
//        if (db != null && db.isOpen()) db.close();
    }

    // TripSTATS Table 데이터 입력
    public void update_TripSTATS(int tripId, String date, int max_w, int used, int dist, int avrpwr) {
        SQLiteDatabase db = getWritableDatabase();
        //db.execSQL("INSERT INTO TripSTATS VALUES("+ id +", '" + name + "', '" + date + "', " + max_w + ", " + used + ", " + dist + ", " + avrpwr + ")");
        db.execSQL("UPDATE TripSTATS SET date = '" + date + "', max_w = " + max_w + ", used = " + used + ", dist = " + dist + ", avrpwr = " + avrpwr +" WHERE tripId = "+ tripId );
//        if (db != null && db.isOpen()) db.close();
    }

    public int init_TripSTATS() {
        SQLiteDatabase db = getWritableDatabase();
        //db.execSQL("INSERT INTO TripSTATS VALUES("+ id +", '" + name + "', '" + date + "', " + max_w + ", " + used + ", " + dist + ", " + avrpwr + ")");
        db.execSQL("INSERT INTO TripSTATS (name) VALUES( '#Init' )");
        int tripId = get_latestTripId();
//        if (db != null && db.isOpen()) db.close();
        return tripId;
    }

    public int get_latestTripId() {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT tripId FROM TripSTATS WHERE tripId =( SELECT MAX(tripId) FROM TripSTATS )", null);
        if (cursor != null && cursor.moveToFirst()) {
            int tripId = cursor.getInt(0);
            cursor.close();
////            //if (db != null && db.isOpen()) db.close();
            return tripId;
        }
        return -1;
    }

    public void deleteGarbage() {
        System.out.println("delete-ing-Garbage");
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM TripSTATS WHERE name = '" + "#Init'");
        System.out.println("delete-ed-Garbage");
        System.out.println(getTripSTATS());
        db.close();
    }

    public void deleteTrip() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM TripLog WHERE tripId <= (SELECT MAX(tripId) -20 FROM tripSTATS )");
        db.execSQL("DELETE FROM TripSTATS WHERE tripId <= (SELECT MAX(tripId) -20 FROM tripSTATS ) ");
//        if (db != null && db.isOpen()) db.close();
    }

    public void deleteAllTrip() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM TripLog");
        db.execSQL("DELETE FROM TripSTATS");
//        if (db != null && db.isOpen()) db.close();
    }


    // TripLog Table 조회
    public String getLog() {
        SQLiteDatabase db = getReadableDatabase();
        String result = "";

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM TripLog", null);
        while (cursor.moveToNext()) {
            result += "logId : " + cursor.getInt(0)
                    + ", tripId : " + cursor.getInt(1)
                    + ", time : " + cursor.getString(2)
                    + ", volt : " + cursor.getInt(3)
                    + ", amp : " + cursor.getInt(4)
                    + ", w : " + cursor.getInt(5)
                    + "\n";
        }
        cursor.close();
//        if (db != null && db.isOpen()) db.close();
        return result;
    }

    public long getProfileCount(String TABLE_NAME) {
        SQLiteDatabase db = this.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, TABLE_NAME);
//        if (db != null && db.isOpen()) db.close();
        return count;
    }

    public int getUsedW(int tripId) {
        Map map = getTripLog(tripId);
        ArrayList<Integer> wList = (ArrayList<Integer>) map.get("W");
        try {
            int first = wList.get(0);
            int last = wList.get(wList.size() - 1);
            int usedW = first - last;
            if (usedW < 0) {
                usedW = -usedW;
            }
            return usedW;
        } catch (Exception e) {
            e.printStackTrace();
            return -999;
        }
    }

    public int getAvgPwrW(int tripId) {
        Map map = getTripLog(tripId);
        ArrayList<Integer> wList = (ArrayList<Integer>) map.get("W");
        int sum = 0;

        if (wList.size() == 0) return -999;

        for (int i = 0; i < wList.size(); i++) {
            sum += wList.get(i);
        }
        int avg = sum / wList.size();
        return avg;
    }

    // TripSTATS Table 조회
    public String getTripSTATS() {
        SQLiteDatabase db = getReadableDatabase();
        String result = "";

        Cursor cursor = db.rawQuery("SELECT * FROM TripSTATS", null);
        while (cursor.moveToNext()) {
            result += "id : " + cursor.getInt(0)
                    + " name : " + cursor.getString(1)
                    + ", date : " + cursor.getString(2)
                    + ", max_w : " + cursor.getInt(3)
                    + ", used : " + cursor.getInt(4)
                    + ", dist : " + cursor.getInt(5)
                    + ", avrpwr : " + cursor.getInt(6)
                    + "\n";
        }

        cursor.close();
//        if (db != null && db.isOpen()) db.close();
        return result;
    }

    public Map getTripSTATSbyID(int tripId) {  //tripId equals tableId (probably)
        SQLiteDatabase db = getReadableDatabase();
        Map map = new HashMap();

        Cursor cursor = db.rawQuery("SELECT * FROM TripSTATS WHERE tripId = " + tripId, null);
        if (cursor != null && cursor.moveToFirst()) {
            map.put("ID", cursor.getInt(0));
            map.put("NAME", cursor.getString(1));
            map.put("DATE", cursor.getString(2));
            map.put("MAX_W", cursor.getInt(3));
            map.put("USED", cursor.getInt(4));
            map.put("DIST", cursor.getInt(5));
            map.put("AVRPWR", cursor.getInt(6));

            cursor.close();
//            if (db != null && db.isOpen()) db.close();
        }
        else {
            map.put("ID", -1);
            map.put("NAME", "null");
            map.put("DATE", "null");
            map.put("MAX_W", -1);
            map.put("USED", -1);
            map.put("DIST", -1);
            map.put("AVRPWR", -1);
        }
        return map;
    }

    public void update_TripName(int tripId, String tripName) {
        SQLiteDatabase db = getWritableDatabase();
        //UPDATE 테이블명 SET 컬럼명1=값1, 컬럼명2=값2 WHERE 조건식
        db.execSQL("UPDATE TripSTATS SET name = '" + tripName + "' WHERE tripId = " + tripId);
//        if (db != null && db.isOpen()) db.close();
    }

    public Map getTripLogW(Map map, int tripId) {
        //TODO: 실시간 그래프 그리기
        if (tripId == -1) {  // Now -ing (Current)
            tripId = get_latestTripId();
        }
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Integer> list = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT w FROM TripLog WHERE tripId = " + tripId, null);
        while (cursor.moveToNext()) {
            list.add(cursor.getInt(0));
        }
        map.put("W", list);

        cursor.close();
//        if (db != null && db.isOpen()) db.close();
        return map;
    }

    public Map getTripLog(int tripId) {

        Map map = new HashMap();

        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<String> list1 = new ArrayList<>();
        ArrayList<Integer> list2 = new ArrayList<>();
        ArrayList<Integer> list3 = new ArrayList<>();
        ArrayList<Integer> list4 = new ArrayList<>();

        // 1 : time
        // 2 : volt
        // 3 : amp
        // 4 : W
        Cursor cursor = db.rawQuery("SELECT * FROM TripLog WHERE tripId = " + tripId, null);
        while (cursor.moveToNext()) {
            list1.add(cursor.getString(2));
            list2.add(cursor.getInt(3));
            list3.add(cursor.getInt(4));
            list4.add(cursor.getInt(5));
        }
        map.put("TIME", list1);
        map.put("VOLT", list2);
        map.put("AMP", list3);
        map.put("W", list4);

        cursor.close();
//        if (db != null && db.isOpen()) db.close();
        return map;
    }

    public int getMaxW(int tripId) {
        if (tripId >= 1) {
            Map map = getTripLog(tripId);
            ArrayList<Integer> wList = (ArrayList<Integer>) map.get("W");
            if (wList.size() != 0) {
                int max = wList.get(0);
                for (int i = 0; i < wList.size(); i++) {
                    if (wList.get(i) > max) {
                        max = wList.get(i);
                    }
                }

                return max;
            } else { return -2;//bt not connected
            }
        } else return -1;
    }

    public Map getTripLogTime(Map map) {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Integer> list = new ArrayList<>();

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM TripLog", null);
        while (cursor.moveToNext()) {
            list.add(cursor.getInt(2));
        }
        map.put("TIME", list);
//        if (db != null && db.isOpen()) db.close();
        cursor.close();
        return map;
    }
}
