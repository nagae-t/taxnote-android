package com.example.taxnoteandroid.Library;

import android.content.Context;
import android.util.Log;

import com.example.taxnoteandroid.Library.zeny.ZNUtils;
import com.example.taxnoteandroid.R;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

import static com.example.taxnoteandroid.TaxnoteConsts.MIXPANEL_TOKEN;

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
        MixpanelAPI mixpanel =
                MixpanelAPI.getInstance(mContext, MIXPANEL_TOKEN);
        JSONObject props = new JSONObject();
        try {
            props.put("subscriptionId", subscriptionId);
            props.put("purchaseToken", purchaseToken);
        } catch (JSONException e) {
        }
        if (mPublisher == null) {
            try {
                props.put("mPublisher", null);
            } catch (JSONException e) {
            }

            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            HttpTransport httpTransport = new NetHttpTransport.Builder().build();
            try {
                InputStream inputStream;
                if ( ZNUtils.isZeny() ) {
                    inputStream = mContext.getResources()
                            .openRawResource(R.raw.zeny_google_api_client);
                } else {
                    inputStream = mContext.getResources()
                            .openRawResource(R.raw.taxnote_google_api_client);
                }

                GoogleCredential credential = GoogleCredential.fromStream(
                        inputStream, httpTransport, jsonFactory);
                credential = credential.createScoped(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER));

                mPublisher = new AndroidPublisher.Builder(
                        httpTransport, jsonFactory, credential)
                        .setApplicationName(mContext.getString(R.string.app_name))
                        .build();
            } catch (Exception e) {
                Log.e("ERROR", "TNGoogleApiClient init : " + e.getLocalizedMessage());
                try {
                    props.put("ERROR1", "TNGoogleApiClient init : " + e.getLocalizedMessage());
                } catch (JSONException je) {
                }
            }

            if (mPublisher == null) {
                mixpanel.track("TNGoogleApiClient getSubscription mPublisher == null", props);
                return null;
            }
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
            try {
                props.put("ERROR2", "getSubscription : " + e.getLocalizedMessage());
            } catch (JSONException je) {
            }
        }
        if (subs != null) {
            mixpanel.track("TNGoogleApiClient getSubscription", props);
            return subs;
        }
        mixpanel.track("TNGoogleApiClient getSubscription subs == null", props);
        return null;
    }

}
