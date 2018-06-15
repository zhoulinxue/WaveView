package org.zhx.waveview;

import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {
    private String TAG = MainActivity.class.getSimpleName();
    CountDownTimer timer;
    WaveView vm;
    RoundProgress roundProgress;
    DecimalFormat df;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vm = (WaveView) findViewById(R.id.waveview);
        roundProgress=findViewById(R.id.roundProgress);
         df = new DecimalFormat("0.00");
        startTask();

    }

    private void startTask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        timer = new CountDownTimer(50 * 1000, 500) {
            float progress = 0.1f;

            @Override
            public void onTick(long millisUntilFinished) {
                if (timer != null) {
                    progress+= 0.01;
                    String pro=df.format(progress*100);
                    vm.setWaterProgress(progress*100);
                    roundProgress.setProgress((int)(progress*100));
                    vm.setWaterProgress(Float.valueOf(pro));
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
