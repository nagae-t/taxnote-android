package com.example.taxnoteandroid.Library;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import com.example.taxnoteandroid.R;
import com.example.taxnoteandroid.TaxnoteConsts;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.model.Entry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import static java.net.Proxy.Type.HTTP;

/**
 * Created by umemotonon on 2016/12/24.
 */

public class DataExportManager implements TaxnoteConsts {

    private static String CHARACTER_CODE_UTF_8     = "UTF-8";
    private static String CHARACTER_CODE_SHIFT_JIS = "Shift_JIS";

    private Context context        = null;
    private String mode            = null;
    private int column_size        = -1; // CSV column size.
    private String[] column_titles = null;
    private Column[] columns       = null;
    private String character_code  = null;
    private String separator       = ",";
    private Entry current_entry    = null;
    private long total_price       = 0;

    public DataExportManager(String mode, String character_code) {

        this.setMode(mode);
        this.setCharacterCode(character_code);
    }

    private void setMode(String mode) {

        this.mode = mode;

        if(mode.compareTo(EXPORT_FORMAT_TYPE_CSV)==0) { // CSV
            intColumns(7); // CSV column size.
            setColumnTitles("日付", "借方勘定", "借方金額", "貸方勘定", "貸方金額", "備考", "残高");
            setColumn(0, new DateColumn());
            setColumn(1, new LeftAccountNameColumn());
            setColumn(2, new LeftAccountPriceColumn());
            setColumn(3, new RightAccountNameColumn());
            setColumn(4, new RightAccountPriceColumn());
            setColumn(5, new SubAccountNameColumn());
            setColumn(6, new TotalPriceColumn()); // 2017/01/25 インデックスの指定を 7 → 6 に変更。
            setSeparator(",");
        }
        else if(mode.compareTo(EXPORT_FORMAT_TYPE_YAYOI)==0) { // 弥生
            intColumns(25); // CSV column size.
            setColumn(0,  new FixedTextColumn("2000")); // TODO ここには何を出力する？
            setColumn(3,  new DateColumn());
            setColumn(4,  new LeftAccountNameColumn());
            setColumn(7,  new FixedTextColumn("対象外")); // TODO ここには何を出力する？
            setColumn(8,  new LeftAccountPriceColumn());
            setColumn(10, new RightAccountNameColumn());
            setColumn(13, new FixedTextColumn("対象外")); // TODO ここには何を出力する？
            setColumn(14, new TotalPriceColumn());
            setColumn(16, new SubAccountNameColumn());
            setColumn(19, new FixedTextColumn("0")); // TODO ここには何を出力する？
            setColumn(24, new FixedTextColumn("NO")); // TODO ここには何を出力する？
            setSeparator("\t");
        }
        else if(mode.compareTo(EXPORT_FORMAT_TYPE_FREEE)==0) { // Freee
            intColumns(14); // CSV column size.
            setColumnTitles("収支区分", "管理番号", "発生日", "支払期日", "取引先", "勘定科目", "税区分", "金額", "備考", "品目", "メモタグ（複数指定可、カンマ区切り）", "支払日", "支払口座" , "支払金額");
            setColumn(0,  new ExpenseDivisionColumn());
            setColumn(2,  new DateColumn());
            setColumn(5,  new LeftAccountPriceColumn());
            setColumn(6,  new FixedTextColumn("対象外")); // TODO ここには何を出力する？
            setColumn(7,  new LeftAccountNameColumn());
            setColumn(8,  new SubAccountNameColumn());
            setColumn(11, new DateColumn()); // TODO ここには何を出力する？
            setColumn(12, new RightAccountNameColumn());
            setColumn(13, new RightAccountPriceColumn()); // TODO ここには何を出力する？
            setSeparator(",");
        }
        else if(mode.compareTo(EXPORT_FORMAT_TYPE_MFCLOUD)==0) { // MF Could
            intColumns(13); // CSV column size.
            setColumnTitles("取引No", "取引日", "借方勘定科目", "借方補助科目", "借方税区分", "借方金額(円)", "貸方勘定科目", "貸方補助科目", "貸方税区分", "貸方金額(円)", "備考", "仕訳メモ");
            setColumn(0,  new IndexColumn()); // TODO ここには何を出力する？
            setColumn(1,  new DateColumn());
            setColumn(2,  new LeftAccountNameColumn());
            setColumn(5,  new LeftAccountPriceColumn());
            setColumn(6,  new RightAccountNameColumn());
            setColumn(9,  new RightAccountPriceColumn());
            setColumn(10, new SubAccountNameColumn());
            setSeparator(",");
        }
        else {
            throw new RuntimeException("Invalid Mode : " + mode);
        }
    }

    public void export(final Context context) {

        this.context = context;

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
            File file = getOutputFile();
            if(file.getParentFile().exists()==false) file.getParentFile().mkdirs(); // If parent folder doesn't exist, create it.
            stream_out = new FileOutputStream(file);
            writer = new PrintWriter(new OutputStreamWriter(stream_out, character_code));

            List<Entry> entries = getSelectedRangeEntries(context); // Get a list of Entry.

            // Output column titles.

            if(column_titles!=null) {
                writer.append(getCSVString(column_titles, separator) + "\n");
            }

            // Out put data rows.

            String[] line = new String[column_size];

            for(Entry entry : entries) {

                setCurrentEntry(entry);
                resetArray(line);

                for(int i=0;i<line.length;i++) {
                    if(columns[i]!=null) {
                        line[i] = columns[i].getValue();
                    }
                }

                writer.append(getCSVString(line, ",") + "\n");
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

    private File getOutputFile() {

        String file_name = "taxnote";

        // Mode

        if(mode.compareTo(EXPORT_FORMAT_TYPE_CSV)==0) { // CSV
            file_name += "_BasicCSV";
        }
        else if(mode.compareTo(EXPORT_FORMAT_TYPE_YAYOI)==0) { // 弥生
            file_name += "_Yayoi";
        }
        else if(mode.compareTo(EXPORT_FORMAT_TYPE_FREEE)==0) { // Freee
            file_name += "_Free";
        }
        else if(mode.compareTo(EXPORT_FORMAT_TYPE_MFCLOUD)==0) { // MF Could
            file_name += "_MFCloud";
        }
        else {
            throw new RuntimeException("Invalid Mode : " + mode);
        }

        // Period

        file_name += "_AllDate"; // TODO ここで、指定された期間にあった文字列をセットするようにします。

        // Character code

        if(this.character_code.compareTo(CHARACTER_CODE_UTF_8)==0) {
            file_name += "_utf8";
        }
        else if(this.character_code.compareTo(CHARACTER_CODE_SHIFT_JIS)==0) {
            file_name += "_shift_jis";
        }

        // File extension

        if(mode.compareTo(EXPORT_FORMAT_TYPE_YAYOI)==0) { // 弥生
            file_name += ".txt";
        } else {
            file_name += ".csv";
        }

        return new File(Environment.getExternalStorageDirectory(), "Taxnote/" + System.currentTimeMillis() + "/" + file_name);
    }

    private void setCurrentEntry(Entry entry) {
        current_entry = entry;
        total_price = (entry.isExpense ? -entry.price : entry.price);
    }

    private void resetArray(String[] array) {

        for(int i=0;i<array.length;i++) {
            array[i] = null;
        }
    }

    private String getCSVString(String[] array, String separator) {

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

        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setAction(Intent.ACTION_SEND);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setType("vnd.android.cursor.item/email"); // 2017/01/25 E.Nozaki intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {"eiichi.nozaki@gmail.com"}); // TODO ここに送信先メールアドレスを指定します。
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Taxnote");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, "これは Taxnote から送信したファイルです。");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));

        List activities = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        if(activities!=null && activities.size()>0) { // 2017/01/25 Check if there is available mailer.
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "有効なメーラーがありません。", Toast.LENGTH_LONG);
        }

        context.startActivity(Intent.createChooser(intent, "メールを送信"));
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

    private void intColumns(int column_size) {
        this.column_size   = column_size;
        this.columns       = new Column[column_size];
        this.column_titles = null;
    }

    private void setColumnTitles(String... column_titles) {
        this.column_titles = column_titles;
    }

    private void setSeparator(String separator) {
        this.separator = separator;
    }

    private void setColumn(int index, Column column) {
        this.columns[index] = column;
    }

    private abstract class Column {
        public abstract String getValue();
    }

    private class DateColumn extends Column{

        SimpleDateFormat simpleDateFormat = null;

        @Override
        public String getValue() {

            if(simpleDateFormat==null) {
                simpleDateFormat = new SimpleDateFormat(context.getResources().getString(R.string.date_string_format_to_month_day));
            }

            return simpleDateFormat.format(current_entry.date);
        }
    }

    private class LeftAccountNameColumn extends Column{
        @Override
        public String getValue() {
            return (current_entry.isExpense ? current_entry.reason.name : current_entry.account.name);
        }
    }

    private class LeftAccountPriceColumn extends Column{
        @Override
        public String getValue() {
            return Long.toString(current_entry.price);
        }
    }

    private class RightAccountNameColumn extends Column {
        @Override
        public String getValue() {
            return (current_entry.isExpense ? current_entry.account.name : current_entry.reason.name);
        }
    }

    private class RightAccountPriceColumn extends Column {
        @Override
        public String getValue() {
            return Long.toString(current_entry.price);
        }
    }

    private class SubAccountNameColumn extends Column {
        @Override
        public String getValue() {
            return current_entry.memo;
        }
    }

    private class TotalPriceColumn extends Column {
        @Override
        public String getValue() {
            return Long.toString(total_price);
        }
    }

    private class ExpenseDivisionColumn extends Column{
        @Override
        public String getValue() {
            return (current_entry.isExpense ? "支出" : "収入");
        }
    }

    private class FixedTextColumn extends Column{

        private String text = null;

        public FixedTextColumn(String text) {
            this.text = text;
        }

        @Override
        public String getValue() {
            return text;
        }
    }

    private class IndexColumn extends Column{
        @Override
        public String getValue() {
            return Long.toString(current_entry.id);
        }
    }

    private void setCharacterCode(String code) {

        if(code.compareTo(EXPORT_CHARACTER_CODE_UTF8)==0) {
            this.character_code = CHARACTER_CODE_UTF_8;
        }
        else if(code.compareTo(EXPORT_CHARACTER_CODE_SHIFTJIS)==0) {
            this.character_code = CHARACTER_CODE_SHIFT_JIS;
        }
        else {
            throw new RuntimeException("Invalid Character Code : " + code);
        }
    }
}
