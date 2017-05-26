package com.example.taxnoteandroid.Library;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ShareCompat;

import com.example.taxnoteandroid.CommonEntryRecyclerAdapter;
import com.example.taxnoteandroid.Library.zeny.ZNUtils;
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
    private boolean isSubjectEnable = false;

    public DataExportManager(Activity activity) {
        this.mActivity = activity;
        this.context = activity.getApplicationContext();
        this.isSubjectEnable = SharedPreferencesManager.getExportSujectEnable(context);
    }


    public DataExportManager(Activity activity, String mode, String character_code) {
        this.mActivity = activity;
        this.context = activity.getApplicationContext();
        this.isSubjectEnable = SharedPreferencesManager.getExportSujectEnable(context);

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

            // Zenyは単式簿記
            if (ZNUtils.isZeny()) {
                String date = this.context.getResources().getString(R.string.entry_tab_fragment_date);
                String leftAccountNameColumn = this.context.getResources().getString(R.string.data_export_debit);
                String expenseNameColumn = this.context.getResources().getString(R.string.Expense);
                String incomeNameColumn = this.context.getResources().getString(R.string.Income);
                String MemoNameColumn = this.context.getResources().getString(R.string.data_export_details);

                intColumns(5); // CSV column size.
                setColumnTitles(date, leftAccountNameColumn,expenseNameColumn,incomeNameColumn, MemoNameColumn);
                setColumn(0, new DateColumn());
                setColumn(1, new LeftAccountNameColumn());
                setColumn(2, new LeftAccountPriceColumn());
                setColumn(3, new RightAccountPriceColumn());
                setColumn(4, new MemoNameColumn());
                setSeparator(",");

                // Taxnoteは複式簿記
            } else {

                // 補助科目が有効かどうか
                boolean isExportSubject = SharedPreferencesManager.getExportSujectEnable(context);

                String date = this.context.getResources().getString(R.string.entry_tab_fragment_date);
                String LeftAccountNameColumn = this.context.getResources().getString(R.string.data_export_account);
                String LeftAccountPriceColumn = this.context.getResources().getString(R.string.data_export_debit);
                String RightAccountNameColumn = this.context.getResources().getString(R.string.data_export_credit_account);
                String RightAccountPriceColumn = this.context.getResources().getString(R.string.data_export_credit);
                String MemoNameColumn = this.context.getResources().getString(R.string.data_export_details);

                // 補助科目・税区分のカラム
                String debitSubAccountCol = context.getString(R.string.data_export_debit_sub_account);
                String debitTaxNameCol = context.getString(R.string.data_export_debit_tax_name);
                String creditSubAccountCol = context.getString(R.string.data_export_credit_sub_account);
                String creditTaxNameCol = context.getString(R.string.data_export_credit_tax_name);

                if (isExportSubject) {
                    intColumns(10); // CSV column size.
                    setColumnTitles(date, LeftAccountNameColumn, debitSubAccountCol, debitTaxNameCol,
                            LeftAccountPriceColumn, RightAccountNameColumn, creditSubAccountCol,
                            creditTaxNameCol, RightAccountPriceColumn, MemoNameColumn);
                    setColumn(0, new DateColumn());
                    setColumn(1, new LeftAccountNameColumn());
                    setColumn(2, new DebitSubAccountColumn());
                    setColumn(3, new DebitTaxNameColumn());
                    setColumn(4, new LeftAccountPriceColumn());
                    setColumn(5, new RightAccountNameColumn());
                    setColumn(6, new CreditSubAccountColumn());
                    setColumn(7, new CreditTaxNameColumn());
                    setColumn(8, new RightAccountPriceColumn());
                    setColumn(9, new MemoNameColumn());

                } else {
                    intColumns(6); // CSV column size.
                    setColumnTitles(date, LeftAccountNameColumn, LeftAccountPriceColumn, RightAccountNameColumn, RightAccountPriceColumn, MemoNameColumn);
                    setColumn(0, new DateColumn());
                    setColumn(1, new LeftAccountNameColumn());
                    setColumn(2, new LeftAccountPriceColumn());
                    setColumn(3, new RightAccountNameColumn());
                    setColumn(4, new RightAccountPriceColumn());
                    setColumn(5, new MemoNameColumn());
                }
                setSeparator(",");
            }

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
            setColumn(16, new MemoNameColumn());
            setColumn(19, new FixedTextColumn("0"));
            setColumn(24, new FixedTextColumn("NO"));

            if (isSubjectEnable) {
                setColumn(5, new DebitSubAccountColumn());
                setColumn(7, new DebitTaxNameColumn("対象外"));
                setColumn(11, new CreditSubAccountColumn());
                setColumn(13, new CreditTaxNameColumn("対象外"));
            }

            setSeparator("\t");

        } else if (mode.compareTo(EXPORT_FORMAT_TYPE_FREEE) == 0) { // Freee

            intColumns(14); // CSV column size.
            setColumnTitles("収支区分", "管理番号", "発生日", "支払期日", "取引先", "勘定科目", "税区分", "金額", "備考", "品目", "メモタグ（複数指定可、カンマ区切り）", "支払日", "支払口座", "支払金額");
            setColumn(0, new ExpenseDivisionColumn());
            setColumn(2, new DateColumn());
            setColumn(5, new ReasonNameColumn());
            setColumn(6, new FixedTextColumn("対象外"));
            setColumn(7, new LeftAccountPriceColumn());
            setColumn(8, new MemoNameColumn());
            setColumn(11, new DateColumn());
            setColumn(12, new AccountNameColumn());
            setColumn(13, new RightAccountPriceColumn());

            if (isSubjectEnable) {
                setColumn(6, new DebitTaxNameColumn("対象外"));
                setColumn(9, new DebitSubAccountColumn());
            }

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
            setColumn(10, new MemoNameColumn());
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
        final ProgressDialog dialog = new ProgressDialog(mActivity);
        dialog.setMessage(context.getString(R.string.data_export));
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.show();

        AsyncTask<Object, Object, File> task = new AsyncTask<Object, Object, File>() {

            @Override
            protected File doInBackground(Object... objects) {
                return generateCsvFile();
            }

            @Override
            protected void onPostExecute(File file) {
                super.onPostExecute(file);
                dialog.cancel();

                if (file != null) {
                    shareFileContent(file);
                } else {
                    DialogManager.showOKOnlyAlert(mActivity, context.getString(R.string.Error), context.getString(R.string.data_export_cant_make_csv));
                }
            }
        };

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private File generateCsvFile() {

        OutputStream streamOut = null;
        PrintWriter writer = null;

        try {
            File file = getOutputFile();
            if (file.getParentFile().exists() == false)
                file.getParentFile().mkdirs(); // If parent folder doesn't exist, create it.
            streamOut = new FileOutputStream(file);
            writer = new PrintWriter(new OutputStreamWriter(streamOut, characterCode));

            List<Entry> entries = getSelectedRangeEntries(); // Get a list of Entry.

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

    private File getOutputFile() {

        String appName = context.getString(R.string.app_name);
        String fileName = appName;

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

        return new File(Environment.getExternalStorageDirectory(), appName
                + "/" + System.currentTimeMillis() + "/" + fileName);

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

    private void shareFileContent(File file) {

        String appName = context.getString(R.string.app_name);

        String subjectString = context.getString(R.string.data_export_mail_title);
        if (mode.equals(EXPORT_PROFIT_LOSS_FORMAT_TYPE_CSV)) {
            subjectString = appName + " " + context.getString(R.string.profit_loss_export)
                + " (" +getReportStartEndDateString()+ ")";
        }

        // ShareCompat
        Uri streamUri = Uri.fromFile(file);

        ShareCompat.IntentBuilder builder = ShareCompat.IntentBuilder.from(mActivity);
        if (mode.compareTo(EXPORT_FORMAT_TYPE_YAYOI) == 0) {
            builder.setType("text/plain");
        } else {
            builder.setType("text/csv");
        }
        builder.setChooserTitle(context.getString(R.string.data_export))
            .setStream(streamUri);

        // for mail
        builder.setSubject(subjectString)
                .setText(getBodyMessage());

        builder.startChooser();

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

    private List<Entry> getSelectedRangeEntries() {

        List<Entry> entries;
        long[] startEnd;
        EntryDataManager entryDataManager = new EntryDataManager(context);

        String exportRangeType = SharedPreferencesManager.getExportRangeType(context);

        switch (exportRangeType) {

            case EXPORT_RANGE_TYPE_ALL:
                entries = entryDataManager.findAll(null, true);
                break;

            case EXPORT_RANGE_TYPE_THIS_MONTH:
                startEnd = getThisMonthStartAndEndDate();
                entries = entryDataManager.findAll(startEnd, true);
                break;

            case EXPORT_RANGE_TYPE_LAST_MONTH:
                startEnd = getLastMonthStartAndEndDate();
                entries = entryDataManager.findAll(startEnd, true);
                break;

            case EXPORT_RANGE_TYPE_CUSTOM:
                startEnd = getCustomStartAndEndDate(context);
                entries = entryDataManager.findAll(startEnd, true);
                break;

            case EXPORT_PROFIT_LOSS_FORMAT_TYPE_CSV: //@@ 多分使わない
                return new ArrayList<>();

            default:
                entries = entryDataManager.findAll(null, true);
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

            // Zenyは単式簿記だからカテゴリ名固定
            if (ZNUtils.isZeny()) {
                return currentEntry.reason.name;

            } else {
                String nameVal = (currentEntry.isExpense ? currentEntry.reason.name : currentEntry.account.name);
                return ValueConverter.parseCategoryName(context, nameVal);
            }
        }
    }

    private class ReasonNameColumn extends Column {
        @Override
        public String getValue() {
            String name = "";
            if (currentEntry.reason == null) {
                if (currentEntry.reasonName == null) return name;
                name = currentEntry.reasonName;
            } else {
                name = currentEntry.reason.name;
            }

            return ValueConverter.parseCategoryName(context, name);
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

            if (ZNUtils.isZeny()) {
                return Long.toString(currentEntry.isExpense ? currentEntry.price : 0);
            } else {
                return Long.toString(currentEntry.price);
            }
        }
    }

    private class RightAccountNameColumn extends Column {
        @Override
        public String getValue() {
            String nameVal = (currentEntry.isExpense ? currentEntry.account.name : currentEntry.reason.name);
            return ValueConverter.parseCategoryName(context, nameVal);
        }
    }

    private class RightAccountPriceColumn extends Column {
        @Override
        public String getValue() {

            if (ZNUtils.isZeny()) {
                return Long.toString(currentEntry.isExpense ? 0 : currentEntry.price);
            } else {
                return Long.toString(currentEntry.price);
            }
        }
    }

    private class MemoNameColumn extends Column {
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

    private class DebitSubAccountColumn extends Column {
        private String defaultName = "";

        public DebitSubAccountColumn() {
        }
        public DebitSubAccountColumn(String _defaultName) {
            this.defaultName = _defaultName;
        }

        @Override
        public String getValue() {
            String nameVal = (currentEntry.isExpense ? currentEntry.reason.name : currentEntry.account.name);
            if (mode.compareTo(EXPORT_FORMAT_TYPE_FREEE) == 0)
                nameVal = currentEntry.reason.name;
            return ValueConverter.parseSubCategoryName(context, nameVal, defaultName);
        }
    }

    private class DebitTaxNameColumn extends Column {
        private String defaultName = "";

        public DebitTaxNameColumn() {
        }
        public DebitTaxNameColumn(String _defaultName) {
            this.defaultName = _defaultName;
        }

        @Override
        public String getValue() {
            String nameVal = (currentEntry.isExpense ? currentEntry.reason.name : currentEntry.account.name);
            if (mode.compareTo(EXPORT_FORMAT_TYPE_FREEE) == 0)
                nameVal = currentEntry.reason.name;
            return ValueConverter.parseTaxPartName(context, nameVal, defaultName);
        }
    }

    private class CreditSubAccountColumn extends Column {
        private String defaultName = "";

        public CreditSubAccountColumn() {
        }
        public CreditSubAccountColumn(String _defaultName) {
            this.defaultName = _defaultName;
        }

        @Override
        public String getValue() {
            String nameVal = (currentEntry.isExpense ? currentEntry.account.name : currentEntry.reason.name);
            return ValueConverter.parseSubCategoryName(context, nameVal, defaultName);
        }
    }

    private class CreditTaxNameColumn extends Column {
        private String defaultName = "";

        public CreditTaxNameColumn() {
        }
        public CreditTaxNameColumn(String _defaultName) {
            this.defaultName = _defaultName;
        }

        @Override
        public String getValue() {
            String nameVal = (currentEntry.isExpense ? currentEntry.account.name : currentEntry.reason.name);
            return ValueConverter.parseTaxPartName(context, nameVal, defaultName);
        }
    }

    // For data backup

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
