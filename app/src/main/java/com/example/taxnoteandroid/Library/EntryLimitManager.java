package com.example.taxnoteandroid.Library;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by umemotonon on 2016/12/24.
 */

public class EntryLimitManager {

    private static final boolean taxnotePlusIsActive = true;
    private static final long limitNumberOfEntryPerMonth = 15;


    public static boolean limitNewEntryForFreeUsersWithDate(long date) {
//        if (taxnotePlusIsActive) {
//            return false;
//        }
//
//
        Calendar cal = Calendar.getInstance();
        Date javaDate = new Date(date);
        cal.setTime(javaDate);
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 0, 0, 0, 0);

////        cal.get(Calendar.MONTH);
////
//
////
//        cal.clear(Calendar.HOUR_OF_DAY); // ! clear would not reset the hour of day !
//        cal.clear(Calendar.MINUTE);
//        cal.clear(Calendar.SECOND);
//        cal.clear(Calendar.MILLISECOND);
        System.out.println("... in milliseconds:      " + cal.toString());
//
//        // get start of the month
//        cal.set(Calendar.DAY_OF_MONTH, 1);
//        System.out.println("Start of the month:       " + cal.getTime());
//        System.out.println("... in milliseconds:      " + cal.getTimeInMillis());
//
//        //QQここ、dateの絞り方
////        List entries = ormaDatabase.selectFromEntry().
////                where(Entry_Schema.INSTANCE.deleted.getQualifiedName() + " = 0")
////                .and()
////                .projectEq(project)
////                .orderBy(Entry_Schema.INSTANCE.date.getQualifiedName() + " " + OrderSpec.DESC)
////                .toList();
//
//
//        return true;
        return true;
    }


//    //limit based on the number of entries in the entried month
//    NSCalendar *calendar            = [[NSCalendar alloc] initWithCalendarIdentifier:NSCalendarIdentifierGregorian];
//    NSDateComponents *nowComponents = [calendar components:NSCalendarUnitEra | NSCalendarUnitYear | NSCalendarUnitMonth | NSCalendarUnitDay fromDate:transactionDate];
//    [nowComponents setDay:1];
//
//    NSDate *beginningOfCurrentMonth = [calendar dateFromComponents:nowComponents];
//    NSDateComponents *oneMonth      = [[NSDateComponents alloc] init];
//    [oneMonth setMonth:1];
//
//    // No need to set user class since multi account is valid for paid users
//    NSDate *beginningOfNextMonth    = [calendar dateByAddingComponents:oneMonth toDate:beginningOfCurrentMonth options:0];
//
//    User *user                      = [KPCoreDataGetHandler currentUserWithContext:nil];
//    NSPredicate *predicate          = [NSPredicate predicateWithFormat:@"date >= %@ AND date < %@ && deleted = NO && user = %@", beginningOfCurrentMonth, beginningOfNextMonth, user];
//
//    NSNumber *numberOfEntriesInTheMonth = [Entry MR_numberOfEntitiesWithPredicate:predicate];
//
//    NSInteger limitIntValue = [KPEntryLimitManager limitIntValue];
//
//    if (numberOfEntriesInTheMonth.intValue < limitIntValue) {
//        return NO;
//    }
//
//    return YES;
}
