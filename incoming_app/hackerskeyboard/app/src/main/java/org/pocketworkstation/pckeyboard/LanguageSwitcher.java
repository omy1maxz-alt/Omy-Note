/*
 * Copyright (C) 2010 Google Inc.
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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Keeps track of list of selected input languages and the current
 * input language that the user has selected.
 */
public class LanguageSwitcher {

    private static final String TAG = "HK/LanguageSwitcher";
    private ArrayList<InputMethodSubtype> mSubtypes = new ArrayList<InputMethodSubtype>();
    private LatinIME mIme;
    private InputMethodManager mImm;
    private InputMethodInfo mImi;

    private int mCurrentIndex = 0;
    private String mDefaultInputLanguage;
    private Locale mDefaultInputLocale;
    private Locale mSystemLocale;
    private String mSelectedLanguages;

    // Languages for which auto-caps should be disabled
    public static final Set<String> NOCAPS_LANGUAGES = new HashSet<String>();
    static {
        NOCAPS_LANGUAGES.add("ar");
        NOCAPS_LANGUAGES.add("iw");
        NOCAPS_LANGUAGES.add("th");
    }

    // Languages which should not use dead key logic. The modifier is entered after the base character.
    public static final Set<String> NODEADKEY_LANGUAGES = new HashSet<String>();
    static {
        NODEADKEY_LANGUAGES.add("ar");
        NODEADKEY_LANGUAGES.add("iw"); // TODO: currently no niqqud in the keymap?
        NODEADKEY_LANGUAGES.add("th");
    }

    // Languages which should not auto-add space after completions
    public static final Set<String> NOAUTOSPACE_LANGUAGES = new HashSet<String>();
    static {
        NOAUTOSPACE_LANGUAGES.add("th");
    }


    public LanguageSwitcher(LatinIME ime) {
        mIme = ime;
        mImm = (InputMethodManager) ime.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (mImm != null) {
            for (InputMethodInfo imi : mImm.getInputMethodList()) {
                if (imi.getPackageName().equals(ime.getPackageName())) {
                    mImi = imi;
                    break;
                }
            }
        }
    }

    public int getLocaleCount() {
        return mSubtypes.size();
    }

    public boolean loadLocales(SharedPreferences sp) {
        String selectedLanguages = sp.getString(LatinIME.PREF_SELECTED_LANGUAGES, null);
        String currentLanguage = sp.getString(LatinIME.PREF_INPUT_LANGUAGE, null);

        if (selectedLanguages == null) {
            loadDefaults();
            mSubtypes.clear();
            return true;
        }

        if (selectedLanguages.equals(mSelectedLanguages)) {
            return false;
        }
        mSelectedLanguages = selectedLanguages;

        Set<String> selectedLanguageSet = new HashSet<>(Arrays.asList(selectedLanguages.split(",")));
        List<InputMethodSubtype> enabledSubtypes = new ArrayList<>();
        if (mImm != null && mImi != null) {
             enabledSubtypes = mImm.getEnabledInputMethodSubtypeList(mImi, true);
        }

        mSubtypes.clear();
        if (enabledSubtypes != null) {
            for (InputMethodSubtype subtype : enabledSubtypes) {
                String localeString = subtype.getLocale();
                String lang = localeString.substring(0, 2);
                if (selectedLanguageSet.contains(localeString) || selectedLanguageSet.contains(lang)) {
                    mSubtypes.add(subtype);
                }
            }
        }

        if (mSubtypes.isEmpty()) {
            loadDefaults();
            return true;
        }

        mCurrentIndex = 0;
        if (currentLanguage != null) {
            for (int i = 0; i < mSubtypes.size(); i++) {
                if (mSubtypes.get(i).getLocale().equals(currentLanguage)) {
                    mCurrentIndex = i;
                    break;
                }
            }
        }
        return true;
    }

    private void loadDefaults() {
        mDefaultInputLocale = mIme.getResources().getConfiguration().locale;
        String country = mDefaultInputLocale.getCountry();
        mDefaultInputLanguage = mDefaultInputLocale.getLanguage() +
                (TextUtils.isEmpty(country) ? "" : "_" + country);
    }

    public String getInputLanguage() {
        if (getLocaleCount() == 0) return mDefaultInputLanguage;
        return mSubtypes.get(mCurrentIndex).getLocale();
    }

    public Locale getInputLocale() {
        if (getLocaleCount() == 0) {
            return mDefaultInputLocale;
        }
        String localeString = mSubtypes.get(mCurrentIndex).getLocale();
        String[] parts = localeString.split("_", -1);
        Locale locale;
        if (parts.length == 1) {
            locale = new Locale(parts[0]);
        } else if (parts.length >= 2) {
            locale = new Locale(parts[0], parts[1]);
        } else {
            locale = mDefaultInputLocale;
        }
        LatinIME.sKeyboardSettings.inputLocale = (locale != null) ? locale : Locale.getDefault();
        return locale;
    }

    public void setCurrentSubtype(InputMethodSubtype subtype) {
        if (subtype == null) return;
        for (int i = 0; i < mSubtypes.size(); i++) {
            if (mSubtypes.get(i).equals(subtype)) {
                mCurrentIndex = i;
                persist();
                return;
            }
        }
    }

    public boolean allowAutoCap() {
        String lang = getInputLanguage();
        if (lang != null && lang.length() > 2) lang = lang.substring(0, 2);
        return !NOCAPS_LANGUAGES.contains(lang);
    }

    public boolean allowDeadKeys() {
        String lang = getInputLanguage();
        if (lang != null && lang.length() > 2) lang = lang.substring(0, 2);
        return !NODEADKEY_LANGUAGES.contains(lang);
    }

    public boolean allowAutoSpace() {
        String lang = getInputLanguage();
        if (lang != null && lang.length() > 2) lang = lang.substring(0, 2);
        return !NOAUTOSPACE_LANGUAGES.contains(lang);
    }

    public Locale getNextInputLocale() {
        if (getLocaleCount() == 0) return mDefaultInputLocale;
        int nextIndex = (mCurrentIndex + 1) % mSubtypes.size();
        String localeString = mSubtypes.get(nextIndex).getLocale();
        String[] parts = localeString.split("_", -1);
        if (parts.length == 1) {
            return new Locale(parts[0]);
        } else {
            return new Locale(parts[0], parts[1]);
        }
    }

    public void setSystemLocale(Locale locale) {
        mSystemLocale = locale;
    }

    public Locale getSystemLocale() {
        return mSystemLocale;
    }

    public Locale getPrevInputLocale() {
        if (getLocaleCount() == 0) return mDefaultInputLocale;
        int prevIndex = (mCurrentIndex - 1 + mSubtypes.size()) % mSubtypes.size();
        String localeString = mSubtypes.get(prevIndex).getLocale();
        String[] parts = localeString.split("_", -1);
        if (parts.length == 1) {
            return new Locale(parts[0]);
        } else {
            return new Locale(parts[0], parts[1]);
        }
    }

    public void reset() {
        mCurrentIndex = 0;
        mSelectedLanguages = "";
        loadLocales(PreferenceManager.getDefaultSharedPreferences(mIme));
    }

    public void next() {
        if (getLocaleCount() > 0) {
            mCurrentIndex = (mCurrentIndex + 1) % mSubtypes.size();
        }
    }

    public void prev() {
        if (getLocaleCount() > 0) {
            mCurrentIndex = (mCurrentIndex - 1 + mSubtypes.size()) % mSubtypes.size();
        }
    }

    public void persist() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mIme);
        Editor editor = sp.edit();
        editor.putString(LatinIME.PREF_INPUT_LANGUAGE, getInputLanguage());
        SharedPreferencesCompat.apply(editor);
    }

    static String toTitleCase(String s) {
        if (s == null || s.length() == 0) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
