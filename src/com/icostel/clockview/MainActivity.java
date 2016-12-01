package com.icostel.clockview;

import com.icostel.clockview.R;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity implements ClockView.TimeCallback {
    private static final String TAG = MainActivity.class.getSimpleName();

    private ClockView mClockView = null;
    private TextView mHourTextView = null;
    private TextView mMinutesTextView = null;
    private View mHourSelector = null;
    private View mMinutesSelector = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        actionBar.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        setContentView(R.layout.activity_main);
        // custom title face
        SpannableString spanString = new SpannableString(getString(R.string.app_name));
        spanString.setSpan(Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf"), 0, spanString.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Update the action bar title with the TypefaceSpan instance
        actionBar.setTitle(spanString);
        Typeface hourFace = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        Typeface mintesFace = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");
        mHourTextView = (TextView) findViewById(R.id.hourTextView);
        mMinutesTextView = (TextView) findViewById(R.id.minutesTextView);
        mClockView = (ClockView) findViewById(R.id.clockView);
        mHourSelector = findViewById(R.id.hourSelector);
        mMinutesSelector = findViewById(R.id.minutesSelector);
        mHourSelector.setVisibility(View.INVISIBLE);
        mMinutesSelector.setVisibility(View.INVISIBLE);
        mHourTextView.setTypeface(hourFace);
        mMinutesTextView.setTypeface(mintesFace);
        mHourTextView.setText(String.valueOf(mClockView.getHour()));
        mMinutesTextView.setText(String.valueOf(mClockView.getMinutes()));
        mHourTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClockView.setHourVisible(true);
                mHourSelector.setVisibility(View.VISIBLE);
                mMinutesSelector.setVisibility(View.INVISIBLE);
                mClockView.setMinutesVisible(false);
                mClockView.refresh();
            }
        });
        // we start by making the hour visible
        mHourTextView.callOnClick();
        mMinutesTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClockView.setMinutesVisible(true);
                mMinutesSelector.setVisibility(View.VISIBLE);
                mHourSelector.setVisibility(View.INVISIBLE);
                mClockView.setHourVisible(false);
                mClockView.refresh();
            }
        });
        mClockView.addCallback(this);
    }

    @Override
    public void onHourChanged(int hour) {
        if (hour == 0) {
            mHourTextView.setText(getString(R.string.hour_0));
        } else {
            mHourTextView.setText(String.valueOf(hour));
        }
    }

    @Override
    public void onMinuesChanged(int minutes) {
        mMinutesTextView.setText(String.valueOf(minutes));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mClockView.removeCallback(this);
    }
}
