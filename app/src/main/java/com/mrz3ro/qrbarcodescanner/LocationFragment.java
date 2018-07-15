package com.mrz3ro.qrbarcodescanner;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

public class LocationFragment extends PreferenceFragment {

    private final static String beep = "beep";
    private final static String frontCamera = "frontCamera";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}
