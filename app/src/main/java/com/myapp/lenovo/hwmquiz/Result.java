package com.myapp.lenovo.hwmquiz;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class Result extends AppCompatActivity implements View.OnClickListener {

    private Button btnReset, btnExit;
    TextView TextView4,TextView5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_result);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        Intent intent = getIntent();
        TextView4 = (TextView) findViewById(R.id.tv4);
        TextView4.setText(intent.getStringExtra("1111"));

        TextView5 = (TextView) findViewById(R.id.tv5);
        TextView5.setText(intent.getStringExtra("2222"));


        btnExit = (Button)findViewById(R.id.btnExit);
        btnReset = (Button)findViewById(R.id.btnReset);
        btnExit.setOnClickListener(this);
        btnReset.setOnClickListener(this);

        AdView mAdView = (AdView) findViewById(R.id.adView2);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent preferencesIntent = new Intent(this, SettingsActivity.class);
        startActivity(preferencesIntent);
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnReset: {
                MainActivityFragment quizFragment = (MainActivityFragment)
                        getSupportFragmentManager().findFragmentById(
                                R.id.quizFragment);
                quizFragment.resetQuiz();
                break;
            }
            case R.id.btnExit: {
                this.finishAffinity();
                break;
            }
        }
    }
}
