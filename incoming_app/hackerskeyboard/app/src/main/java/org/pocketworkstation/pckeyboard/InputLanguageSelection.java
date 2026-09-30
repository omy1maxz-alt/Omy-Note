/*
 * Copyright (C) 2008-2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.pocketworkstation.pckeyboard;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InputLanguageSelection extends PreferenceActivity {
    private static final String TAG = "PCKeyboardILS";

    private InputMethodManager mImm;
    private InputMethodInfo mImi;
    private List<InputMethodSubtype> mSubtypes;
    private Set<String> mSelectedLanguages;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.language_prefs);

        mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mImi = findMyImi();

        Preference openSystemSettings = findPreference("open_system_language_settings");
        openSystemSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SUBTYPE_SETTINGS);
                intent.putExtra(Settings.EXTRA_INPUT_METHOD_ID, mImi.getId());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(InputLanguageSelection.this, "Error: could not open system settings", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String selectedLanguagePref = sp.getString(LatinIME.PREF_SELECTED_LANGUAGES, "");
        mSelectedLanguages = new HashSet<>(Arrays.asList(selectedLanguagePref.split(",")));

        mSubtypes = mImm.getEnabledInputMethodSubtypeList(mImi, true);

        PreferenceGroup parent = (PreferenceGroup) findPreference("language_list");
        for (InputMethodSubtype subtype : mSubtypes) {
            CheckBoxPreference pref = new CheckBoxPreference(this);
            String localeString = subtype.getLocale();
            String displayName = subtype.getDisplayName(this, getPackageName(), getApplicationInfo()).toString();
            pref.setTitle(displayName + " [" + localeString + "]");
            pref.setKey(localeString);

            boolean checked = mSelectedLanguages.contains(localeString) || mSelectedLanguages.contains(localeString.substring(0, 2));
            pref.setChecked(checked);
            parent.addPreference(pref);
        }
    }

    private InputMethodInfo findMyImi() {
        if (mImm == null) return null;
        for (InputMethodInfo imi : mImm.getInputMethodList()) {
            if (imi.getPackageName().equals(getPackageName())) {
                return imi;
            }
        }
        return null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        StringBuilder checkedLanguages = new StringBuilder();
        PreferenceGroup parent = (PreferenceGroup) findPreference("language_list");
        int count = parent.getPreferenceCount();
        for (int i = 0; i < count; i++) {
            CheckBoxPreference pref = (CheckBoxPreference) parent.getPreference(i);
            if (pref.isChecked()) {
                checkedLanguages.append(pref.getKey()).append(",");
            }
        }
        String checkedLangsStr = checkedLanguages.length() > 0 ? checkedLanguages.substring(0, checkedLanguages.length() - 1) : "";

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = sp.edit();
        editor.putString(LatinIME.PREF_SELECTED_LANGUAGES, checkedLangsStr);
        SharedPreferencesCompat.apply(editor);
    }
}
