package com.example.taxnoteandroid.dataManager;

import android.content.Context;
import android.util.Log;

import com.example.taxnoteandroid.Library.EntryLimitManager;
import com.example.taxnoteandroid.Library.taxnote.TNApiModel;
import com.example.taxnoteandroid.Library.taxnote.TNApiUser;
import com.example.taxnoteandroid.TaxnoteApp;
import com.example.taxnoteandroid.model.Account;
import com.example.taxnoteandroid.model.Entry;
import com.example.taxnoteandroid.model.Entry_Schema;
import com.example.taxnoteandroid.model.Entry_Selector;
import com.example.taxnoteandroid.model.Entry_Updater;
import com.example.taxnoteandroid.model.OrmaDatabase;
import com.example.taxnoteandroid.model.Project;
import com.example.taxnoteandroid.model.Reason;
import com.github.gfx.android.orma.OrderSpec;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public Entry findById(long id) {
        return ormaDatabase.selectFromEntry().idEq(id).valueOrNull();
    }

    public List<Entry> findAllNeedSave(boolean isNeedSave) {
        int needSave = (isNeedSave) ? 1 : 0;
        List<Entry> entries = ormaDatabase.selectFromEntry()
                .where(Entry_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                .and()
                .where(Entry_Schema.INSTANCE.needSave.getQualifiedName() + " = " + needSave)
                .toList();
        return entries;
    }

    public List<Entry> findAllNeedSync(boolean isNeedSync) {
        int needSync = (isNeedSync) ? 1 : 0;
        List<Entry> entries = ormaDatabase.selectFromEntry()
                .where(Entry_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                .and()
                .where(Entry_Schema.INSTANCE.needSync.getQualifiedName() + " = " + needSync)
                .toList();
        return entries;
    }

    public List<Entry> findAllDeleted(boolean isDeleted) {
        int deleted = (isDeleted) ? 1 : 0;
        List<Entry> entries = ormaDatabase.selectFromEntry()
                .where(Entry_Schema.INSTANCE.deleted.getQualifiedName() + " = " + deleted)
                .toList();
        return entries;
    }

    public List<Entry> findAll() {
        return ormaDatabase.selectFromEntry().toList();
    }

    public List<Entry> findAll(long[] startAndEndDate, Boolean asc) {

        ProjectDataManager projectDataManager   = new ProjectDataManager(mContext);
        Project project                         = projectDataManager.findCurrentProjectWithContext();

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

    public int count(long[] startEndDate) {
        ProjectDataManager projectDataManager   = new ProjectDataManager(mContext);
        Project project                         = projectDataManager.findCurrentProjectWithContext();
        long startDate  = startEndDate[0];
        long endDate    = startEndDate[1];
        int countData = ormaDatabase.selectFromEntry().
                where(Entry_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                .projectEq(project)
                .where(Entry_Schema.INSTANCE.date.getQualifiedName() + " > " + startDate)
                .where(Entry_Schema.INSTANCE.date.getQualifiedName() + " < " + endDate)
                .count();
        return countData;
    }

    // 収入・支出別
    public List<Entry> findAll(long[] startAndEndDate, boolean isExpense, Boolean asc) {
        ProjectDataManager projectDataManager   = new ProjectDataManager(mContext);
        Project project                         = projectDataManager.findCurrentProjectWithContext();

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
        Project project                         = projectDataManager.findCurrentProjectWithContext();
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
        List<Entry> searchTargets = findAll(startEndDate, false);
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
        Project project                         = projectDataManager.findCurrentProjectWithContext();

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
        return ormaDatabase.updateEntry().idEq(id)
                .date(date)
                .needSync(true)
                .execute();
    }

    public int updateAccount(long id, Account account) {
        return ormaDatabase.updateEntry().idEq(id)
                .account(account)
                .needSync(true)
                .execute();
    }

    public int updateReason(long id, Reason reason) {
        return ormaDatabase.updateEntry().idEq(id)
                .reason(reason)
                .needSync(true)
                .execute();
    }

    public int updateMemo(long id, String memo) {
        return ormaDatabase.updateEntry().idEq(id)
                .memo(memo)
                .needSync(true)
                .execute();
    }

    public int updatePrice(long id, long price) {
        return ormaDatabase.updateEntry().idEq(id)
                .price(price)
                .needSync(true)
                .execute();
    }

    public int updateNeedSave(long id, boolean needSave) {
        return ormaDatabase.updateEntry().idEq(id).needSave(needSave).execute();
    }

    public int updateNeedSync(long id, boolean needSync) {
        return ormaDatabase.updateEntry().idEq(id).needSync(needSync).execute();
    }

    public void update(Entry entry) {
        Entry_Updater updater = ormaDatabase.updateEntry();
        updater.idEq(entry.id)
                .date(entry.date)
                .updated(entry.updated)
                .price(entry.price)
                .deleted(entry.deleted)
                .isExpense(entry.isExpense)
                .needSave(entry.needSave)
                .needSync(entry.needSync)
                .uuid(entry.uuid)
                .memo(entry.memo)
                .project(entry.project)
                .reason(entry.reason)
                .account(entry.account)
                .execute();
    }

    public void updateSetDeleted(String uuid, TNApiModel apiModel) {
        boolean isLoggingIn = TNApiUser.isLoggingIn(mContext);
        Entry entry = findByUuid(uuid);
        if (entry == null) return;

        if (isLoggingIn) {
            ormaDatabase.updateEntry().idEq(entry.id)
                    .deleted(true)
                    .execute();

            // send api
            apiModel.deleteEntry(uuid, null);
        } else {
            delete(entry.id);
        }
    }


    //--------------------------------------------------------------//
    //    -- Delete --
    //--------------------------------------------------------------//

    public int delete(long id) {
        return ormaDatabase.deleteFromEntry().idEq(id).execute();
    }

    public int delete(String uuid) {
        Entry entry = findByUuid(uuid);
        if (entry == null) {
            return 0;
        }
        return delete(entry.id);
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
            int startMonthIndex = SharedPreferencesManager.getStartMonthOfYearIndex(context);
            int cYear = c.get(Calendar.YEAR);
            int cMonth = c.get(Calendar.MONTH);
            int cDate = c.get(Calendar.DATE);

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
            int endDate = endCal.get(Calendar.DATE) - 1;
            if (closingDateIndex != 28 && endDate == 0) {
                endMonth -= 1;
                endCal.set(endYear, endMonth, 1);
                endDate = endCal.getActualMaximum(Calendar.DAY_OF_MONTH);
            }

            switch (_periodType) {
                case PERIOD_TYPE_MONTH:
                    String monthTitle = Integer.toString(cYear)
                            + "/" + Integer.toString(cMonth + 1);
                    if (closingDateIndex != 28) {
                        String endTitle = " ~ " + endMonth + "/" + endDate;
                        if (startYear != endYear)
                            endTitle = " ~ " + endYear + "/" + endMonth + "/" + endDate;

                        monthTitle = startYear + "/" + startMonth + "/" + startDate
                                + endTitle;
                    }

                    return monthTitle;
                case PERIOD_TYPE_DAY:
                    String dayTitle = cYear + "/" + (cMonth + 1) + "/" +cDate;
                    return dayTitle;
            }

            // for year title
            String yearTitle = Integer.toString(cYear);
            if (startMonthIndex > 0) {
                endMonth -= 1;
                yearTitle = startYear + "/" + startMonth
                        + " ~ " + endYear + "/" + endMonth;
            }

            if (closingDateIndex != 28) {
                yearTitle = startYear + "/" + startMonth + "/" + startDate
                        + " ~ " + endYear + "/" + endMonth + "/" + endDate;
            }

            return yearTitle;
        }

        public List<Calendar> getReportCalendars(int closingDateIndex, List<Entry> entries) {
            List<Calendar> calendars = new ArrayList<>();
            for (Entry entry : entries) {
                Calendar calendar = getGroupingCalendar(entry);
                if (!calendars.contains(calendar)) {
                    calendars.add(calendar);
                }
            }

            int calSize = calendars.size();
            if (calSize == 0) return calendars;

            Calendar firstCal = calendars.get(0);
            Calendar newFirstCal = (Calendar)firstCal.clone();
            Calendar lastCal = (calSize == 1) ? firstCal : calendars.get(calSize-1);
            Calendar newLastCal = (Calendar)lastCal.clone();

            // 月の締め日が月末以外に設定されていたら
            // 頭と最後に1ヶ月ずつ増やす
            if (_periodType == PERIOD_TYPE_MONTH && closingDateIndex != 28) {
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

            // 年別のときは前後１年ずつ増やす
            if (_periodType == PERIOD_TYPE_YEAR) {
                int newFirstYear = firstCal.get(Calendar.YEAR);
                newFirstCal.set(newFirstYear-1, firstCal.get(Calendar.MONTH), firstCal.get(Calendar.DATE));

                int newLastYear = lastCal.get(Calendar.YEAR);
                newLastCal.set(newLastYear+1, lastCal.get(Calendar.MONTH), lastCal.get(Calendar.DATE));

                calendars.add(0, newFirstCal);
                calendars.add(newLastCal);
            }

            return calendars;
        }
    }
}
