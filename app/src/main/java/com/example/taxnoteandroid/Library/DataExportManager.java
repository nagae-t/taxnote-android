package com.example.taxnoteandroid.Library;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import com.example.taxnoteandroid.CommonEntryRecyclerAdapter;
import com.example.taxnoteandroid.R;
import com.example.taxnoteandroid.TaxnoteConsts;
import com.example.taxnoteandroid.dataManager.AccountDataManager;
import com.example.taxnoteandroid.dataManager.EntryDataManager;
import com.example.taxnoteandroid.dataManager.ProjectDataManager;
import com.example.taxnoteandroid.dataManager.ReasonDataManager;
import com.example.taxnoteandroid.dataManager.RecurringDataManager;
import com.example.taxnoteandroid.dataManager.SharedPreferencesManager;
import com.example.taxnoteandroid.dataManager.SummaryDataManager;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.Entry;
import com.example.taxnoteandroid.model.Project;
import com.example.taxnoteandroid.model.Reason;
import com.example.taxnoteandroid.model.Recurring;
import com.example.taxnoteandroid.model.Summary;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by umemotonon on 2016/12/24.
 */

public class DataExportManager implements TaxnoteConsts {

    private static String CHARACTER_CODE_UTF_8 = "UTF-8";
    private static String CHARACTER_CODE_SHIFT_JIS = "Shift_JIS";

    private Activity mActivity;
    private Context context = null;
    private String mode = null;
    private int columnSize = -1; // CSV column size.
    private String[] columnTitles = null;
    private Column[] columns = null;
    private String characterCode = null;
    private String separator = ",";
    private Entry currentEntry = null;
    private long totalPrice = 0;
    private long[] startEndDate;
    private List<Entry> reportData;

    public DataExportManager(Activity activity) {
        this.mActivity = activity;
        this.context = activity.getApplicationContext();
    }


    public DataExportManager(Activity activity, String mode, String character_code) {
        this.mActivity = activity;
        this.context = activity.getApplicationContext();

        this.setMode(mode);
        this.setCharacterCode(character_code);
    }

    // 損益表の出力用
    public DataExportManager(Activity activity, String charCode, long[] startEndDate, List<Entry> data) {
        this.mActivity = activity;
        this.context = activity.getApplicationContext();
        this.startEndDate = startEndDate;
        this.reportData = data;
        this.setMode(EXPORT_PROFIT_LOSS_FORMAT_TYPE_CSV);
        this.setCharacterCode(charCode);
    }

    private static long[] getThisMonthStartAndEndDate() {

        long now = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

        calendar.setTimeInMillis(now);
        calendar.set(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        long timeStart = calendar.getTimeInMillis();
        calendar.add(Calendar.MONTH, 1);
        long timeEnd = calendar.getTimeInMillis();

        return new long[]{timeStart, timeEnd};
    }

    private static long[] getLastMonthStartAndEndDate() {

        long now = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

        calendar.setTimeInMillis(now);
        calendar.set(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        long timeEnd = calendar.getTimeInMillis();
        calendar.add(Calendar.MONTH, -1);
        long timeStart = calendar.getTimeInMillis();

        return new long[]{timeStart, timeEnd};
    }

    private static long[] getCustomStartAndEndDate(Context context) {

        // Get saved begin and end dates
        long beginDate = SharedPreferencesManager.getDateRangeBeginDate(context);
        long endDate = SharedPreferencesManager.getDateRangeEndDate(context);

        // Add one day
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.setTimeInMillis(endDate);
        calendar.add(Calendar.HOUR, 24);
        endDate = calendar.getTimeInMillis();

        return new long[]{beginDate, endDate};
    }

    private void setMode(String mode) {

        this.mode = mode;

        if (mode.compareTo(EXPORT_FORMAT_TYPE_CSV) == 0) { // CSV

            //@@ 英語にする時翻訳必要
            intColumns(6); // CSV column size.
            setColumnTitles("日付", "借方勘定", "借方金額", "貸方勘定", "貸方金額", "備考");
            setColumn(0, new DateColumn());
            setColumn(1, new LeftAccountNameColumn());
            setColumn(2, new LeftAccountPriceColumn());
            setColumn(3, new RightAccountNameColumn());
            setColumn(4, new RightAccountPriceColumn());
            setColumn(5, new SubAccountNameColumn());
//            setColumn(6, new TotalPriceColumn());
            setSeparator(",");

        } else if (mode.compareTo(EXPORT_FORMAT_TYPE_YAYOI) == 0) { // 弥生

            intColumns(25); // CSV column size.
            setColumn(0, new FixedTextColumn("2000"));
            setColumn(3, new DateColumn());
            setColumn(4, new LeftAccountNameColumn());
            setColumn(7, new FixedTextColumn("対象外"));
            setColumn(8, new LeftAccountPriceColumn());
            setColumn(10, new RightAccountNameColumn());
            setColumn(13, new FixedTextColumn("対象外"));
            setColumn(14, new RightAccountPriceColumn());
            setColumn(16, new SubAccountNameColumn());
            setColumn(19, new FixedTextColumn("0"));
            setColumn(24, new FixedTextColumn("NO"));
            setSeparator("\t");

        } else if (mode.compareTo(EXPORT_FORMAT_TYPE_FREEE) == 0) { // Freee

            intColumns(14); // CSV column size.
            setColumnTitles("収支区分", "管理番号", "発生日", "支払期日", "取引先", "勘定科目", "税区分", "金額", "備考", "品目", "メモタグ（複数指定可、カンマ区切り）", "支払日", "支払口座", "支払金額");
            setColumn(0, new ExpenseDivisionColumn());
            setColumn(2, new DateColumn());
            setColumn(5, new ReasonNameColumn());
            setColumn(6, new FixedTextColumn("対象外"));
            setColumn(7, new LeftAccountPriceColumn());
            setColumn(8, new SubAccountNameColumn());
            setColumn(11, new DateColumn());
            setColumn(12, new AccountNameColumn());
            setColumn(13, new RightAccountPriceColumn());
            setSeparator(",");

        } else if (mode.compareTo(EXPORT_FORMAT_TYPE_MFCLOUD) == 0) { // MF Could

            intColumns(13); // CSV column size.
            setColumnTitles("取引No", "取引日", "借方勘定科目", "借方補助科目", "借方税区分", "借方金額(円)", "貸方勘定科目", "貸方補助科目", "貸方税区分", "貸方金額(円)", "備考", "仕訳メモ", "");
            setColumn(0, new IndexColumn());
            setColumn(1, new DateColumn());
            setColumn(2, new LeftAccountNameColumn());
            setColumn(5, new LeftAccountPriceColumn());
            setColumn(6, new RightAccountNameColumn());
            setColumn(9, new RightAccountPriceColumn());
            setColumn(10, new SubAccountNameColumn());
            setSeparator(",");
        } else if (mode.compareTo(EXPORT_PROFIT_LOSS_FORMAT_TYPE_CSV) == 0) { // 損益表の出力
            intColumns(2); // CSV column size.
            String title = context.getString(R.string.profit_loss_export);
            title += " " + getReportStartEndDateString();
            setColumnTitles(title, "");
            setColumn(0, new ReasonNameColumn());
            setColumn(1, new TotalPriceColumn());
            setSeparator(",");
        } else {
            throw new RuntimeException("Invalid Mode : " + mode);
        }
    }

    public void export() {

        // Progress dialog
        final ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(context.getString(R.string.data_export));
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.show();

        AsyncTask<Object, Object, File> task = new AsyncTask<Object, Object, File>() {

            @Override
            protected File doInBackground(Object... objects) {
                return generateCsvFile(context);
            }

            @Override
            protected void onPostExecute(File file) {
                super.onPostExecute(file);
                dialog.cancel();

                if (file != null) {
                    sendFileByEmail(context, file);
                } else {
                    DialogManager.showOKOnlyAlert(context, context.getString(R.string.Error), context.getString(R.string.data_export_cant_make_csv));
                }
            }
        };

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private File generateCsvFile(Context context) {

        OutputStream streamOut = null;
        PrintWriter writer = null;

        try {
            File file = getOutputFile(context);
            if (file.getParentFile().exists() == false)
                file.getParentFile().mkdirs(); // If parent folder doesn't exist, create it.
            streamOut = new FileOutputStream(file);
            writer = new PrintWriter(new OutputStreamWriter(streamOut, characterCode));

            List<Entry> entries = getSelectedRangeEntries(context); // Get a list of Entry.

            // Output column titles.

            if (columnTitles != null) {
                writer.append(getCSVString(columnTitles, separator) + "\n");
            }

            // Out put data rows.

            String[] line = new String[columnSize];

            // 損益表の出力の場合
            if (mode.equals(EXPORT_PROFIT_LOSS_FORMAT_TYPE_CSV)) {
                entries = reportData;
            }

            for (Entry entry : entries) {

                setCurrentEntry(entry);
                resetArray(line);

                for (int i = 0; i < line.length; i++) {
                    if (columns[i] != null) {
                        line[i] = columns[i].getValue();
                    }
                }

                writer.append(getCSVString(line, ",") + "\n");
            }

            return file;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (streamOut != null) {
                    streamOut.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private File getOutputFile(Context context) {

        String fileName = "taxnote";

        // Mode
        if (mode.compareTo(EXPORT_FORMAT_TYPE_CSV) == 0 ||
                mode.compareTo(EXPORT_PROFIT_LOSS_FORMAT_TYPE_CSV) == 0) { // CSV
            fileName += "_BasicCSV";
        } else if (mode.compareTo(EXPORT_FORMAT_TYPE_YAYOI) == 0) { // 弥生
            fileName += "_Yayoi";
        } else if (mode.compareTo(EXPORT_FORMAT_TYPE_FREEE) == 0) { // Freee
            fileName += "_Free";
        } else if (mode.compareTo(EXPORT_FORMAT_TYPE_MFCLOUD) == 0) { // MF Could
            fileName += "_MFCloud";
        } else {
            throw new RuntimeException("Invalid Mode : " + mode);
        }

        // Period
        String exportRangeType = SharedPreferencesManager.getExportRangeType(context);

        if (mode.compareTo(EXPORT_PROFIT_LOSS_FORMAT_TYPE_CSV) == 0) {
            fileName += getCustomDateRangeStrings();
        } else if (exportRangeType.equals(EXPORT_RANGE_TYPE_ALL)) {
            fileName += "_AllDate";
        } else if (exportRangeType.equals(EXPORT_RANGE_TYPE_THIS_MONTH)) {
            fileName += "_ThisMonth";
        } else if (exportRangeType.equals(EXPORT_RANGE_TYPE_LAST_MONTH)) {
            fileName += "_LastMonth";
        } else {
            fileName += getCustomDateRangeStrings();
        }

        // Character code
        if (mode.compareTo(EXPORT_FORMAT_TYPE_CSV) == 0 ||
                mode.compareTo(EXPORT_PROFIT_LOSS_FORMAT_TYPE_CSV) == 0) { // CSV
            if (this.characterCode.compareTo(CHARACTER_CODE_UTF_8) == 0) {
                fileName += "_utf8";
            } else if (this.characterCode.compareTo(CHARACTER_CODE_SHIFT_JIS) == 0) {
                fileName += "_shift_jis";
            }
        } else if (mode.compareTo(EXPORT_FORMAT_TYPE_YAYOI) == 0) { // 弥生
            fileName += "_shift_jis";
        } else if (mode.compareTo(EXPORT_FORMAT_TYPE_FREEE) == 0) { // Freee
            fileName += "_utf8";
        } else if (mode.compareTo(EXPORT_FORMAT_TYPE_MFCLOUD) == 0) { // MF Could
            fileName += "_utf8";
        }

        // File extension
        if (mode.compareTo(EXPORT_FORMAT_TYPE_YAYOI) == 0) { // 弥生
            fileName += ".txt";
        } else {
            fileName += ".csv";
        }

        return new File(Environment.getExternalStorageDirectory(), "Taxnote/" + System.currentTimeMillis() + "/" + fileName);

//        return new File(context.getCacheDir(), "Taxnote/" + System.currentTimeMillis() + "/" + fileName);
    }

    private String getCustomDateRangeStrings() {

        long[] startAndEndDate = getCustomStartAndEndDate(context);

        // 損益表の場合
        if (mode.compareTo(EXPORT_PROFIT_LOSS_FORMAT_TYPE_CSV) == 0) {
            startAndEndDate = startEndDate;
        }

        long startDate  = startAndEndDate[0];
        long endDate    = startAndEndDate[1];

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                context.getResources().getString(R.string.date_string_format_for_custom_range),
                Locale.getDefault());

        String startDateString  = simpleDateFormat.format(startDate);
        String endDateString    = simpleDateFormat.format(endDate);

        return "_" + startDateString + "~" + endDateString;
    }

    private String getReportStartEndDateString() {
        long startDate  = startEndDate[0];
        long endDate    = startEndDate[1];

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                context.getResources().getString(R.string.date_string_format_to_year_month_day),
                Locale.getDefault());

        String startDateString  = simpleDateFormat.format(startDate);
        String endDateString    = simpleDateFormat.format(endDate);

        return startDateString + "~" + endDateString;
    }

    private void setCurrentEntry(Entry entry) {
        currentEntry = entry;
        totalPrice = (entry.isExpense ? -entry.price : entry.price);
        if (mode.equals(EXPORT_PROFIT_LOSS_FORMAT_TYPE_CSV)) {
            totalPrice = entry.price;
            return;
        }
    }

    private void resetArray(String[] array) {

        for (int i = 0; i < array.length; i++) {
            array[i] = null;
        }
    }

    private String getCSVString(String[] array, String separator) {

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < array.length; i++) {

            if (array[i] != null) {
                builder.append(array[i]);
            } else {
                builder.append("");
            }

            if (i != array.length - 1) {
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
        intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{""});

        String subjectString = context.getString(R.string.data_export_mail_title);
        if (mode.equals(EXPORT_PROFIT_LOSS_FORMAT_TYPE_CSV)) {
            subjectString = "Taxnote " + context.getString(R.string.profit_loss_export)
                + " (" +getReportStartEndDateString()+ ")";
        }

        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subjectString);
        intent.putExtra(android.content.Intent.EXTRA_TEXT, getBodyMessage());

        // TODO FileProviderを使えばいけるかも。
        // https://developer.android.com/reference/android/support/v4/content/FileProvider.html
        // https://developer.android.com/about/versions/nougat/android-7.0-changes.html#perm
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));

        List activities = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        if (activities != null && activities.size() > 0) { // 2017/01/25 Check if there is available mailer.
            context.startActivity(intent);
        } else {
            DialogManager.showOKOnlyAlert(context, context.getString(R.string.Error), context.getString(R.string.data_export_no_valid_mailer));
        }

        context.startActivity(Intent.createChooser(intent, context.getString(R.string.data_export)));
    }

    private String getBodyMessage() {

        String message;

        // Set message
        if (mode.compareTo(EXPORT_FORMAT_TYPE_YAYOI) == 0) {
            message = context.getString(R.string.data_export_mail_message_yayoi);
        } else if (mode.compareTo(EXPORT_FORMAT_TYPE_FREEE) == 0) {
            message = context.getString(R.string.data_export_mail_message_freee);
        } else if (mode.compareTo(EXPORT_FORMAT_TYPE_MFCLOUD) == 0) {
            message = context.getString(R.string.data_export_mail_message_mfcloud);
        } else {
            message = context.getString(R.string.data_export_mail_message_csv);
        }

        return message;
    }

    private List<Entry> getSelectedRangeEntries(Context context) {

        List<Entry> entries;
        long[] startEnd;
        EntryDataManager entryDataManager = new EntryDataManager(context);

        String exportRangeType = SharedPreferencesManager.getExportRangeType(context);

        switch (exportRangeType) {

            case EXPORT_RANGE_TYPE_ALL:
                entries = entryDataManager.findAll(context, null, true);
                break;

            case EXPORT_RANGE_TYPE_THIS_MONTH:
                startEnd = getThisMonthStartAndEndDate();
                entries = entryDataManager.findAll(context, startEnd, true);
                break;

            case EXPORT_RANGE_TYPE_LAST_MONTH:
                startEnd = getLastMonthStartAndEndDate();
                entries = entryDataManager.findAll(context, startEnd, true);
                break;

            case EXPORT_RANGE_TYPE_CUSTOM:
                startEnd = getCustomStartAndEndDate(context);
                entries = entryDataManager.findAll(context, startEnd, true);
                break;

            case EXPORT_PROFIT_LOSS_FORMAT_TYPE_CSV: //@@ 多分使わない
                return new ArrayList<>();

            default:
                entries = entryDataManager.findAll(context, null, true);
                break;
        }

        return entries;
    }


    //--------------------------------------------------------------//
    //    -- Set Column --
    //--------------------------------------------------------------//

    private void intColumns(int columnSize) {
        this.columnSize = columnSize;
        this.columns = new Column[columnSize];
        this.columnTitles = null;
    }

    private void setColumnTitles(String... columnTitles) {
        this.columnTitles = columnTitles;
    }

    private void setSeparator(String separator) {
        this.separator = separator;
    }

    private void setColumn(int index, Column column) {
        this.columns[index] = column;
    }

    private void setCharacterCode(String code) {

        if (mode.compareTo(EXPORT_FORMAT_TYPE_CSV) == 0
                || mode.compareTo(EXPORT_PROFIT_LOSS_FORMAT_TYPE_CSV) == 0) { // CSV

            if (code.compareTo(EXPORT_CHARACTER_CODE_UTF8) == 0) {
                this.characterCode = CHARACTER_CODE_UTF_8;
            } else if (code.compareTo(EXPORT_CHARACTER_CODE_SHIFTJIS) == 0) {
                this.characterCode = CHARACTER_CODE_SHIFT_JIS;
            }

        } else if (mode.compareTo(EXPORT_FORMAT_TYPE_YAYOI) == 0) { // 弥生
            this.characterCode = CHARACTER_CODE_SHIFT_JIS;

        } else if (mode.compareTo(EXPORT_FORMAT_TYPE_FREEE) == 0) { // Freee
            this.characterCode = CHARACTER_CODE_UTF_8;

        } else if (mode.compareTo(EXPORT_FORMAT_TYPE_MFCLOUD) == 0) { // MF Could
            this.characterCode = CHARACTER_CODE_UTF_8;

        } else {
            throw new RuntimeException("Invalid Mode : " + mode);
        }
    }

    private abstract class Column {
        public abstract String getValue();
    }

    private class DateColumn extends Column {

        SimpleDateFormat simpleDateFormat = null;

        @Override
        public String getValue() {

            if (simpleDateFormat == null) {
                simpleDateFormat = new SimpleDateFormat(context.getResources().getString(R.string.date_string_format_for_export));
            }

            return simpleDateFormat.format(currentEntry.date);
        }
    }

    private class LeftAccountNameColumn extends Column {
        @Override
        public String getValue() {
            return (currentEntry.isExpense ? currentEntry.reason.name : currentEntry.account.name);
        }
    }

    private class ReasonNameColumn extends Column {
        @Override
        public String getValue() {
            if (currentEntry.reason == null) {
                if (currentEntry.reasonName == null) return "";
                return currentEntry.reasonName;
            }

            return (currentEntry.reason.name);
        }
    }

    private class AccountNameColumn extends Column {
        @Override
        public String getValue() {
            return (currentEntry.account.name);
        }
    }

    private class LeftAccountPriceColumn extends Column {
        @Override
        public String getValue() {
            return Long.toString(currentEntry.price);
        }
    }

    private class RightAccountNameColumn extends Column {
        @Override
        public String getValue() {
            return (currentEntry.isExpense ? currentEntry.account.name : currentEntry.reason.name);
        }
    }

    private class RightAccountPriceColumn extends Column {
        @Override
        public String getValue() {
            return Long.toString(currentEntry.price);
        }
    }

    private class SubAccountNameColumn extends Column {
        @Override
        public String getValue() {
            return currentEntry.memo;
        }
    }

    private class TotalPriceColumn extends Column {
        @Override
        public String getValue() {
            if (mode.equals(EXPORT_PROFIT_LOSS_FORMAT_TYPE_CSV) &&
                    (currentEntry.reasonName == null ||
                    currentEntry.viewType == CommonEntryRecyclerAdapter.VIEW_ITEM_HEADER)) {
                return "";
            }
            String valueString = Long.toString(totalPrice);
            if (context != null)
                valueString = "\""+ValueConverter.formatPrice(context, totalPrice)+"\"";

            return valueString;
        }
    }

    private class ExpenseDivisionColumn extends Column {
        @Override
        public String getValue() {
            return (currentEntry.isExpense ? "支出" : "収入");
        }
    }

    private class FixedTextColumn extends Column {

        private String text = null;

        public FixedTextColumn(String text) {
            this.text = text;
        }

        @Override
        public String getValue() {
            return text;
        }
    }

    private class IndexColumn extends Column {
        @Override
        public String getValue() {
            return Long.toString(currentEntry.id);
        }
    }

    //@@ For data backup

    // Project, Account, Reason, Entry, Summary, Recurring
    public String generateDbToJson() {
        Gson gson = new Gson();
        Map<String, Object> maps = new LinkedHashMap<>();

        // Project DB data
        ProjectDataManager projectDataManager = new ProjectDataManager(context);
        List<Project> projectList = projectDataManager.findAll();
        maps.put("project", projectList);

        // Account DB data
        AccountDataManager accountDataManager = new AccountDataManager(context);
        List<Account> accountList = accountDataManager.findAll();
        maps.put("account", accountList);

        // Reason DB data
        ReasonDataManager reasonDataManager = new ReasonDataManager(context);
        List<Reason> reasonList = reasonDataManager.findAll();
        maps.put("reason", reasonList);

        // Entry DB data
        EntryDataManager entryDataManager = new EntryDataManager(context);
        List<Entry> entryList = entryDataManager.findAll();
        maps.put("entry", entryList);

        // Summary DB data
        SummaryDataManager summaryDataManager = new SummaryDataManager(context);
        List<Summary> summaryList = summaryDataManager.findAll();
        maps.put("summary", summaryList);

        // Recurring DB data
        RecurringDataManager recDataManager = new RecurringDataManager(context);
        List<Recurring> recList = recDataManager.findAll();
        maps.put("recurring", recList);

        return gson.toJson(maps);
    }
}
