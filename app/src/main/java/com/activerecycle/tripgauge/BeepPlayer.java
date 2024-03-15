package com.activerecycle.tripgauge;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;

/*
* 미사용 클래스 !!!
* */
public class BeepPlayer {
    public static void playBeep(Context context) {
        // 시스템 서비스로부터 오디오 관리자 획득
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        // 음량을 최대로 설정 (필요에 따라서만)
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0);

        // 경고음 생성
        ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME);

        // 경고음 재생
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 2000); // 경고음 재생 시간을 설정할 수 있음 (여기서는 2초)
    }
}
