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
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.taxnoteandroid.dataManager.ProjectDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * Created by b0ne on 2017/03/08.
 */

public class DefaultCommonActivity extends AppCompatActivity {

    private static final String KEY_NAME = "taxnote_key";
    private static final byte[] SECRET_BYTE_ARRAY = new byte[] {1, 2, 3, 4, 5, 6};
    private static final int AUTHENTICATION_DURATION_SECONDS = 10;
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
            mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (!mKeyguardManager.isKeyguardSecure()) {
                return;
            }
            createKey();
            tryEncrypt();
        }
    }

    /**
     * セキュリティキーの作成.
     */
    private void createKey() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
            } catch (NoSuchAlgorithmException | NoSuchProviderException
                    | InvalidAlgorithmParameterException | KeyStoreException
                    | CertificateException | IOException e) {
                // TODO: 例外発生時の挙動
                throw new RuntimeException("Failed to create a symmetric key", e);
            }
        }
    }

    /**
     * 認証画面を表示.
     */
    private void tryEncrypt() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
                // セキュリティキーが無効
                Toast.makeText(this, "Keys are invalidated after created. Retry the purchase\n"
                                + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                // TODO: 例外発生時の挙動
                throw new RuntimeException(e);
            }
        } else {
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
            } catch (Exception e) {
                // UserNotAuthenticatedException, KeyPermanentlyInvalidatedException が使えないため、
                // 例外発生 = 要認証、とする
                Intent intent = mKeyguardManager.createConfirmDeviceCredentialIntent(null, null);
                if (intent != null) {
                    startActivity(intent);
                }
            }
        }
    }
}
