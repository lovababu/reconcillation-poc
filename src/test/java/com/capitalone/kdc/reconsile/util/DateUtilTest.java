package com.capitalone.kdc.reconsile.util;

import com.capitalone.kdc.reconcile.model.Account;
import com.capitalone.kdc.reconcile.util.DataUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zry285 on 10/5/17.
 */
@RunWith(JUnit4.class)
public class DateUtilTest {

    @Test
    public void testFeedAccountData() {
       final Map<String, Account>  accountData = new HashMap<>();
        DataUtil.feedAccoountData(accountData);
        System.out.println(accountData);
        Assert.assertTrue(accountData.size() == 10);
        Assert.assertNotNull(accountData.get("00001"));
        Assert.assertEquals(10, accountData.get("00001").getBalance().intValue());
    }
}
