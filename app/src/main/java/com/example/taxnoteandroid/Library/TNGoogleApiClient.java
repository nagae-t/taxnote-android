package com.example.taxnoteandroid.Library;

import android.content.Context;
import android.util.Log;

import com.example.taxnoteandroid.R;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.api.services.androidpublisher.model.SubscriptionPurchase;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

/**
 * Created by b0ne on 2017/04/06.
 */

public class TNGoogleApiClient {

    private AndroidPublisher mPublisher;

    private Context mContext;

    public TNGoogleApiClient(Context context) {
        this.mContext = context;
    }

    public SubscriptionPurchase getSubscription(String subscriptionId, String purchaseToken) {
        if (mPublisher == null) {

            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            HttpTransport httpTransport = new NetHttpTransport.Builder().build();
            try {
                InputStream inputStream = mContext.getResources()
                        .openRawResource(R.raw.taxnote_google_api_client);

                GoogleCredential credential = GoogleCredential.fromStream(
                        inputStream, httpTransport, jsonFactory);
                credential = credential.createScoped(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER));

                mPublisher = new AndroidPublisher.Builder(
                        httpTransport, jsonFactory, credential)
                        .setApplicationName("taxnote")
                        .build();
            } catch (Exception e) {
                Log.e("ERROR", "TNGoogleApiClient init : " + e.getLocalizedMessage());
            }

            if (mPublisher == null) return null;
        }

        String packageName = mContext.getPackageName();
        SubscriptionPurchase subs = null;
        try {
            AndroidPublisher.Purchases.Subscriptions.Get get =
                    mPublisher.purchases().subscriptions().get(
                            packageName, subscriptionId, purchaseToken);
            subs = get.execute();

        } catch (IOException e) {
            Log.e("ERROR", "getSubscription : " + e.getLocalizedMessage());
        }
        if (subs != null) {
            return subs;
        }
        return null;
    }

}
