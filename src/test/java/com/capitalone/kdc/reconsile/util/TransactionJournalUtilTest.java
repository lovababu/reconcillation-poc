package com.capitalone.kdc.reconsile.util;

import com.capitalone.kdc.reconcile.model.Transaction;
import com.capitalone.kdc.reconcile.util.TransactionJournalUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Created by lovababu on 08/10/17.
 */
@RunWith(JUnit4.class)
public class TransactionJournalUtilTest {

    @Test
    public void testWrite() {
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID().toString());
        transaction.setdAccountId("Acct001");
        transaction.setStatus("Completed");
        transaction.setTxnType("CR");
        transaction.setTimeStamp(LocalDateTime.now());
        transaction.setAmount(new BigDecimal(34));

        try {
            TransactionJournalUtil.journalEntry(transaction);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRead() {
        testWrite();
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID().toString());
        transaction.setdAccountId("Acct001");
        transaction.setStatus("Completed");
        transaction.setTxnType("CR");
        transaction.setTimeStamp(LocalDateTime.now());
        transaction.setAmount(new BigDecimal(34));

        try {
            List<String> lines = TransactionJournalUtil.readJournalEntries("Acct001");
            lines.stream().forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
