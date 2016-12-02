package com.icostel.clockview;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class ClockView extends View {

    private static final String TAG = ClockView.class.getSimpleName();

    /**
     * Used by any component that needs to update something, when the hour or
     * minutes are changed by the user via dragging the circle on the clock.<br>
     * Receivers should implement the
     * {@link com.icostel.clockview.ClockView.TimeCallback} interface and: <br>
     * <ul>
     * <li>{@link com.icostel.clockview.ClockView#addCallback(TimeCallback)
     * addCallback()} to register</li>
     * <li>{@link com.icostel.clockview.ClockView#addCallback(TimeCallback)
     * removeCallback()} to unregister</li>
     * </ul>
     */
    public interface TimeCallback {

        void onHourChanged(int hour);

        void onMinuesChanged(int minutes);
    }

    public static class Constants {
        private static final int CLOCK_STROKE_COLOR = 0xFFFF0000; // R
        private static final int HOUR_FILL_COLOR = 0xFF00FF00; // G
        private static final int MINUTES_FILL_COLOR = 0xFF0000FF; // B
        private static final int CLOCK_RADIUS = 100; // px
        private static final int CLOCK_STROKE_WIDTH = 5;
        private static final boolean USE_ANTI_ALIAS = false;
        private static final int HOUR = 3;
        private static final int MINUTES = 45;
        private static final int HOUR_RADIUS = 20;
        private static final int MINUTES_RADIUS = 10;
        private static final boolean MINUTES_VISIBLE = true;
        private static final boolean HOUR_VISIBLE = true;
        private static final int DEGREE_CORRECTION = -90;
        private static final int MAX_HOURS = 12;
        private static final int MAX_MINUTES = 60;
        private static final double HOUR_ANGLE_UNIT = 2 * Math.PI / Constants.MAX_HOURS;
        private static final double MINUTE_ANGLE_UNIT = 2 * Math.PI / Constants.MAX_MINUTES;
    };

    private static final ArrayList<ClockView.TimeCallback> CALLBACKS_HOLDER = new ArrayList<ClockView.TimeCallback>();
    private boolean mUseAntiAlias = Constants.USE_ANTI_ALIAS;
    private int mClockRadius = Constants.CLOCK_RADIUS;
    private int mClockStrokeWidth = Constants.CLOCK_STROKE_WIDTH;
    private int mHourRadius = Constants.HOUR_RADIUS;
    private int mMinutesRadius = Constants.MINUTES_RADIUS;
    private int mHour = Constants.HOUR;

    private int mMinutes = Constants.MINUTES;
    private Paint mClockStrokePaint = null;
    private Paint mHourFillPaint = null;
    private Paint mMinutesFillPaint = null;
    private Paint mHourSweepPaint = null;
    private Paint mMinutesSweepPaint = null;
    private boolean mMinutesVisible = Constants.MINUTES_VISIBLE;
    private boolean mHourVisible = Constants.HOUR_VISIBLE;
    private Point mViewCenter = null;

    public ClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // get attributes from XML and initialize everything
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ClockView, 0, 0);
        try {
            mUseAntiAlias = a.getBoolean(R.styleable.ClockView_useAntiAlias, Constants.USE_ANTI_ALIAS);
            mClockStrokePaint = mUseAntiAlias ? new Paint(Paint.ANTI_ALIAS_FLAG) : new Paint();
            mHourFillPaint = mUseAntiAlias ? new Paint(Paint.ANTI_ALIAS_FLAG) : new Paint();
            mMinutesFillPaint = mUseAntiAlias ? new Paint(Paint.ANTI_ALIAS_FLAG) : new Paint();
            mHourSweepPaint = mUseAntiAlias ? new Paint(Paint.ANTI_ALIAS_FLAG) : new Paint();
            mMinutesSweepPaint = mUseAntiAlias ? new Paint(Paint.ANTI_ALIAS_FLAG) : new Paint();
            mClockStrokePaint
                    .setColor(a.getColor(R.styleable.ClockView_clockStrokeColor, Constants.CLOCK_STROKE_COLOR));
            mClockStrokeWidth = a.getInteger(R.styleable.ClockView_clockStrokeWidth, Constants.CLOCK_STROKE_WIDTH);
            mHourFillPaint.setColor(a.getColor(R.styleable.ClockView_hourFillColor, Constants.HOUR_FILL_COLOR));
            mMinutesFillPaint
                    .setColor(a.getColor(R.styleable.ClockView_minutesFillColor, Constants.MINUTES_FILL_COLOR));
            mHourSweepPaint.setColor(a.getColor(R.styleable.ClockView_hourFillColor, Constants.HOUR_FILL_COLOR));
            mMinutesSweepPaint.setColor(a.getColor(R.styleable.ClockView_minutesFillColor, Constants.HOUR_FILL_COLOR));
            mHour = a.getInteger(R.styleable.ClockView_hour, Constants.HOUR);
            mMinutes = a.getInteger(R.styleable.ClockView_minutes, Constants.MINUTES);
            mClockRadius = a.getInteger(R.styleable.ClockView_clockRadius, Constants.CLOCK_RADIUS);
            mHourRadius = a.getInteger(R.styleable.ClockView_hourRadius, Constants.HOUR_RADIUS);
            mMinutesRadius = a.getInteger(R.styleable.ClockView_minutesRadius, Constants.MINUTES_RADIUS);
            mMinutesVisible = a.getBoolean(R.styleable.ClockView_minutesVisible, Constants.MINUTES_VISIBLE);
            mHourVisible = a.getBoolean(R.styleable.ClockView_hourVisible, Constants.HOUR_VISIBLE);
            mClockStrokePaint.setStyle(Paint.Style.STROKE);
            mHourFillPaint.setStyle(Paint.Style.FILL);
            mMinutesFillPaint.setStyle(Paint.Style.FILL);
            mHourSweepPaint.setStyle(Paint.Style.STROKE);
            mMinutesSweepPaint.setStyle(Paint.Style.STROKE);
            mClockStrokePaint.setStrokeWidth(mClockStrokeWidth);
            mHourSweepPaint.setStrokeWidth(mClockStrokeWidth);
            mHourSweepPaint.setStrokeCap(Paint.Cap.ROUND);
            mMinutesSweepPaint.setStrokeWidth(mClockStrokeWidth);
            mMinutesSweepPaint.setStrokeCap(Paint.Cap.ROUND);
            mMinutesSweepPaint.setStrokeWidth(mClockStrokeWidth);
            // when the view is ready the msg(runnable) will be handled
            post(new Runnable() {
                @Override
                public void run() {
                    mViewCenter = new Point(getWidth() / 2, getHeight() / 2);
                }
            });
        } catch (RuntimeException ex) {
            Log.e(TAG, "Error ClockView(), err: " + ex.getMessage());
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int touchX = (int) event.getX();
        int touchY = (int) event.getY();
        double angle = Math.atan2(touchY - mViewCenter.y, touchX - mViewCenter.x);
        if (mHourVisible) {
            mHour = (int) Math.round(angle / Constants.HOUR_ANGLE_UNIT);
            if (Integer.signum(mHour) == -1) {
                mHour += Constants.MAX_HOURS;
            }
        }
        if (mMinutesVisible) {
            mMinutes = (int) Math.round(angle / Constants.MINUTE_ANGLE_UNIT);
            if (Integer.signum(mMinutes) == -1) {
                mMinutes += Constants.MAX_MINUTES;
            }
        }
        refresh();
        return true;
    }

    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(mViewCenter.x, mViewCenter.y, mClockRadius, mClockStrokePaint);
        // so that hour 0 points to North and not to the East
        setRotation(Constants.DEGREE_CORRECTION);
        int hourX = (int) (mViewCenter.x + mClockRadius * Math.cos(Constants.HOUR_ANGLE_UNIT * mHour));
        int hourY = (int) (mViewCenter.y + mClockRadius * Math.sin(Constants.HOUR_ANGLE_UNIT * mHour));
        int minutesX = (int) (mViewCenter.x + mClockRadius * Math.cos(Constants.MINUTE_ANGLE_UNIT * mMinutes));
        int minutesY = (int) (mViewCenter.y + mClockRadius * Math.sin(Constants.MINUTE_ANGLE_UNIT * mMinutes));
        if (mHourVisible) {
            canvas.drawCircle(hourX, hourY, mHourRadius, mHourFillPaint);
            // draw the sweep overlay
            canvas.drawArc(mViewCenter.x - mClockRadius, mViewCenter.y - mClockRadius, mViewCenter.x + mClockRadius,
                    mViewCenter.y + mClockRadius, 0, mHour * (float) Math.toDegrees(Constants.HOUR_ANGLE_UNIT), false,
                    mHourSweepPaint);
        }
        if (mMinutesVisible) {
            canvas.drawCircle(minutesX, minutesY, mMinutesRadius, mMinutesFillPaint);
            // draw the sweep overlay
            canvas.drawArc(mViewCenter.x - mClockRadius, mViewCenter.y - mClockRadius, mViewCenter.x + mClockRadius,
                    mViewCenter.y + mClockRadius, 0, mMinutes * (float) Math.toDegrees(Constants.MINUTE_ANGLE_UNIT),
                    false, mMinutesSweepPaint);
        }
    }

    public void addCallback(ClockView.TimeCallback callback) {
        if (!CALLBACKS_HOLDER.contains(callback)) {
            CALLBACKS_HOLDER.add(callback);
        }
    }

    public void removeCallback(ClockView.TimeCallback callback) {
        if (CALLBACKS_HOLDER.contains(callback)) {
            CALLBACKS_HOLDER.remove(callback);
        }
    }

    public void refresh() {
        postInvalidate();
        for (ClockView.TimeCallback callback : CALLBACKS_HOLDER) {
            callback.onHourChanged(this.mHour);
            callback.onMinuesChanged(this.mMinutes);
        }
    }

    public int getHour() {
        return mHour;
    }

    public int getMinutes() {
        return mMinutes;
    }

    public void setHourVisible(boolean hourVisible) {
        mHourVisible = hourVisible;
    }

    public void setMinutesVisible(boolean minutesVisible) {
        mMinutesVisible = minutesVisible;
    }

}
