package com.aufthesis.encryption;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


/**
 // Created by yoichi75jp2 on 2016/09/27.
 */

public class EncryptFragment extends Fragment {

    EditText m_passwordEdit;
    EditText m_messageEdit;
    Button m_execButton;
    Button m_clearButton;

    private List<Integer> m_listID = new ArrayList<>(Arrays.asList(1,2,3));
    private InterstitialAd m_InterstitialAd;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_encrypt, container, false);

        // AdMobインターステイシャル
        m_InterstitialAd = new InterstitialAd(getActivity());
        m_InterstitialAd.setAdUnitId(getString(R.string.adUnitInterId));
        m_InterstitialAd.loadAd(new AdRequest.Builder().build());
        m_InterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                //if (m_InterstitialAd.isLoaded()) {
                    //m_InterstitialAd.show();
                //}
            }
            @Override
            public void onAdClosed() {
                m_InterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        m_passwordEdit = getActivity().findViewById(R.id.password);
        m_messageEdit = getActivity().findViewById(R.id.message);
        m_execButton = getActivity().findViewById(R.id.exec);
        m_clearButton = getActivity().findViewById(R.id.clear_encrypt);

        m_execButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Snackbar.make(view, "Exec 暗号化", Snackbar.LENGTH_LONG)
                //       .setAction("Action", null).show();
                String encryption;
                try
                {
                    String password = m_passwordEdit.getText().toString();
                    if(password.length() < 4)
                    {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                        dialog.setTitle("Password");
                        dialog.setMessage(getString(R.string.password_message));
                        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int count = MainActivity.m_prefs.getInt(getString(R.string.count_miss), 0);
                                count++;
                                if(count >= 3)
                                {
                                    Collections.shuffle(m_listID);
                                    if(m_listID.get(0) == 1)
                                    {
                                        count = 0;
                                        if (m_InterstitialAd.isLoaded()) {
                                            m_InterstitialAd.show();
                                        }
                                    }
                                }
                                SharedPreferences.Editor editor = MainActivity.m_prefs.edit();
                                editor.putInt(getString(R.string.count_miss), count);
                                editor.apply();
                            }
                        });
                        dialog.show();
                    }
                    else
                    {
                        encryption = encrypt(m_messageEdit.getText().toString(), password);
                        Intent intent = new Intent(getActivity(), ResultActivity.class);
                        intent.putExtra("result", encryption);
                        int requestCode = 1;
                        startActivityForResult(intent, requestCode);
                        // アニメーションの設定
                        getActivity().overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
                        m_messageEdit.setText("");  //メッセージ消滅
                    }
                }
                catch(Exception exp)
                {
                    String exception = exp.getMessage();
                    int i = 0;
                }
            }
        });

        m_clearButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                m_messageEdit.setText("");
                m_passwordEdit.setText("");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(MainActivity.m_sendAction == 1)
        {
            m_messageEdit.setText(MainActivity.m_sendMessage);
            MainActivity.m_sendAction = 0;
        }
    }

    /**
     * 暗号化メソッド
     *
     * @param message 暗号化する文字列
     * @param password パスワード
     * @return 暗号化文字列
     */
    public String encrypt(String message, String password) throws Exception{
        // 変数初期化
        String strResult = null;
        try {
            // パスワードから秘密鍵を生成
            byte[] pass1 = password.getBytes();
            byte[] pass2 = new byte[32];
            for(int i = 0; i < 32; i++)
            {
                if(i < pass1.length)
                    pass2[i] = pass1[i];
                else
                    pass2[i] = 0x00;
            }
            SecretKeySpec key = new SecretKeySpec(pass2, "AES");
            IvParameterSpec iv = new IvParameterSpec(MainActivity.ENCRYPT_IV.getBytes());

            // 暗号化準備
            Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            pbeCipher.init(Cipher.ENCRYPT_MODE, key, iv);

            // クリアテキスト
            byte[] cleartext = message.getBytes();

            // 暗号化
            byte[] ciphertext = pbeCipher.doFinal(cleartext);

            strResult = Base64.encodeToString(ciphertext, Base64.DEFAULT);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        // 暗号化文字列を返却
        return strResult;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        FragmentTabHost tabHost = (FragmentTabHost)getActivity().findViewById(android.R.id.tabhost);
        m_passwordEdit.setText("");
        switch (requestCode) {
            case 1:
                tabHost.setCurrentTab(1);
                break;
            case 2:
                tabHost.setCurrentTab(0);
                break;
        }
    }

}
