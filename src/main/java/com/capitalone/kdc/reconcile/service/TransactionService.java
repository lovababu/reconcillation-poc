package com.capitalone.kdc.reconcile.service;

import com.capitalone.kdc.reconcile.exception.TransactionException;
import com.capitalone.kdc.reconcile.model.Account;
import com.capitalone.kdc.reconcile.model.Transaction;
import com.capitalone.kdc.reconcile.util.DataUtil;
import com.capitalone.kdc.reconcile.util.TransactionJournalUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Created by zry285 on 10/5/17.
 */
public class TransactionService {


    private final Map<String, Account> accountInMemoryDatabase;

    public TransactionService() {
        this.accountInMemoryDatabase = new HashMap<>();
        init();
    }

    private void init() {
        DataUtil.feedAccoountData(accountInMemoryDatabase);
    }

    public Account get(String act) throws TransactionException {
        return validateAccount(act);
    }

    public boolean draw(String fromAcct, BigDecimal amount) throws TransactionException, IOException {
        boolean isSuccess;
        Account dAcct = validateAccount(fromAcct);
        LocalDateTime curr = LocalDateTime.now();
        Transaction txn = new Transaction();
        txn.setId(UUID.randomUUID().toString());
        txn.setdAccountId(dAcct.getId());
        txn.setAmount(amount);
        txn.setTxnType("DR");
        txn.setTimeStamp(curr);
        if (dAcct.getBalance().compareTo(amount) == 1) {
            dAcct.setBalance(dAcct.getBalance().subtract(amount));
            dAcct.setLastUpdatedOn(curr);
            txn.setStatus("Completed");
            dAcct.addTransaction(txn);
            isSuccess = true;
        } else {
            txn.setTimeStamp(curr);
            txn.setStatus("Failed");
            txn.setError("In sufficient balance.");
            isSuccess = false;
        }
        TransactionJournalUtil.journalEntry(txn);
        return isSuccess;
    }

    public boolean credit(String fromAcct, BigDecimal amount) throws TransactionException, IOException {
        Account cAcct = validateAccount(fromAcct);
        LocalDateTime curr = LocalDateTime.now();
        Transaction txn = new Transaction();
        txn.setId(UUID.randomUUID().toString());
        txn.setcAccountId(cAcct.getId());
        txn.setAmount(amount);
        txn.setTxnType("CR");
        txn.setTimeStamp(curr);
        cAcct.setBalance(cAcct.getBalance().add(amount));
        cAcct.setLastUpdatedOn(curr);
        txn.setStatus("Completed");
        cAcct.addTransaction(txn);
        TransactionJournalUtil.journalEntry(txn);
        return true;
    }

    public boolean transact(String fromAcct, String toAcct, BigDecimal amount) throws TransactionException, IOException {
        boolean isSuccess;
        Account dAcct = validateAccount(fromAcct);
        //should not validate, since we may not know the third-party account at this stage.
        Account cAcct = accountInMemoryDatabase.get(toAcct);
        LocalDateTime curr = LocalDateTime.now();
        Transaction txn = new Transaction();
        txn.setId(UUID.randomUUID().toString());
        txn.setdAccountId(dAcct.getId());
        txn.setcAccountId(cAcct.getId());
        txn.setAmount(amount);
        txn.setTxnType("TX");
        txn.setTimeStamp(curr);
        if (dAcct.getBalance().compareTo(amount) == 1) {
            dAcct.setBalance(dAcct.getBalance().subtract(amount));
            cAcct.setBalance(cAcct.getBalance().add(amount));
            dAcct.setLastUpdatedOn(curr);
            txn.setStatus("Completed");
            dAcct.addTransaction(txn);
            isSuccess = true;
        } else {
            txn.setStatus("Failed");
            txn.setError("In sufficient balance.");
            isSuccess = false;
        }
        TransactionJournalUtil.journalEntry(txn);
        return isSuccess;
    }

    public void reconcillationOn(String acct) throws TransactionException, IOException {
        Account dAcct = validateAccount(acct);
        //get the journal entries for the acct.
        dAcct.getTransactions().sort(Transaction.sortById);
        List<String> journalEntries = TransactionJournalUtil.readJournalEntries(acct);
        sortJournalEntries(journalEntries);
        int jCount = journalEntries != null ? journalEntries.size() : 0;
        int txCount = dAcct.getTransactions() != null ? dAcct.getTransactions().size() : 0;
        int loop = jCount > txCount ? jCount : txCount;
        if (journalEntries != null && dAcct.getTransactions() != null) {
            for (int i = 0; i < loop; i++) {
                String jEntry = journalEntries.get(i);
                Transaction tx = dAcct.getTransactions().get(i);
                if (!(jEntry.contains(tx.getTimeStamp().toString())
                        && jEntry.contains(tx.getAmount().toString())
                        && jEntry.contains(tx.getStatus()))) {
                    TransactionJournalUtil.writeReconcileLog(jEntry);
                    throw new RuntimeException("There is miss match found in Account transaction and journal entries.");
                }
            }
            System.out.println("Account transactions exactly matched with Journal entries.");
        }
    }

    private void sortJournalEntries(List<String> journalEntries) {
        journalEntries.sort((o1, o2) -> {
            String o1Id = o1.split(",")[0];
            String o2Id = o2.split(",")[0];
            return o1Id.compareTo(o2Id);
        });
    }

    private Account validateAccount(String accountId) throws TransactionException {
        if (accountInMemoryDatabase.containsKey(accountId)) {
            return accountInMemoryDatabase.get(accountId);
        } else {
            throw new TransactionException("Account not Found.");
        }
    }
}
