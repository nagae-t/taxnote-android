package com.example.taxnoteandroid.misc;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;

import com.example.taxnoteandroid.R;

public class CustomTabsUtils {

    public enum Content {

        TOP(""),
        UPGRADE("categories/4403835704857"),
        EXPORT("categories/4403835703449"),
        BACKUP("categories/4403829975449"),
        SUBSIDIARY("articles/4403838354329")
        ;

        private final String path;

        private Content(@NonNull String path) {
            this.path = path;
        }
    }

    public static void showHelp(@NonNull Context context, @NonNull Content content) {
        showUrl(context, context.getString(R.string.url_help) + content.path);
    }

    public static void showUrl(@NonNull Context context, @NonNull String url) {
        final CustomTabsIntent tabsIntent = new CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setToolbarColor(ContextCompat.getColor(context, R.color.primary))
                .build();
        tabsIntent.launchUrl(context, Uri.parse(url));
    }
}
