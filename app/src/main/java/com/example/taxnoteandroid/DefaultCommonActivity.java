package com.example.taxnoteandroid;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.security.keystore.UserNotAuthenticatedException;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;

import com.example.taxnoteandroid.dataManager.ProjectDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.model.Project;

import java.security.InvalidKeyException;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * Created by b0ne on 2017/03/08.
 */

public class DefaultCommonActivity extends AppCompatActivity {

    private static final String KEY_NAME = "taxnote_key";
    private static final byte[] SECRET_BYTE_ARRAY = new byte[]{1, 2, 3, 4, 5, 6};
    private static final int AUTHENTICATION_DURATION_SECONDS = 2;
    private KeyguardManager mKeyguardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeStyle = SharedPreferencesManager.getAppThemeStyle(this);
        setTheme(ProjectDataManager.getThemeStyle(themeStyle));
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        TaxnoteApp app = (TaxnoteApp) getApplication();
        if (app.getAppStatus() == TaxnoteApp.AppStatus.RETURNED_TO_FOREGROUND) {
            // パスコードロック(6系以降のみ対象)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ProjectDataManager projectDataManager = new ProjectDataManager(this);
                Project project = projectDataManager.findCurrent();
                if (project.passcode) {
                    mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                    if (mKeyguardManager.isKeyguardSecure()) {
                        tryEncrypt();
                    }
                }
            }
        }
    }

    /**
     * セキュリティキーの作成.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void createKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setUserAuthenticationValidityDurationSeconds(AUTHENTICATION_DURATION_SECONDS)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 認証画面を表示.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void tryEncrypt() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            SecretKey secretKey = (SecretKey) keyStore.getKey(KEY_NAME, null);
            Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES
                    + "/" + KeyProperties.BLOCK_MODE_CBC
                    + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7
            );
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            cipher.doFinal(SECRET_BYTE_ARRAY);
        } catch (UserNotAuthenticatedException e) {
            // 要認証
            Intent intent = mKeyguardManager.createConfirmDeviceCredentialIntent(null, null);
            if (intent != null) {
                startActivity(intent);
            }
        } catch (KeyPermanentlyInvalidatedException e) {
            // Keys are invalidated after created. Retry the purchase.
            createKey();
            tryEncrypt();
        } catch (InvalidKeyException e) {
            // Keys are not created.
            createKey();
            tryEncrypt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
