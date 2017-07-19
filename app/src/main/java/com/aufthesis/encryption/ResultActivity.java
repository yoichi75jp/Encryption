package com.aufthesis.encryption;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

/**
 // Created by yoichi75jp2 on 2016/09/29.
 */

public class ResultActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView m_resultText;
    Context m_context;
    String m_result;

    private AdView m_adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        m_context = this;

        Intent intent = getIntent();
        m_result = intent.getExtras().getString("result");
        m_resultText = (TextView)findViewById(R.id.result_text);
        m_resultText.setText(m_result);

        Button button = (Button)findViewById(R.id.copy);
        button.setOnClickListener(this);
        button = (Button)findViewById(R.id.share);
        button.setOnClickListener(this);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        //バナー広告
        m_adView = (AdView) findViewById(R.id.adView2);
        AdRequest adRequest = new AdRequest.Builder().build();
        m_adView.loadAd(adRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_result, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                this.induceReview();
                // アニメーションの設定
                overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.copy:
                // Gets a handle to the clipboard service.
                ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                // Creates a new text clip to put on the clipboard
                ClipData clip = ClipData.newPlainText("result",m_resultText.getText().toString());
                clipboard.setPrimaryClip(clip);

                Toast toast = Toast.makeText(m_context, getString(R.string.copied),Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                break;

            case R.id.share:
                try
                {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, m_result);
                    //shareIntent.putExtra(Intent.EXTRA_SUBJECT, "暗号文");
                    startActivity(shareIntent);
                }
                catch (Exception ex)
                {
                    String s = ex.getMessage();
                    int i = 0;
                }
                break;

        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (m_adView != null) {
            m_adView.resume();
        }
    }

    @Override
    public void onPause() {
        if (m_adView != null) {
            m_adView.pause();
        }
        super.onPause();
    }
    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onDestroy()
    {
        if (m_adView != null) {
            m_adView.destroy();
        }
        super.onDestroy();
        setResult(RESULT_OK);
    }

    public void onBackPressed()
    {
        this.induceReview();
    }

    private void induceReview()
    {
        int count = MainActivity.m_prefs.getInt(getString(R.string.count_induce), 0);
        count++;
        if(count >= 50)
        {
            count = 0;
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(getString(R.string.induce_title));
            dialog.setMessage(getString(R.string.induce_message));
            dialog.setPositiveButton(getString(R.string.induce_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent googlePlayIntent = new Intent(Intent.ACTION_VIEW);
                    googlePlayIntent.setData(Uri.parse("market://details?id=com.aufthesis.encryption"));
                    startActivity(googlePlayIntent);
                }
            });
            dialog.setNegativeButton(getString(R.string.induce_cancel), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            dialog.show();
        }
        SharedPreferences.Editor editor = MainActivity.m_prefs.edit();
        editor.putInt(getString(R.string.count_induce), count);
        editor.apply();
        finish();
    }

}
