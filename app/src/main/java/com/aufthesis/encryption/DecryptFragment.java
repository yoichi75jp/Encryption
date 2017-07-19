package com.aufthesis.encryption;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import android.widget.TextView;

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

public class DecryptFragment extends Fragment {

    String m_cipher_text = "";
    EditText m_passwordEdit;
    TextView m_decryptEdit;
    Button m_execButton;
    Button m_pasteButton;
    Button m_clearButton;

    private List<Integer> m_listID = new ArrayList<>(Arrays.asList(1,2,3));
    private InterstitialAd m_InterstitialAd;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_decrypt, container, false);
        // AdMobインターステイシャル
        m_InterstitialAd = new InterstitialAd(getActivity());
        m_InterstitialAd.setAdUnitId(getString(R.string.adUnitInterId));
        m_InterstitialAd.loadAd(new AdRequest.Builder().build());
        m_InterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                //if (m_InterstitialAd.isLoaded()) {
                 //   m_InterstitialAd.show();
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
        m_passwordEdit = (EditText)getActivity().findViewById(R.id.password);
        m_decryptEdit = (TextView)getActivity().findViewById(R.id.cipher_text);
        m_execButton = (Button)getActivity().findViewById(R.id.exec);
        m_clearButton = (Button)getActivity().findViewById(R.id.clear_decrypt);
        m_pasteButton = (Button)getActivity().findViewById(R.id.paste_decrypt);

        m_execButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Snackbar.make(view, "Exec 解読", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                String decryption;
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
                        decryption = decrypt(m_decryptEdit.getText().toString(), m_passwordEdit.getText().toString());

                        Intent intent = new Intent(getActivity(), ResultActivity.class);
                        intent.putExtra("result", decryption);
                        int requestCode = 2;
                        startActivityForResult(intent, requestCode);
                        // アニメーションの設定
                        getActivity().overridePendingTransition(R.animator.slide_in_right, R.animator.slide_out_left);
                    }
                }
                catch(Exception exp)
                {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                    dialog.setTitle(getString(R.string.incorrect_title));
                    dialog.setMessage(getString(R.string.incorrect_message));
                    dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            m_passwordEdit.setText("");
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
            }
        });

        m_pasteButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ClipboardManager clipboard = (ClipboardManager)getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = clipboard.getPrimaryClip();
                //クリップデータからItemを取得
                if(clip != null){
                    ClipData.Item item = clip.getItemAt(0);
                    m_decryptEdit.setText(item.getText());
                    m_cipher_text = m_decryptEdit.getText().toString();
                }
            }
        });
        m_clearButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                m_decryptEdit.setText("");
                m_passwordEdit.setText("");
                m_cipher_text = m_decryptEdit.getText().toString();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if(MainActivity.m_sendAction == 2)
        {
            m_decryptEdit.setText(MainActivity.m_sendMessage);
            m_cipher_text = m_decryptEdit.getText().toString();
            MainActivity.m_sendAction = 0;
        }
        m_decryptEdit.setText(m_cipher_text);
    }


    /**
     * 復号メソッド
     *
     * @param ciphertext 復号する文字列
     * @param password パスワード
     * @return 復号文字列
     */
    public String decrypt(String ciphertext, String password) throws Exception{
        // 変数初期化
        String strResult = null;
        try {
            // 復号用の秘密鍵を生成
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

            // 復号準備
            Cipher cDec = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cDec.init(Cipher.DECRYPT_MODE, key, iv);

            // 復号
            byte[] output = cDec.doFinal(Base64.decode(ciphertext, Base64.DEFAULT));

            strResult = new String(output);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            showMessage("error",e.getMessage());
        }
        // 復号文字列を返却
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


    private void showMessage(String title, String message)
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialog.show();
    }
}
