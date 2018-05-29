package org.zhx.waveview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

/*
 * -----------------------------------------------------------------
 * Copyright (C) 2018-2020, by vogtec, All rights reserved.
 * -----------------------------------------------------------------
 * File: WaveView.java
 * Author: zhouxue
 * Version: 1.0
 * Create: 2018/5/29 15:31
 * Changes (from 2018/5/29)
 * -----------------------------------------------------------------
 * 2018/5/29 : Create WaveView.java (zhouxue);
 * -----------------------------------------------------------------
 */
public class WaveView extends View {
    private Paint backgroundPaint;
    private Paint firstWavePaint;
    private Paint secondWavePaint;
    private int backgroundColor;
    private int firstWaveColor;
    private int secondWaveColor;
    private Path firstPath;
    private Path secondPath;
    private Bitmap bitmap;
    private Canvas bitmapCanvas;
    /**
     * 水波的速度
     */
    private int speed;

    /**
     * 振幅
     */
    private int amplitude;
    /**
     * 角速度
     */
    private static final float palstance = 0.5F;

    /**
     * 最高水位
     */
    private static final float waterProgressMax = 100;
    /**
     * 水位高度
     */
    private float waterProgress;

    /**
     * 控件尺寸
     */
    private int waveSize;
    private int angle;
    /**
     * 开始 波动水位
     */
    private static final int whatStartWave = 100;
    private WaveHandler waveHandler;
    private WaveThread waveThread;
    private boolean canLoopWave;

    /**
     * 文字画笔
     */
    private Paint mTextPaint;
    /**
     * 默认文字颜色
     */
    private final int DEFAULT_TEXT_COLOR = Color.parseColor("#42ADFF");
    /**
     * 默认文字大小
     */
    private final int DEFAULT_TEXT_SIZE = 30;

    /**
     * 文字颜色
     */
    private int mTextColor = DEFAULT_TEXT_COLOR;

    /**
     * 边框颜色
     */
    private int borderColor = DEFAULT_TEXT_COLOR;
    /**
     * 文字大小
     */
    private int mTextSize = DEFAULT_TEXT_SIZE;

    private Paint mBorderPaint;

    private int borderWidth;



    public WaveView(Context context) {
        super(context);
        initView(null);
    }

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(waveSize+borderWidth, waveSize+borderWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        firstPath.reset();
        secondPath.reset();
        bitmapCanvas.drawCircle(waveSize / 2, waveSize / 2, waveSize / 2, backgroundPaint);
        /**水位线*/
        float waterLine = (waterProgressMax - waterProgress) * waveSize * 0.01F;
        /* x、y*/
        firstPath.moveTo(0, waterLine);
        secondPath.moveTo(0, waterLine);

        int x1 = 0;
        int y1 = 0;
        int x2 = 0;
        int y2 = 0;

        for (int i = 0; i < waveSize; i++) {
            x1 = i;
            x2 = i;
            y1 = (int) (amplitude * Math.sin((i * palstance + angle) * Math.PI / 180) + waterLine);
            y2 = (int) (amplitude * Math.sin((i * palstance + angle - 90) * Math.PI / 180) + waterLine);
            firstPath.quadTo(x1, y1, x1 + 1, y1);
            secondPath.quadTo(x2, y2, x2 + 1, y2);
        }
        if(waterProgress<waterProgressMax) {
            firstPath.lineTo(waveSize, waveSize);
            firstPath.lineTo(0, waveSize);
            firstPath.close();

            secondPath.lineTo(waveSize, waveSize);
            secondPath.lineTo(0, waveSize);
            secondPath.close();
            bitmapCanvas.drawPath(firstPath, firstWavePaint);
            bitmapCanvas.drawPath(secondPath, secondWavePaint);
        }else {
            backgroundPaint.setColor(firstWaveColor);
            bitmapCanvas.drawCircle(waveSize / 2, waveSize / 2, waveSize / 2, backgroundPaint);
        }
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.drawText((int)waterProgress + " %", (waveSize - mTextPaint.measureText(waterProgress+"")) / 2, waveSize /2, mTextPaint);
        if (borderWidth > 0) {
            // 边框大于0,表示需要绘制边框
            canvas.drawCircle(waveSize / 2, waveSize / 2,waveSize / 2, mBorderPaint);
        }
    }


    private void initView(AttributeSet attrs) {
        angle = 360;
        canLoopWave = true;
        waterProgress = 0;
        Context context = getContext();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.WaveView);
        backgroundColor = typedArray.getColor(R.styleable.WaveView_backgroundColor, Color.parseColor("#44EEEEEE"));
        firstWaveColor = typedArray.getColor(R.styleable.WaveView_firstWaveColor, Color.parseColor("#C3F5FE"));
        secondWaveColor = typedArray.getColor(R.styleable.WaveView_secondWaveColor, Color.parseColor("#43DCFE"));
        waterProgress = typedArray.getFloat(R.styleable.WaveView_waterProgress, 0);
        mTextColor=typedArray.getColor(R.styleable.WaveView_watertextColor,DEFAULT_TEXT_COLOR);
        mTextSize=typedArray.getInt(R.styleable.WaveView_watertextSize,DEFAULT_TEXT_SIZE);
        borderColor=typedArray.getColor(R.styleable.WaveView_waterBorderColor,DEFAULT_TEXT_COLOR);
        borderWidth=typedArray.getInt(R.styleable.WaveView_waterBorderWidth,0);

        amplitude = typedArray.getInt(R.styleable.WaveView_amplitude, dpToPx(20));
        speed = typedArray.getInt(R.styleable.WaveView_speed, 1);
        waveSize = typedArray.getDimensionPixelSize(R.styleable.WaveView_waveSize, 160);
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);

        waveThread = new WaveThread();
        waveHandler = new WaveHandler();

        mBorderPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setColor(borderColor);
        mBorderPaint.setStyle(Paint.Style.STROKE);

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setAntiAlias(true);

        bitmap = Bitmap.createBitmap(waveSize, waveSize, Bitmap.Config.ARGB_8888);

        bitmapCanvas = new Canvas(bitmap);
        bitmapCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        /*初始化波浪画笔*/
        firstWavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        firstWavePaint.setAntiAlias(true);
        firstWavePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        firstWavePaint.setColor(firstWaveColor);
        firstWavePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));

        secondWavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        secondWavePaint.setAntiAlias(true);
        secondWavePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        secondWavePaint.setColor(secondWaveColor);
        secondWavePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));

        firstPath = new Path();
        secondPath = new Path();

        /*开启线程*/
        waveThread.start();

    }

    /**
     * 设置 水位的进度 [0-100]
     *
     * @param waterProgress 水位进度
     */
    public WaveView setWaterProgress(float waterProgress) {
        if (waterProgress <= 0) {
            waterProgress = 0;
        } else if (waterProgress >= 100) {
            waterProgress = 100;
        }
        this.waterProgress = waterProgress;
        invalidate();
        return this;
    }

    /**
     * 设置第一条波浪线的 颜色
     */
    public WaveView setFirstWaveColor(int firstWaveColor) {
        this.firstWaveColor = firstWaveColor;
        firstWavePaint.setColor(firstWaveColor);
        invalidate();
        return this;
    }

    /**
     * 设置第二条波浪线的 颜色
     */
    public WaveView setSecondWaveColor(int secondWaveColor) {
        this.secondWaveColor = secondWaveColor;
        secondWavePaint.setColor(secondWaveColor);
        invalidate();
        return this;
    }

    /**
     * 设置 波动速读
     */
    public WaveView setSpeed(int speed) {
        if (speed < 1) {
            speed = 1;
        }
        if (speed > 10) {
            speed = 10;
        }
        this.speed = speed;
        return this;
    }

    /**
     * 波浪 波动的 线程
     */
    private final class WaveThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (canLoopWave) {
                angle = angle - 1 * speed;
                if (angle == 0) {
                    angle = 360;
                }
                waveHandler.sendEmptyMessage(whatStartWave);
                SystemClock.sleep(10);
            }
        }
    }

    /**
     * 波浪 波动的 操作者
     */
    private final class WaveHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (whatStartWave == msg.what) {
                invalidate();
            }
        }
    }

    /**
     * 设置 振幅
     *
     * @param amplitude 单位 dp
     */
    public WaveView setAmplitude(int amplitude) {
        if (amplitude <= 0) {
            amplitude = 0;
        }
        this.amplitude = amplitude;
        invalidate();
        return this;
    }


    /**
     * 数据转换: dp---->px
     */
    private int dpToPx(float dp) {
        return (int) (dp * getContext().getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        canLoopWave = false;
        if (waveThread != null) {
            waveThread.interrupt();
            waveThread = null;
        }
        if (waveHandler != null) {
            waveHandler.removeMessages(whatStartWave);
            waveHandler = null;
        }
    }
}
