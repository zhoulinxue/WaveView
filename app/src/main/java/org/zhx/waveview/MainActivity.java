package org.zhx.waveview;

import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private String TAG = MainActivity.class.getSimpleName();
    CountDownTimer timer;
    WaveView vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vm = (WaveView) findViewById(R.id.waveview);
        startTask();
    }

    private void startTask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        timer = new CountDownTimer(51 * 1000, 1000) {
            int progress = 0;

            @Override
            public void onTick(long millisUntilFinished) {
                if (timer != null) {
                    progress += 2;
                    vm.setWaterProgress(progress);
                    Log.e(TAG, progress + "");
                }
            }

            @Override
            public void onFinish() {

            }
        };
        timer.start();
    }
}
