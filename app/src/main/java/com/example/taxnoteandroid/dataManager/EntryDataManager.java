package com.example.taxnoteandroid.dataManager;

import android.content.Context;

import com.example.taxnoteandroid.Library.EntryLimitManager;
import com.example.taxnoteandroid.TaxnoteApp;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.Entry;
import com.example.taxnoteandroid.model.Entry_Schema;
import com.example.taxnoteandroid.model.Entry_Selector;
import com.example.taxnoteandroid.model.OrmaDatabase;
import com.example.taxnoteandroid.model.Project;
import com.example.taxnoteandroid.model.Reason;
import com.github.gfx.android.orma.OrderSpec;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

public class EntryDataManager {

    // レポート期間タイプ別の定義
    public static final int PERIOD_TYPE_YEAR = 1;
    public static final int PERIOD_TYPE_MONTH = 2;
    public static final int PERIOD_TYPE_DAY = 3;
//    public static final int TYPE_INCOME = 10;
//    public static final int TYPE_EXPENSE = 11;

    private OrmaDatabase ormaDatabase;
    private Context mContext;

    public EntryDataManager(Context context) {
        ormaDatabase = TaxnoteApp.getOrmaDatabase();
        mContext = context;
    }


    //--------------------------------------------------------------//
    //    -- Create --
    //--------------------------------------------------------------//

    public long save(Entry entry) {
        return ormaDatabase.insertIntoEntry(entry);
    }

    public static boolean isSaveSuccess(long id) {
        return id != -1;
    }


    //--------------------------------------------------------------//
    //    -- Read --
    //--------------------------------------------------------------//

    public Entry findByUuid(String uuid) {
        return ormaDatabase.selectFromEntry().where(Entry_Schema.INSTANCE.uuid.getQualifiedName() + " = ?", uuid).valueOrNull();
    }

    public List<Entry> findAll(Context context, long[] startAndEndDate, Boolean asc) {

        ProjectDataManager projectDataManager   = new ProjectDataManager(context);
        Project project                         = projectDataManager.findCurrentProjectWithContext(context);

        List<Entry> entries;
        String orderSpec = (asc) ? OrderSpec.ASC : OrderSpec.DESC;

        if (startAndEndDate != null) {

            // Get entries filtered within startDate and endDate
            long startDate  = startAndEndDate[0];
            long endDate    = startAndEndDate[1];

            entries = ormaDatabase.selectFromEntry().
                    where(Entry_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                    .projectEq(project)
                    .where(Entry_Schema.INSTANCE.date.getQualifiedName() + " > " + startDate)
                    .where(Entry_Schema.INSTANCE.date.getQualifiedName() + " < " + endDate)
                    .orderBy(Entry_Schema.INSTANCE.date.getQualifiedName() + " " + orderSpec)
                    .orderBy(Entry_Schema.INSTANCE.updated.getQualifiedName() + " " + orderSpec)
                    .toList();
        } else {

            entries = ormaDatabase.selectFromEntry().
                    where(Entry_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                    .projectEq(project)
                    .orderBy(Entry_Schema.INSTANCE.date.getQualifiedName() + " " + orderSpec)
                    .orderBy(Entry_Schema.INSTANCE.updated.getQualifiedName() + " " + orderSpec)
                    .toList();
        }

        return entries;
    }

    //@@ 収入・支出別
    public List<Entry> findAll(long[] startAndEndDate, boolean isExpense, Boolean asc) {
        ProjectDataManager projectDataManager   = new ProjectDataManager(mContext);
        Project project                         = projectDataManager.findCurrentProjectWithContext(mContext);

        List<Entry> entries;
        String orderSpec = (asc) ? OrderSpec.ASC : OrderSpec.DESC;

        int expense = (isExpense) ? 1 : 0;
        Entry_Selector selector = ormaDatabase.selectFromEntry().
                where(Entry_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                .projectEq(project)
                .where(Entry_Schema.INSTANCE.isExpense.getQualifiedName() + " = " + expense);

        String schemeData = Entry_Schema.INSTANCE.date.getQualifiedName();
        if (startAndEndDate != null) {

            // Get entries filtered within startDate and endDate
            long startDate  = startAndEndDate[0];
            long endDate    = startAndEndDate[1];

            selector = selector
                    .where(schemeData + " > " + startDate)
                    .where(schemeData + " < " + endDate);
        }
        entries = selector
                .orderBy(schemeData + " " + orderSpec)
                .orderBy(Entry_Schema.INSTANCE.updated.getQualifiedName() + " " + orderSpec)
                .toList();

        return entries;
    }

    //@@ 未完成、まだデバッグ中
    public List<Entry> findAll(String word) {
        ProjectDataManager projectDataManager   = new ProjectDataManager(mContext);
        Project project                         = projectDataManager.findCurrentProjectWithContext(mContext);
        List<Entry> entries = new ArrayList<>();
        String orderSpec = OrderSpec.DESC;

        // https://github.com/gfx/Android-Orma/search?utf8=%E2%9C%93&q=like
        ormaDatabase.selectFromEntry()
                .projectEq(project)
                .where(Entry_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                .where(Entry_Schema.INSTANCE.reason.name + " LIKE ?", "%" + word + "%")
//                .where(Entry_Schema.INSTANCE.reason.getEscapedName()
//                    + ".`name` LIKE `%" + word + "%`") <- 失敗
                .orderBy(Entry_Schema.INSTANCE.date.getQualifiedName() + " " + orderSpec)
                .toList();


        return entries;
    }

    /**
     * ワードでEntryデータを検索
     *
     * @param word
     * @param startEndDate
     * @return
     */
    public List<Entry> searchBy(String word, String reasonName, long[] startEndDate) {
        List<Entry> entries = new ArrayList<>();
        List<Entry> searchTargets = findAll(mContext, startEndDate, false);
        for(Entry entry : searchTargets) {

            Pattern wordPattern = Pattern.compile(Pattern.quote(word));
            Matcher accountNameMatcher = wordPattern.matcher(entry.account.name);
            Matcher reasonNameMatcher = wordPattern.matcher(entry.reason.name);
            Matcher memoMatcher = wordPattern.matcher(entry.memo);
            Matcher priceMatcher = wordPattern.matcher(String.valueOf(entry.price));

            if (accountNameMatcher.find() || reasonNameMatcher.find()
                    || memoMatcher.find() || priceMatcher.find()) {

                if (reasonName != null) {
                    if (entry.reason.name.equals(reasonName))
                        entries.add(entry);
                } else {
                    entries.add(entry);
                }
            }
        }
        return entries;
    }

    /**
     * ワード、収入・支出別でEntryデータを検索
     *
     * @param word
     * @param startEndDate
     * @param isExpense
     * @return
     */
    public List<Entry> searchBy(String word, String reasonName, long[] startEndDate, boolean isExpense) {
        List<Entry> entries = new ArrayList<>();
        List<Entry> searchTargets = findAll(startEndDate, isExpense, false);
        for(Entry entry : searchTargets) {

            Pattern wordPattern = Pattern.compile(Pattern.quote(word));
            Matcher accountNameMatcher = wordPattern.matcher(entry.account.name);
            Matcher reasonNameMatcher = wordPattern.matcher(entry.reason.name);
            Matcher memoMatcher = wordPattern.matcher(entry.memo);
            Matcher priceMatcher = wordPattern.matcher(String.valueOf(entry.price));

            if (accountNameMatcher.find() || reasonNameMatcher.find()
                    || memoMatcher.find() || priceMatcher.find()) {

                if (reasonName != null) {
                    if (entry.reason.name.equals(reasonName))
                        entries.add(entry);
                } else {
                    entries.add(entry);
                }
            }
        }
        return entries;
    }

    public Entry hasReasonInEntryData(Reason reason) {
        return ormaDatabase.selectFromEntry().and().reasonEq(reason).valueOrNull();
    }

    public Entry hasAccountInEntryData(Account account) {
        return ormaDatabase.selectFromEntry().and().accountEq(account).valueOrNull();
    }

    public long findSumBalance(long endDate) {
        ProjectDataManager projectDataManager   = new ProjectDataManager(mContext);
        Project project                         = projectDataManager.findCurrentProjectWithContext(mContext);

        String schemeDelete = Entry_Schema.INSTANCE.deleted.getQualifiedName();
        String schemeIsExpense = Entry_Schema.INSTANCE.isExpense.getQualifiedName();
        String schemeDate = Entry_Schema.INSTANCE.date.getQualifiedName();

        Entry_Selector selector = ormaDatabase.selectFromEntry()
                .where(schemeDelete + " = 0")
                .projectEq(project)
                .where(schemeDate + " < " + endDate);

        Long sumIncome = selector.clone().where(schemeIsExpense + " = 0").sumByPrice();
        Long sumExpense = selector.clone().where(schemeIsExpense + " = 1").sumByPrice();

        long total = 0;
        if (sumIncome != null) total += sumIncome;
        if (sumExpense != null) total -= sumExpense;

        return total;
    }


    //--------------------------------------------------------------//
    //    -- Update --
    //--------------------------------------------------------------//

    public int updateDate(long id, long date) {
        return ormaDatabase.updateEntry().idEq(id).date(date).execute();
    }

    public int updateAccount(long id, Account account) {
        return ormaDatabase.updateEntry().idEq(id).account(account).execute();
    }

    public int updateReason(long id, Reason reason) {
        return ormaDatabase.updateEntry().idEq(id).reason(reason).execute();
    }

    public int updateMemo(long id, String memo) {
        return ormaDatabase.updateEntry().idEq(id).memo(memo).execute();
    }

    public int updatePrice(long id, long price) {
        return ormaDatabase.updateEntry().idEq(id).price(price).execute();
    }


    //--------------------------------------------------------------//
    //    -- Delete --
    //--------------------------------------------------------------//

    public int delete(long id) {
        return ormaDatabase.deleteFromEntry().idEq(id).execute();
    }


    /**
     * Data Grouping for report.
     */
    public static class ReportGrouping {
        private int _periodType = PERIOD_TYPE_YEAR;

        public ReportGrouping(int periodType) {
            this._periodType = periodType;
        }

        public Calendar getGroupingCalendar(Entry entry) {
            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            calendar.setTimeInMillis(entry.date);
            switch (_periodType) {
                case PERIOD_TYPE_YEAR:
                    calendar.set(calendar.get(Calendar.YEAR), 0, 1, 0, 0, 0);
                    break;
                case PERIOD_TYPE_MONTH:
                    calendar.set(calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH), 1, 0, 0, 0);
                    break;
                case PERIOD_TYPE_DAY:
                    calendar.set(calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DATE), 0, 0, 0);
                    break;
            }
            calendar.set(Calendar.MILLISECOND, 0);

            return calendar;
        }

        public String getTabTitle(Context context, int closingDateIndex, Calendar c) {
            int cYear = c.get(Calendar.YEAR);
            int cMonth = c.get(Calendar.MONTH);
            int cDate = c.get(Calendar.DATE);
            switch (_periodType) {
                case PERIOD_TYPE_MONTH:
                    String monthTitle = Integer.toString(cYear)
                            + "/" + Integer.toString(cMonth + 1);
                    if (closingDateIndex != 28) {
                        long[] startEndDate = EntryLimitManager
                                .getStartAndEndDate(context, _periodType, c);
                        Calendar startCal = Calendar.getInstance();
                        startCal.clear();
                        startCal.setTimeInMillis(startEndDate[0]);
                        Calendar endCal = Calendar.getInstance();
                        endCal.clear();
                        endCal.setTimeInMillis(startEndDate[1]);

                        int startYear = startCal.get(Calendar.YEAR);
                        int startMonth = startCal.get(Calendar.MONTH)+1;
                        int startDate = startCal.get(Calendar.DATE);
                        int endYear = endCal.get(Calendar.YEAR);
                        int endMonth = endCal.get(Calendar.MONTH)+1;
                        int endDate = endCal.get(Calendar.DATE);
                        String endTitle = " ~ " + endMonth + "/" + endDate;
                        if (startYear != endYear)
                            endTitle = " ~ " + endYear + "/" + endMonth + "/" + endDate;

                        monthTitle = startYear + "/" + startMonth + "/" + startDate
                                + endTitle;
                    }

                    return monthTitle;
                case PERIOD_TYPE_DAY:
                    return Integer.toString(cYear)
                            + "/" + Integer.toString(cMonth + 1)
                            + "/" + Integer.toString(cDate);
            }
            return Integer.toString(cYear);
        }

        public List<Calendar> getReportCalendars(int closingDateIndex, List<Entry> entries) {
            List<Calendar> calendars = new ArrayList<>();
            for (Entry entry : entries) {
                Calendar calendar = getGroupingCalendar(entry);
                if (!calendars.contains(calendar)) {
                    calendars.add(calendar);
                }
            }

            //@@ 月の締め日が月末以外に設定されていたら
            // 頭と最後に1ヶ月ずつ増やす
            if (closingDateIndex != 28) {
                int calSize = calendars.size();
                Calendar firstCal = calendars.get(0);
                Calendar newFirstCal = (Calendar)firstCal.clone();
                int newFirstYear = firstCal.get(Calendar.YEAR);
                int newFirstMonth = firstCal.get(Calendar.MONTH)-1;
                if (newFirstMonth < 0) {
                    newFirstMonth = 11;
                    newFirstYear -= 1;
                } else if (_periodType == PERIOD_TYPE_YEAR) {
                    newFirstYear -= 1;
                }
                newFirstCal.set(newFirstYear,
                        newFirstMonth,
                        firstCal.get(Calendar.DATE));

                Calendar lastCal = (calSize == 1) ? firstCal : calendars.get(calSize-1);
                Calendar newLastCal = (Calendar)lastCal.clone();
                int newLastYear = lastCal.get(Calendar.YEAR);
                int newLastMonth = lastCal.get(Calendar.MONTH)+1;
                if (newLastMonth == 12) {
                    newLastMonth = 0;
                    newLastYear += 1;
                } else if (_periodType == PERIOD_TYPE_YEAR) {
                    newLastYear += 1;
                }
                newLastCal.set(newLastYear,
                        newLastMonth,
                        firstCal.get(Calendar.DATE));

                calendars.add(0, newFirstCal);
                calendars.add(newLastCal);
            }

            return calendars;
        }
    }
}
