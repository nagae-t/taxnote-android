package com.example.taxnoteandroid.Library;

import android.content.Context;

import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.model.Entry;

import java.util.List;

import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_RANGE_TYPE_ALL;
import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_RANGE_TYPE_CUSTOM;
import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_RANGE_TYPE_LAST_MONTH;
import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_RANGE_TYPE_THIS_MONTH;

/**
 * Created by umemotonon on 2016/12/24.
 */

public class DataExportManager {

    //QQ データエクスポートのコードこちらに移動したでそうろう
    public static void export(Context context) {

    }

    private List<Entry> getEntries(Context context) {

        //@@@ いまここの途中！！！！
        List<Entry> entries;
        EntryDataManager entryDataManager = new EntryDataManager(context);

        String exportRangeType = SharedPreferencesManager.getExportRangeType(context);

        switch(exportRangeType) {

            case EXPORT_RANGE_TYPE_ALL:
                entries = entryDataManager.findAll(context, null);
                break;

            case EXPORT_RANGE_TYPE_THIS_MONTH:
                entries = entryDataManager.findAll(context, null);
                break;

            case EXPORT_RANGE_TYPE_LAST_MONTH:
                entries = entryDataManager.findAll(context, null);
                break;

            case EXPORT_RANGE_TYPE_CUSTOM:

                // Get saved begin and end dates
                long beginDate      = SharedPreferencesManager.getDateRangeBeginDate(context);
                long endDate        = SharedPreferencesManager.getDateRangeEndDate(context);
                long[] start_end    = {beginDate, endDate};

                // Get entries filtered by begin and end dates
                entries = entryDataManager.findAll(context, start_end);
                break;
        }

        return entries;
    }








//    public static String join(CharSequence delimiter, List<Entry> entries) {
//        StringBuilder sb = new StringBuilder();
//        boolean firstTime = true;
////        for (Object token: tokens) {
////            if (firstTime) {
////                firstTime = false;
////            } else {
////                sb.append(delimiter);
////            }
////            sb.append(token);
////        }
//        return sb.toString();
//    }
}
