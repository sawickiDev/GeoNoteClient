package com.steveq.geonoteclient.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.steveq.geonoteclient.R;


public class NumberPickerPreference extends DialogPreference {
    private static final String TAG = NumberPickerPreference.class.getSimpleName();

    private static final int DEFAULT_MIN_VALUE = 100;
    private static final int DEFAULT_MAX_VALUE = 1000;
    private static final int INCREMENT = 100;

    private final int minValue;
    private final int maxValue;

    SeekBar seekBar;
    TextView currentRadiusTextView;
    private int seekValue;

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.NumberPickerPreference);
        minValue = ta.getInteger(R.styleable.NumberPickerPreference_minValue, DEFAULT_MIN_VALUE);
        maxValue = ta.getInteger(R.styleable.NumberPickerPreference_maxValue, DEFAULT_MAX_VALUE);
        ta.recycle();

        setDialogLayoutResource(R.layout.number_picker_layout);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        setDialogIcon(null);

        seekValue = this.getPersistedInt(100);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, DEFAULT_MIN_VALUE);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        seekValue = restorePersistedValue ? getPersistedInt(DEFAULT_MIN_VALUE) : (Integer)defaultValue;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        seekBar = (SeekBar)view.findViewById(R.id.radiusSeekBar);
        seekBar.setMax(maxValue - minValue);
        seekBar.setProgress(seekValue - minValue);
        currentRadiusTextView = (TextView)view.findViewById(R.id.currentRadiusTextView);
        currentRadiusTextView.setText(getContext().getString(R.string.radius_label, String.valueOf(seekValue)));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                int progress = (i + minValue) / INCREMENT;
                progress = progress * INCREMENT;
                currentRadiusTextView.setText(getContext().getString(R.string.radius_label, String.valueOf(progress)));
                seekValue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        Log.d(TAG, "RESULT :: " + positiveResult);
        if (positiveResult && seekValue != 0) {
            persistInt(seekValue);
        }
    }
}
