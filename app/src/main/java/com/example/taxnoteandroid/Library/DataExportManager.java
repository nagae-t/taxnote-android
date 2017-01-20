package com.example.taxnoteandroid.Library;

import android.content.Context;

import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.model.Entry;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_RANGE_TYPE_ALL;
import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_RANGE_TYPE_CUSTOM;
import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_RANGE_TYPE_LAST_MONTH;
import static com.example.taxnoteandroid.TaxnoteConsts.EXPORT_RANGE_TYPE_THIS_MONTH;

/**
 * Created by umemotonon on 2016/12/24.
 */

public class DataExportManager {

    private int size_csv_column = 7; // CSVのコラム数

    private int index_date                 = -1;  // 日付を出力するコラムのインデックス（一番左なら0、左から5番目なら4を指定）
    private int index_left_account_name   = -1; // 貸方勘定の項目名を出力するコラムのインデックス（一番左なら0、左から5番目なら4を指定）
    private int index_left_account_price  = -1; // 貸方勘定の金額を出力するコラムのインデックス（一番左なら0、左から5番目なら4を指定）
    private int index_right_account_name  = -1; // 借勘定の項目名を出力するコラムのインデックス（一番左なら0、左から5番目なら4を指定）
    private int index_right_account_price = -1; // 借方勘定の項目名を出力するコラムのインデックス（一番左なら0、左から5番目なら4を指定）
    private int index_sub_account_name    = -1; // 備考を出力するコラムのインデックス（一番左なら0、左から5番目なら4を指定）
    private int index_total_price          = -1; // 残高を出力するコラムのインデックス（一番左なら0、左から5番目なら4を指定）

  public void setCsvColumnSize(int size_csv_column) {
    this.size_csv_column = size_csv_column;
  }

  public void setIndex_Date(int index_date) {
    this.index_date = index_date;
  }

  public void setIndex_LeftAccountName(int index_left_account_name) {
    this.index_left_account_name = index_left_account_name;
  }

  public void setIndex_LeftAccountPrice(int index_left_account_price) {
    this.index_left_account_price = index_left_account_price;
  }

  public void setIndex_RightAccountName(int index_right_account_name) {
    this.index_right_account_name = index_right_account_name;
  }

  public void setIndex_RightAccountPrice(int index_right_account_price) {
    this.index_right_account_price = index_right_account_price;
  }

  public void setIndex_SubAccountName(int index_sub_account_name) {
    this.index_sub_account_name = index_sub_account_name;
  }

  public void setIndex_TotalPrice(int index_total_price) {
    this.index_total_price = index_total_price;
  }

  public void export(Context context) {

        long total = 0;

        List<Entry> entries = getSelectedRangeEntries(context);

        String[] csv_line = new String[size_csv_column];

        for(Entry entry : entries) {

          total = (entry.isExpense ? -entry.price : entry.price);

          csv_line[index_date] = new Date(entry.date).toString();
          csv_line[index_left_account_name] = (entry.isExpense ? entry.reason.name : entry.account.name);
          csv_line[index_left_account_price] = Long.toString(entry.price);
          csv_line[index_right_account_name] = (entry.isExpense ? entry.account.name : entry.reason.name);
          csv_line[index_right_account_price] = Long.toString(entry.price);
          csv_line[index_sub_account_name] = entry.memo;
          csv_line[index_total_price] = Long.toString(total);
        }
    }

    public List<Entry> getSelectedRangeEntries(Context context) {

        List<Entry> entries;
        long[] start_end;
        EntryDataManager entryDataManager = new EntryDataManager(context);

        String exportRangeType = SharedPreferencesManager.getExportRangeType(context);

        switch(exportRangeType) {

            case EXPORT_RANGE_TYPE_ALL:
                entries     = entryDataManager.findAll(context, null);
                break;

            case EXPORT_RANGE_TYPE_THIS_MONTH:
                start_end   = getThisMonthStartAndEndDate();
                entries     = entryDataManager.findAll(context, start_end);
                break;

            case EXPORT_RANGE_TYPE_LAST_MONTH:
                start_end   = getLastMonthStartAndEndDate();
                entries     = entryDataManager.findAll(context, start_end);
                break;

            case EXPORT_RANGE_TYPE_CUSTOM:
                start_end   = getCustomStartAndEndDate(context);
                entries     = entryDataManager.findAll(context, start_end);
                break;

            default:
                entries = entryDataManager.findAll(context, null);
                break;
        }

        return entries;
    }

    private static long[] getThisMonthStartAndEndDate() {

        long now            = System.currentTimeMillis();
        Calendar calendar   = Calendar.getInstance(TimeZone.getDefault());

        calendar.setTimeInMillis(now);
        calendar.set(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        long time_start = calendar.getTimeInMillis();
        calendar.add(Calendar.MONTH, 1);
        long time_end = calendar.getTimeInMillis();

        long[] start_end = {time_start, time_end};

        return start_end;
    }

    private static long[] getLastMonthStartAndEndDate() {

        long now            = System.currentTimeMillis();
        Calendar calendar   = Calendar.getInstance(TimeZone.getDefault());

        calendar.setTimeInMillis(now);
        calendar.set(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        long time_end = calendar.getTimeInMillis();
        calendar.add(Calendar.MONTH, - 1);
        long time_start = calendar.getTimeInMillis();

        long[] start_end = {time_start, time_end};

        return start_end;
    }

    private static long[] getCustomStartAndEndDate(Context context) {

        // Get saved begin and end dates
        long beginDate      = SharedPreferencesManager.getDateRangeBeginDate(context);
        long endDate        = SharedPreferencesManager.getDateRangeEndDate(context);

        // Add one day
        Calendar calendar   = Calendar.getInstance(TimeZone.getDefault());
        calendar.setTimeInMillis(endDate);
        calendar.add(Calendar.HOUR, 24);
        endDate = calendar.getTimeInMillis();

        long[] start_end = {beginDate, endDate};

        return start_end;
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

  // 使い方のサンプル。

  public void test(Context context) {

    DataExportManager manager = new DataExportManager();

    manager.setCsvColumnSize(6); // CSVの横の長さ

    manager.setIndex_Date(0);
    manager.setIndex_LeftAccountName(1);
    manager.setIndex_LeftAccountPrice(2);
    manager.setIndex_RightAccountName(3);
    manager.setIndex_RightAccountPrice(4);

    manager.export(context); // これで出力 → メール送信！

  }
}
