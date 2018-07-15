package com.mrz3ro.qrbarcodescanner;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.Nullable;

public class SettingsActivity extends PreferenceActivity {

    private final static String TAG = "SettingsAcitivity";

    public SettingsActivity() {}

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new LocationFragment()).commit();
    }
}
