package com.capitalone.kdc.reconcile.util;

import com.capitalone.kdc.reconcile.model.Account;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Created by zry285 on 10/5/17.
 */
public class DataUtil {

    public static void feedAccoountData(final Map<String, Account> inMemoryAccountData) {
        IntStream.rangeClosed(1, 10).forEach(value -> prepareAccount(value, inMemoryAccountData));
    }

    private static void prepareAccount(int value, Map<String, Account> inMemoryAccountData) {
        Account act = new Account();
        act.setId(String.format("%05d", value)); //00010 and so on
        act.setNickName(String.format("MyCappAcct_%d", value));
        act.setStatus("active");
        act.setBalance(new BigDecimal(value * 10)); //act 1 balance will be 10, 2 will be 20 and so on.
        act.setLastUpdatedOn(LocalDateTime.now());
        inMemoryAccountData.put(act.getId(), act);
    }
}
