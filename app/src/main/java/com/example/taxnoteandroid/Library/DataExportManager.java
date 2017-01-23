package com.example.taxnoteandroid.Library;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.provider.DocumentFile;
import android.widget.Toast;

import com.example.taxnoteandroid.TaxnoteConsts;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.model.Entry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by umemotonon on 2016/12/24.
 */

public class DataExportManager implements TaxnoteConsts {

    private String MODE = EXPORT_FORMAT_TYPE_CSV;

    private int size_csv_column = 7; // CSV column size.

    private int index_date                = -1; // 日付を出力するコラムのインデックス（一番左なら0、左から5番目なら4を指定）
    private int index_left_account_name   = -1; // 貸方勘定の項目名を出力するコラムのインデックス（一番左なら0、左から5番目なら4を指定）
    private int index_left_account_price  = -1; // 貸方勘定の金額を出力するコラムのインデックス（一番左なら0、左から5番目なら4を指定）
    private int index_right_account_name  = -1; // 借勘定の項目名を出力するコラムのインデックス（一番左なら0、左から5番目なら4を指定）
    private int index_right_account_price = -1; // 借方勘定の項目名を出力するコラムのインデックス（一番左なら0、左から5番目なら4を指定）
    private int index_sub_account_name    = -1; // 備考を出力するコラムのインデックス（一番左なら0、左から5番目なら4を指定）
    private int index_total_price         = -1; // 残高を出力するコラムのインデックス（一番左なら0、左から5番目なら4を指定）

    public DataExportManager(String mode) {

        if(mode.compareTo(EXPORT_FORMAT_TYPE_CSV)==0) {
            setCsvColumnSize(7); // CSV column size.
            setIndex_Date(0);
            setIndex_LeftAccountName(1);
            setIndex_LeftAccountPrice(2);
            setIndex_RightAccountName(3);
            setIndex_RightAccountPrice(4);
            setIndex_SubAccountName(5);
            setIndex_TotalPrice(6);
        }
        else if(mode.compareTo(EXPORT_FORMAT_TYPE_YAYOI)==0) {
            // TODO ここに、弥生用の CSV ファイルのフォーマットを指定します。
            // TODO この作業は、このクラスの仕様が確定した後に行います。
            setCsvColumnSize(7); // CSV column size.
            setIndex_Date(0);
            setIndex_LeftAccountName(1);
            setIndex_LeftAccountPrice(2);
            setIndex_RightAccountName(3);
            setIndex_RightAccountPrice(4);
            setIndex_SubAccountName(5);
            setIndex_TotalPrice(6);
        }
        else if(mode.compareTo(EXPORT_FORMAT_TYPE_FREEE)==0) {
            // TODO ここに、Freee用の CSV ファイルのフォーマットを指定してください。
            // TODO この作業は、このクラスの仕様が確定した後に行います。
            setCsvColumnSize(7); // CSV column size.
            setIndex_Date(0);
            setIndex_LeftAccountName(1);
            setIndex_LeftAccountPrice(2);
            setIndex_RightAccountName(3);
            setIndex_RightAccountPrice(4);
            setIndex_SubAccountName(5);
            setIndex_TotalPrice(6);
        }
        else if(mode.compareTo(EXPORT_FORMAT_TYPE_MFCLOUD)==0) {
            // TODO ここに、MS Cloud用の CSV ファイルのフォーマットを指定してください。
            // TODO この作業は、このクラスの仕様が確定した後に行います。
            setCsvColumnSize(7); // CSV column size.
            setIndex_Date(0);
            setIndex_LeftAccountName(1);
            setIndex_LeftAccountPrice(2);
            setIndex_RightAccountName(3);
            setIndex_RightAccountPrice(4);
            setIndex_SubAccountName(5);
            setIndex_TotalPrice(6);
        }
        else {
            throw new RuntimeException("Invalid Mode : " + mode);
        }
    }

    public void export(final Context context) {

        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage("CSVファイルの出力");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.show();

        AsyncTask<Object, Object, File> task= new AsyncTask<Object, Object, File>() {

            @Override
            protected File doInBackground(Object... objects) {
                return generateCsvFile(context);
            }

            @Override
            protected void onPostExecute(File file) {
                super.onPostExecute(file);
                dialog.cancel();

                if(file!=null) {
                    sendFileByEmail(context, file);
                } else {
                    Toast.makeText(context, "エラーが発生しました。CSVファイルを出力できません。", Toast.LENGTH_LONG);
                }

            }
        };

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private File generateCsvFile(Context context) {

        OutputStream stream_out = null;
        PrintWriter writer = null;

        try {
            File file = new File(Environment.getExternalStorageDirectory(), "Taxnote/" + System.currentTimeMillis() + "/taxnote_BasicCSV_AllDate_utf8.csv");
            if(file.getParentFile().exists()==false) file.getParentFile().mkdirs(); // If parent folder doesn't exist, create it.
            stream_out = new FileOutputStream(file);
            writer = new PrintWriter(new OutputStreamWriter(stream_out,"UTF-8"));

            long total = 0;

            List<Entry> entries = getSelectedRangeEntries(context); // Get a list of Entry.

            String[] csv_line = new String[size_csv_column];

            // Output column titles.

            csv_line[index_date]                = "日付";
            csv_line[index_left_account_name]   = "借方勘定";
            csv_line[index_left_account_price]  = "借方金額";
            csv_line[index_right_account_name]  = "貸方勘定";
            csv_line[index_right_account_price] = "貸方金額";
            csv_line[index_sub_account_name]    = "備考";
            csv_line[index_total_price]         = "残高";

            writer.append(getCSVString(csv_line, ",") + "\n");

            // Out put data rows.

            for(Entry entry : entries) {

                resetArray(csv_line);

                total = (entry.isExpense ? -entry.price : entry.price);

                csv_line[index_date]                = new Date(entry.date).toString();
                csv_line[index_left_account_name]   = (entry.isExpense ? entry.reason.name : entry.account.name);
                csv_line[index_left_account_price]  = Long.toString(entry.price);
                csv_line[index_right_account_name]  = (entry.isExpense ? entry.account.name : entry.reason.name);
                csv_line[index_right_account_price] = Long.toString(entry.price);
                csv_line[index_sub_account_name]    = entry.memo;
                csv_line[index_total_price]         = Long.toString(total);

                writer.append(getCSVString(csv_line, ",") + "\n");
            }

            return file;

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {

            try {
                if(writer!=null) {
                    writer.close();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }

            try {
                if(stream_out!=null) {
                    stream_out.close();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public void resetArray(String[] array) {

        for(int i=0;i<array.length;i++) {
            array[i] = null;
        }
    }

    public String getCSVString(String[] array, String separator) {

        StringBuilder builder = new StringBuilder();

        for(int i=0;i<array.length;i++) {

            if(array[i]!=null) {
                builder.append(array[i]);
            } else {
                builder.append("");
            }

            if(i!=array.length-1) {
                builder.append(separator);
            }
        }

        return builder.toString();
    }

    private void sendFileByEmail(Context context, File file) {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"eiichi.nozaki@gmail.com"}); // TODO ここに送信先メールアドレスを指定します。
        intent.putExtra(Intent.EXTRA_SUBJECT, "Taxnote");
        intent.putExtra(Intent.EXTRA_TEXT, "これは Taxnote から送信したファイルです。");
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        context.startActivity(intent);
    }

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
}
