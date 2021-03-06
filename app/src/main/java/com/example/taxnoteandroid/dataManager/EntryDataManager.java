package com.example.taxnoteandroid.dataManager;

import android.content.Context;

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
    public static final int PERIOD_TYPE_ALL = 1;
    public static final int PERIOD_TYPE_YEAR = 2;
    public static final int PERIOD_TYPE_MONTH = 3;
    public static final int PERIOD_TYPE_DAY = 4;

    private OrmaDatabase ormaDatabase;
    private Context mContext;
    private ProjectDataManager mProjectManager;
    private Project mCurrentProject;

    private boolean mIsCompAllProject;

    public EntryDataManager(Context context) {
        ormaDatabase = TaxnoteApp.getOrmaDatabase();
        mContext = context;
        mProjectManager = new ProjectDataManager(mContext);
        mCurrentProject = mProjectManager.findCurrent();
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

    private List<Entry> findAll(long[] startAndEndDate, Boolean asc) {

        List<Entry> entries;
        String orderSpec = (asc) ? OrderSpec.ASC : OrderSpec.DESC;

        Entry_Selector selector = (mIsCompAllProject)
                ? ormaDatabase.selectFromEntry()
                : ormaDatabase.selectFromEntry().projectEq(mCurrentProject);

        if (startAndEndDate != null) {

            // Get entries filtered within startDate and endDate
            long startDate  = startAndEndDate[0];
            long endDate    = startAndEndDate[1];

            entries = selector .
                    where(Entry_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                    .where(Entry_Schema.INSTANCE.date.getQualifiedName() + " > " + startDate)
                    .where(Entry_Schema.INSTANCE.date.getQualifiedName() + " < " + endDate)
                    .orderBy(Entry_Schema.INSTANCE.date.getQualifiedName() + " " + orderSpec)
                    .orderBy(Entry_Schema.INSTANCE.updated.getQualifiedName() + " " + orderSpec)
                    .toList();
        } else {

            entries = selector .
                    where(Entry_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                    .orderBy(Entry_Schema.INSTANCE.date.getQualifiedName() + " " + orderSpec)
                    .orderBy(Entry_Schema.INSTANCE.updated.getQualifiedName() + " " + orderSpec)
                    .toList();
        }

        return entries;
    }

    // 最新（未来）の仕訳帳を取得
    public Entry getNewestDate() {
        Entry result = ormaDatabase.selectFromEntry().projectEq(mCurrentProject)
                .where(Entry_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                .orderBy(Entry_Schema.INSTANCE.date.getQualifiedName() + " " + OrderSpec.DESC)
                .valueOrNull();
        return result;
    }

    public int count(long[] startEndDate) {
        long startDate  = startEndDate[0];
        long endDate    = startEndDate[1];
        int countData = ormaDatabase.selectFromEntry().
                where(Entry_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                .projectEq(mCurrentProject)
                .where(Entry_Schema.INSTANCE.date.getQualifiedName() + " > " + startDate)
                .where(Entry_Schema.INSTANCE.date.getQualifiedName() + " < " + endDate)
                .count();
        return countData;
    }

    public int countAll() {
        int countData = ormaDatabase.selectFromEntry().
                where(Entry_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                .projectEq(mCurrentProject)
                .count();
        return countData;
    }

    public int countNeedSave(boolean isNeedSave) {
        int needSave = (isNeedSave) ? 1 : 0;
        return ormaDatabase.selectFromEntry()
                .where(Entry_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                .and()
                .where(Entry_Schema.INSTANCE.needSave.getQualifiedName() + " = " + needSave)
                .count();
    }



    /**
     * 繰越残高の取得
     *
     * @param endDate いつまでか（year, month...）
     * @return
     */
    public long getCarriedBalance(long endDate) {

        List<Entry> entries;
        String orderSpec = OrderSpec.ASC;

        Entry_Selector selector = (mIsCompAllProject)
                ? ormaDatabase.selectFromEntry()
                : ormaDatabase.selectFromEntry().projectEq(mCurrentProject);

        Entry_Selector entrySelector = selector
                .where(Entry_Schema.INSTANCE.deleted.getQualifiedName() + " = 0");

        // 損益表の設定値から反映する

        if (endDate != 0) {
            entrySelector = entrySelector
                    .where(Entry_Schema.INSTANCE.date.getQualifiedName() + " < " + endDate);
        }
        entries = entrySelector
                .orderBy(Entry_Schema.INSTANCE.date.getQualifiedName() + " " + orderSpec)
                .toList();

        long result = 0;
        for (Entry _entry : entries) {
            if (_entry.isExpense) {
                result -= _entry.price;
            } else {
                result += _entry.price;
            }
        }

        return result;
    }

    // 収入・支出別
    private List<Entry> findAll(long[] startAndEndDate, boolean isExpense, Boolean asc) {

        List<Entry> entries;
        String orderSpec = (asc) ? OrderSpec.ASC : OrderSpec.DESC;

        int expense = (isExpense) ? 1 : 0;
        Entry_Selector selector = ormaDatabase.selectFromEntry().
                where(Entry_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                .projectEq(mCurrentProject)
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

    // 備考で探す
    private List<Entry> findAll(long[] startAndEndDate, String memo, boolean isExpense, Boolean asc) {

        List<Entry> entries;
        String orderSpec = (asc) ? OrderSpec.ASC : OrderSpec.DESC;

        int expense = (isExpense) ? 1 : 0;
        Entry_Selector selector = ormaDatabase.selectFromEntry().
                where(Entry_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                .projectEq(mCurrentProject)
                .where(Entry_Schema.INSTANCE.isExpense.getQualifiedName() + " = " + expense)
                .where(Entry_Schema.INSTANCE.memo.getQualifiedName()
                        + "='"+memo+"'");

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
    private List<Entry> findAll(String word) {
        List<Entry> entries = new ArrayList<>();
        String orderSpec = OrderSpec.DESC;

        // https://github.com/gfx/Android-Orma/search?utf8=%E2%9C%93&q=like
        ormaDatabase.selectFromEntry()
                .projectEq(mCurrentProject)
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
    public List<Entry> searchBy(String word, String reasonName, long[] startEndDate, Boolean asc) {
        List<Entry> entries = new ArrayList<>();
        List<Entry> searchTargets = findAll(startEndDate, asc);
        word = word == null ? "" : word;
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
    public List<Entry> searchBy(String word, String reasonName, long[] startEndDate, boolean isExpense, Boolean asc) {
        List<Entry> entries = new ArrayList<>();
        List<Entry> searchTargets = findAll(startEndDate, isExpense, asc);
        word = word == null ? "" : word;
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

    public List<Entry> searchBy(String word, String reasonName, long[] startEndDate, String memo, boolean isExpense, Boolean asc) {
        List<Entry> entries = new ArrayList<>();
        List<Entry> searchTargets = findAll(startEndDate, memo, isExpense, asc);
        word = word == null ? "" : word;
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
        return ormaDatabase.selectFromEntry()
                .projectEq(mCurrentProject)
                .and().reasonEq(reason).valueOrNull();
    }

    public Entry hasAccountInEntryData(Account account) {
        return ormaDatabase.selectFromEntry()
                .projectEq(mCurrentProject)
                .and().accountEq(account).valueOrNull();
    }

    public int countByAccount(Account account) {
        return ormaDatabase.selectFromEntry()
                .projectEq(mCurrentProject)
                .where(Entry_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                .and()
                .accountEq(account)
                .count();
    }

    public int countByReason(Reason reason) {
        return ormaDatabase.selectFromEntry()
                .projectEq(mCurrentProject)
                .where(Entry_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                .and()
                .reasonEq(reason)
                .count();
    }

    public long findSumBalance(long endDate) {

        String schemeDelete = Entry_Schema.INSTANCE.deleted.getQualifiedName();
        String schemeIsExpense = Entry_Schema.INSTANCE.isExpense.getQualifiedName();
        String schemeDate = Entry_Schema.INSTANCE.date.getQualifiedName();

        Entry_Selector selector = ormaDatabase.selectFromEntry()
                .where(schemeDelete + " = 0")
                .projectEq(mCurrentProject)
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

    public void updateCombine(Reason fromReason, Reason toReason) {
        ormaDatabase.updateEntry().project(mCurrentProject)
                .reasonEq(fromReason)
                .reason(toReason)
                .needSync(true)
                .execute();
    }

    public void updateCombine(Account fromAccount, Account toAccount) {
        ormaDatabase.updateEntry().project(mCurrentProject)
                .accountEq(fromAccount)
                .account(toAccount)
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

    public void updateAllNeedSave() {
        ormaDatabase.updateEntry()
                .needSave(true).needSync(false)
                .execute();
    }

    public void updateSetDeleted(String uuid) {
        updateSetDeleted(uuid, null);
    }

    public void updateSetDeleted(String uuid, TNApiModel apiModel) {
        if (uuid == null) return;

        boolean isLoggingIn = TNApiUser.isLoggingIn(mContext);
        Entry entry = findByUuid(uuid);
        if (entry == null) return;

        if (isLoggingIn) {
            ormaDatabase.updateEntry().idEq(entry.id)
                    .deleted(true)
                    .execute();

            // send api
            if (apiModel != null) {
                apiModel.deleteEntry(uuid, null);
            }
        } else {
            delete(entry.id);
        }
    }


    //--------------------------------------------------------------//
    //    -- Delete --
    //--------------------------------------------------------------//

    public void deleteByAccount(Account account, TNApiModel apiModel) {
        boolean isLoggingIn = TNApiUser.isLoggingIn(mContext);

        if (isLoggingIn) {
            List<Entry> targetList =  ormaDatabase.selectFromEntry()
                    .projectEq(mCurrentProject)
                    .where(Entry_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                    .and().accountEq(account).toList();
            for (Entry _entry : targetList) {
                ormaDatabase.updateEntry().idEq(_entry.id)
                        .deleted(true).execute();
            }

            apiModel.saveAllNeedSaveSyncDeletedData(null);

        } else {
            ormaDatabase.deleteFromEntry()
                    .projectEq(mCurrentProject)
                    .accountEq(account.id)
                    .execute();
        }
    }

    public void deleteByReason(Reason reason, TNApiModel apiModel) {
        boolean isLoggingIn = TNApiUser.isLoggingIn(mContext);

        if (isLoggingIn) {
            List<Entry> targetList =  ormaDatabase.selectFromEntry()
                    .projectEq(mCurrentProject)
                    .where(Entry_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
                    .and().reasonEq(reason).toList();
            for (Entry _entry : targetList) {
                ormaDatabase.updateEntry().idEq(_entry.id)
                        .deleted(true).execute();
            }

            apiModel.saveAllNeedSaveSyncDeletedData(null);

        } else {
            ormaDatabase.deleteFromEntry()
                    .projectEq(mCurrentProject)
                    .reasonEq(reason.id)
                    .execute();
        }
    }


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

    // 全帳簿の合計かどうか設定する
    public void setCompAllProject(boolean val) {
        this.mIsCompAllProject = val;
    }


    /**
     * Data Grouping for report.
     */
    public static class ReportGrouping {
        private int _periodType = PERIOD_TYPE_YEAR;

        public ReportGrouping(int periodType) {
            this._periodType = periodType;
        }

        public Calendar getGroupingCalendar(Entry entry, int closingDateIndex) {
            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            calendar.setTimeInMillis(entry.date);
            switch (_periodType) {
                case PERIOD_TYPE_YEAR:
                    calendar.set(calendar.get(Calendar.YEAR), 0, 1, 0, 0, 0);
                    break;
                case PERIOD_TYPE_MONTH:
                    int lastDayOfMonthIndex = 26;
                    int date = calendar.get(Calendar.DATE);
                    if (closingDateIndex >= lastDayOfMonthIndex) {
                        calendar.set(calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH), 1, 0, 0, 0);
                    } else {
                        calendar.set(calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH), closingDateIndex + 3, 0, 0, 0);
                        if (date > closingDateIndex + 2) {
                            calendar.add(Calendar.MONTH, 1);
                        }
                    }
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
            int lastDayOfMonthIndex = 26;

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
            if (closingDateIndex != lastDayOfMonthIndex && endDate == 0) {
                endMonth -= 1;
                endCal.set(endYear, endMonth, 1);
                endDate = endCal.getActualMaximum(Calendar.DAY_OF_MONTH);
            }

            switch (_periodType) {
                case PERIOD_TYPE_MONTH:
                    String monthTitle = Integer.toString(cYear)
                            + "/" + Integer.toString(cMonth + 1);
                    if (closingDateIndex != lastDayOfMonthIndex) {
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
                if (closingDateIndex == lastDayOfMonthIndex)
                    endMonth -= 1;
                yearTitle = startYear + "/" + startMonth
                        + " ~ " + endYear + "/" + endMonth;
            }

            if (closingDateIndex != lastDayOfMonthIndex) {
                yearTitle = startYear + "/" + startMonth + "/" + startDate
                        + " ~ " + endYear + "/" + endMonth + "/" + endDate;
            }

            return yearTitle;
        }

        public List<Calendar> getReportCalendars(int closingDateIndex, List<Entry> entries) {
            List<Calendar> calendars = new ArrayList<>();

            // 棒グラフのすべての期間では、
            // 選択していた年の4年前の年から8年先の8年間
            if (_periodType == PERIOD_TYPE_ALL) {
                Calendar selectedCal = TaxnoteApp.getInstance().SELECTED_TARGET_CAL;
                if (selectedCal == null) selectedCal = Calendar.getInstance();
                int firstYear = selectedCal.get(Calendar.YEAR) - 4;
                for (int i=1; i<=8; i++) {
                    Calendar _cal = Calendar.getInstance();
                    _cal.clear();
                    _cal.set(firstYear+i, 0, 1, 0, 0, 0);
                    _cal.set(Calendar.MILLISECOND, 0);
                    calendars.add(_cal);
                }
                return calendars;
            }


            for (Entry entry : entries) {
                Calendar calendar = getGroupingCalendar(entry, closingDateIndex);
                if (!calendars.contains(calendar)) {
                    calendars.add(calendar);
                }
            }

            int calSize = calendars.size();
            if (calSize == 0) return calendars;

            int type = -1;
            if (_periodType == PERIOD_TYPE_MONTH && closingDateIndex != 26) {
                // 月の締め日が月末以外に設定されていたら
                // 頭と最後に1ヶ月ずつ増やす
                type = Calendar.MONTH;
            } else if (_periodType == PERIOD_TYPE_YEAR) {
                // 年別のときは前後１年ずつ増やす
                type = Calendar.YEAR;
            }
            if (type >= 0) {
                Calendar newFirstCal = Calendar.getInstance();
                newFirstCal.setTime(calendars.get(0).getTime());
                newFirstCal.add(type, -1);
                calendars.add(0, newFirstCal);

                Calendar newLastCal = Calendar.getInstance();
                newLastCal.setTime(calendars.get(calendars.size() - 1).getTime());
                newLastCal.add(type, 1);
                calendars.add(newLastCal);
            }

            return calendars;
        }
    }
}
