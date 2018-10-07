package com.myapp.lenovo.hwmquiz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.Set;

public class MainActivity extends AppCompatActivity {
    public static final String CHOICES = "pref_numberOfChoices";
    public static final String QUESTIONS = "pref_numberOfQuestions";
    public static final String FRACTIONS = "pref_regionsToInclude";

    private boolean preferencesChanged = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        PreferenceManager.getDefaultSharedPreferences(this).
                registerOnSharedPreferenceChangeListener(
                        preferencesChangeListener);

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-8789957081409372~5487594246");

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (preferencesChanged) {
            MainActivityFragment quizFragment = (MainActivityFragment)
                    getSupportFragmentManager().findFragmentById(
                            R.id.quizFragment);
            quizFragment.updateGuessRows(
                    PreferenceManager.getDefaultSharedPreferences(this));
            quizFragment.updateFractions(
                    PreferenceManager.getDefaultSharedPreferences(this));
            quizFragment.resetQuiz();
            preferencesChanged = false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent preferencesIntent = new Intent(this, SettingsActivity.class);
                startActivity(preferencesIntent);
                return true;
            case R.id.renew:
                MainActivityFragment quizFragment = (MainActivityFragment)
                        getSupportFragmentManager().findFragmentById(
                                R.id.quizFragment);
                quizFragment.resetQuiz();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private OnSharedPreferenceChangeListener preferencesChangeListener =
            new OnSharedPreferenceChangeListener() {
                // called when the user changes the app's preferences
                @Override
                public void onSharedPreferenceChanged(
                        SharedPreferences sharedPreferences, String key) {
                    MainActivityFragment quizFragment = (MainActivityFragment)
                            getSupportFragmentManager().findFragmentById(
                                    R.id.quizFragment);
                    if (key.equals(QUESTIONS)) {
                        quizFragment.updateNumberOfQuestions(sharedPreferences);
                        quizFragment.resetQuiz();

                        Toast.makeText(MainActivity.this,
                                R.string.restarting_quiz,
                                Toast.LENGTH_SHORT).show();
                    }
                    if (key.equals(CHOICES)) {
                        quizFragment.updateGuessRows(sharedPreferences);
                        quizFragment.loadButtons();
                    }
                    else if (key.equals(FRACTIONS)) {
                        Set<String> fractions =
                                sharedPreferences.getStringSet(FRACTIONS, null);

                        if (fractions != null && fractions.size() > 0) {
                            quizFragment.updateFractions(sharedPreferences);
                            Toast.makeText(MainActivity.this,
                                    R.string.restarting_quiz,
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            SharedPreferences.Editor editor =
                                    sharedPreferences.edit();
                            fractions.add(getString(R.string.default_fraction));
                            editor.putStringSet(FRACTIONS, fractions);
                            editor.apply();

                            Toast.makeText(MainActivity.this,
                                    R.string.default_fraction_message,
                                    Toast.LENGTH_LONG).show();
                        }
                        quizFragment.resetQuiz();
                    }
                }
            };
}

