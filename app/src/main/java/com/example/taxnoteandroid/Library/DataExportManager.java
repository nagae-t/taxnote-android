package com.example.taxnoteandroid.Library;

import com.example.taxnoteandroid.model.Entry;

import java.util.List;

/**
 * Created by umemotonon on 2016/12/24.
 */

public class DataExportManager {

    //QQ データエクスポートのコードこちらに移動したでそうろう
    public static void export() {

//                EntryDataManager entryDataManager = new EntryDataManager(this);
//                List<Entry> entries = entryDataManager.findAll(this);

    }

    public static String join(CharSequence delimiter, List<Entry> entries) {
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
//        for (Object token: tokens) {
//            if (firstTime) {
//                firstTime = false;
//            } else {
//                sb.append(delimiter);
//            }
//            sb.append(token);
//        }
        return sb.toString();
    }
}
