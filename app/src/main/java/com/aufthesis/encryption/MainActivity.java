package com.aufthesis.encryption;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.content.res.ResourcesCompat;
import android.view.KeyEvent;
import android.widget.LinearLayout;
import android.widget.TabHost;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;

public class MainActivity extends FragmentActivity implements FragmentTabHost.OnTabChangeListener {

    // Used to load the 'native-lib' library on application startup.
    /**
    static {
        System.loadLibrary("native-lib");
    }
     /**/

    public static final String ENCRYPT_IV = "aufhebensynthese";
    /**
    // ソルト値
    public static final byte[] salt = {(byte)0xc7, (byte)0x73, (byte)0x21,
                                       (byte)0x8c, (byte)0x7e, (byte)0xc8,
                                       (byte)0xee, (byte)0x99};

    // イテレーションカウント値
    public static final int count = 2048;
    /**/
    public static int m_sendAction = 0;
    private CharSequence m_extraTxt;
    private FragmentTabHost m_tabHost;
    //private TabWidget m_tabWidget;
    public static String m_sendMessage;

    //public static int m_selectTabIndex = 0;

    public static SharedPreferences m_prefs;

    private AdView m_adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle fireLogBundle = new Bundle();
        fireLogBundle.putString("TEST", "MyApp MainActivity.onCreate() is called.");
        MyApp.getFirebaseAnalytics().logEvent(FirebaseAnalytics.Event.APP_OPEN, fireLogBundle);
        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
         /**/
        m_prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);

        m_tabHost = findViewById(android.R.id.tabhost);
        m_tabHost.setup(this, getSupportFragmentManager(), R.id.container);

        //m_tabWidget = (TabWidget) findViewById(android.R.id.tabs);

        TabHost.TabSpec tabSpec1, tabSpec2;

        tabSpec1 = m_tabHost.newTabSpec("tab1");
        tabSpec1.setIndicator(getString(R.string.encryption), ResourcesCompat.getDrawable(getResources(), R.drawable.ic_lock_outline_black_24dp, null));
        m_tabHost.addTab(tabSpec1, EncryptFragment.class, null);
        //View tabView1 = m_tabWidget.getChildTabViewAt(0);

        tabSpec2 = m_tabHost.newTabSpec("tab2");
        tabSpec2.setIndicator(getString(R.string.decryption), ResourcesCompat.getDrawable(getResources(), R.drawable.ic_lock_open_black_24dp, null));
        m_tabHost.addTab(tabSpec2, DecryptFragment.class, null);
        //View tabView2 = m_tabWidget.getChildTabViewAt(1);

        //m_tabHost.getTabWidget().setStripEnabled(false);
        //m_tabHost.getTabWidget().setDividerDrawable(null);
        m_tabHost.setOnTabChangedListener(this);

        Intent intent = getIntent();
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action)) {
            // 共有を受けたとき
            Bundle extras = intent.getExtras();
            if (extras != null) {
                m_extraTxt = extras.getCharSequence(Intent.EXTRA_TEXT);
                if (m_extraTxt != null) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setTitle(getString(R.string.sent_title));
                    dialog.setMessage(getString(R.string.sent_message));
                    dialog.setPositiveButton(getString(R.string.sent_decrypt), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            m_sendAction = 2;
                            m_sendMessage = m_extraTxt.toString();
                            m_tabHost.setCurrentTab(1);
                        }
                    });
                    dialog.setNegativeButton(getString(R.string.sent_encrypt), new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            m_sendAction = 1;
                            m_sendMessage = m_extraTxt.toString();
                            m_tabHost.setCurrentTab(1);
                            m_tabHost.setCurrentTab(0);
                        }
                    });
                    dialog.show();
                }
            }
        }
        else
            m_sendMessage = "";
        m_tabHost.setCurrentTab(1);
        m_tabHost.setCurrentTab(0);

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this, "ca-app-pub-1485554329820885~8979939852");

        //バナー広告
        m_adView = findViewById(R.id.adView1);
        AdRequest adRequest = new AdRequest.Builder().build();
        m_adView.loadAd(adRequest);
    }

    @Override
    public void onTabChanged(String tabId) {
        /*
        // 選択タブ（変更前）の背景色
        if(m_selectTabIndex == 0)
            m_tabWidget.getChildTabViewAt(m_selectTabIndex).setBackgroundColor(Color.parseColor("#ffe5e5"));
        else
            m_tabWidget.getChildTabViewAt(m_selectTabIndex).setBackgroundColor(Color.parseColor("#e5ffe5"));

        // 選択タブ
        m_selectTabIndex  = m_tabHost.getCurrentTab();
        // 選択タブ（変更後）の背景色
        if(m_selectTabIndex == 0)
            m_tabWidget.getChildTabViewAt(m_selectTabIndex).setBackgroundColor(Color.parseColor("#c12626"));
        else
            m_tabWidget.getChildTabViewAt(m_selectTabIndex).setBackgroundColor(Color.parseColor("#00c100"));
         */

    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    //public native String stringFromJNI();


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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(getString(R.string.final_title));
            dialog.setMessage(getString(R.string.final_message));
            dialog.setPositiveButton(getString(R.string.final_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            dialog.setNegativeButton(getString(R.string.final_cancel), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            dialog.show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
