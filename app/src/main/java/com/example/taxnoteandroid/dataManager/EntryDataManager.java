package com.example.taxnoteandroid.dataManager;

import android.content.Context;

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

        // @@ こんなんでいけそう
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

        public String getTabTitle(Calendar c) {
            int cYear = c.get(Calendar.YEAR);
            int cMonth = c.get(Calendar.MONTH);
            int cDate = c.get(Calendar.DATE);
            switch (_periodType) {
                case PERIOD_TYPE_MONTH:
                    return Integer.toString(cYear)
                            + "/" + Integer.toString(cMonth + 1);
                case PERIOD_TYPE_DAY:
                    return Integer.toString(cYear)
                            + "/" + Integer.toString(cMonth + 1)
                            + "/" + Integer.toString(cDate);
            }
            return Integer.toString(cYear);
        }
    }
}
