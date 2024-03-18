package com.activerecycle.tripgauge;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.IBinder;

public class BeepService extends Service {

    private ToneGenerator toneGenerator;

    @Override
    public void onCreate() {
        super.onCreate();
        //toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME);
        toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, 50);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 경고음 재생
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 2000); // 2초 동안 경고음 재생

        // START_STICKY를 반환하여 서비스가 강제로 중지되었을 때 자동으로 다시 시작하도록 함
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 서비스가 종료될 때 ToneGenerator 해제
        if (toneGenerator != null) {
            toneGenerator.release();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
