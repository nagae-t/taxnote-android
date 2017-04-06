package com.example.taxnoteandroid.Library;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.taxnoteandroid.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.api.services.androidpublisher.model.SubscriptionPurchase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static java.util.Collections.singleton;

// AndroidPublisher.Purchases.Subscription

/**
 * Created by b0ne on 2017/04/06.
 */

public class TNGoogleApiClient {
    private static final String CLIENT_EMAIL = "";

    private GoogleApiClient mApiClient;
    private AndroidPublisher mPublisher;

    private Context mContext;

    public TNGoogleApiClient(Context context) {
        this.mContext = context;

        HttpTransport httpTransport = new NetHttpTransport.Builder().build();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        GoogleCredential credential = null;
        try {
            //@@ raw に p12 ファイルを追加
            InputStream inputStream = context.getResources()
                    .openRawResource(R.raw.default_reason);
            File p12file = createFileFromInputStream(inputStream);

            credential = new GoogleCredential.Builder()
                    .setTransport(httpTransport)
                    .setJsonFactory(jsonFactory)
                    .setServiceAccountId(CLIENT_EMAIL)
                    .setServiceAccountScopes(singleton(AndroidPublisherScopes.ANDROIDPUBLISHER))
                    .setServiceAccountPrivateKeyFromP12File(p12file)
                    .build();
        } catch (Exception e) {
            Log.e("ERROR", "TNGoogleApiClient init : " + e.getLocalizedMessage());
        }

        if (credential != null) {
            mPublisher = new AndroidPublisher.Builder(
                    httpTransport, jsonFactory, credential)
                    .setApplicationName("taxnote")
                    .build();
        }
    }

    public SubscriptionPurchase getSubscription(String subscriptionId, String purchaseToken) {
        if (mApiClient == null || mPublisher == null) return null;

        SubscriptionPurchase subs = null;
        try {
            AndroidPublisher.Purchases.Subscriptions.Get get =
                    mPublisher.purchases().subscriptions().get(
                        "package_name", subscriptionId, purchaseToken);
            subs = get.execute();

        } catch (IOException e) {
            Log.e("ERROR", "getSubscription : " + e.getLocalizedMessage());
        }
        if (subs != null) {
            // subscriptionId
            Log.v("TEST", "getSubs: subscriptionId: " + subscriptionId);
            Log.v("TEST", "getSubs: autoRenewing: " + subs.getAutoRenewing());
            Log.v("TEST", "getSubs: paymentState: " + subs.getPaymentState());
            Log.v("TEST", "getSubs: startTimeMillis: " + subs.getStartTimeMillis());
            Log.v("TEST", "getSubs: expiryTimeMillis: " + subs.getExpiryTimeMillis());
            return subs;
        }
        return null;
    }

    // http://stackoverflow.com/questions/30888783/key-p12-open-failed-enoent-no-such-file-or-directory
    private File createFileFromInputStream(InputStream inputStream) {

        File file = new File(Environment.getExternalStorageDirectory(),
                "KeyHolder/KeyFile/");
        if (!file.exists()) {
            if (!file.mkdirs())
                Log.d("KeyHolder", "Folder not created");
            else
                Log.d("KeyHolder", "Folder created");
        } else
            Log.d("KeyHolder", "Folder present");

        String path = file.getAbsolutePath();

        try {
            File f = new File(path+"/MyKey");
            OutputStream outputStream = new FileOutputStream(f);
            byte buffer[] = new byte[1024];
            int length = 0;

            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return f;
        } catch (IOException e) {
            // Logging exception
            Log.e("ERROR", "createFileFromInputStream : " + e.getLocalizedMessage());
        }

        Log.v("TEST", "createFileFromInputStream return null ");

        return null;
    }
}
