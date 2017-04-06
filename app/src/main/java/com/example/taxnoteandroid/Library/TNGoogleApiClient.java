package com.example.taxnoteandroid.Library;

import android.content.Context;

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
import java.io.IOException;
import java.security.GeneralSecurityException;

import static java.util.Collections.singleton;

// AndroidPublisher.Purchases.Subscription

/**
 * Created by b0ne on 2017/04/06.
 */

public class TNGoogleApiClient {
    private static final String CLIENT_EMAIL = "";
    private GoogleApiClient mApiClient;
    private AndroidPublisher mPublisher;

    public TNGoogleApiClient(Context context) {

        HttpTransport httpTransport = new NetHttpTransport.Builder().build();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        GoogleCredential credential = null;
        try {
            credential = new GoogleCredential.Builder()
                    .setTransport(httpTransport)
                    .setJsonFactory(jsonFactory)
                    .setServiceAccountId(CLIENT_EMAIL)
                    .setServiceAccountScopes(singleton(AndroidPublisherScopes.ANDROIDPUBLISHER))
                    .setServiceAccountPrivateKeyFromP12File(new File("file.p12"))
                    .build();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (credential != null) {
            mPublisher = new AndroidPublisher.Builder(
                    httpTransport, jsonFactory, credential)
                    .setApplicationName("taxnote")
                    .build();
        }
    }

    public void getSubscription(String subscriptionId, String purchaseToken) {
        if (mApiClient == null || mPublisher == null) return;

        SubscriptionPurchase subscription = null;
        try {
            AndroidPublisher.Purchases.Subscriptions.Get get =
                    mPublisher.purchases().subscriptions().get(
                        "package_name", subscriptionId, purchaseToken);
            subscription = get.execute();

        } catch (IOException e) {
            e.printStackTrace();
        }
        if (subscription != null) {
            
        }
    }
}
