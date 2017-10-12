package com.capitalone.kdc.reconcile.service;

import com.capitalone.kdc.reconcile.exception.TransactionException;
import com.capitalone.kdc.reconcile.model.Account;
import com.capitalone.kdc.reconcile.model.Transaction;
import com.capitalone.kdc.reconcile.util.DataUtil;
import com.capitalone.kdc.reconcile.util.TransactionJournalUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        boolean isSuccess = false;
        Account dAcct = validateAccount(fromAcct);
        LocalDateTime curr = LocalDateTime.now();
        Transaction txn = new Transaction();
        txn.setId(UUID.randomUUID().toString());
        txn.setcAccountId(dAcct.getId());
        txn.setAmount(amount);
        txn.setTxnType("CR");
        txn.setTimeStamp(curr);
        if (dAcct.getBalance().compareTo(amount) == 1) {
            dAcct.setBalance(dAcct.getBalance().subtract(amount));
            dAcct.setLastUpdatedOn(curr);
            txn.setStatus("Completed");
            dAcct.addTransaction(txn);
            isSuccess = true;
        } else {
            txn.setStatus("Failed");
            txn.setError("In sufficient balance.");
        }
        TransactionJournalUtil.journalEntry(txn);
        return isSuccess;
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
        List<String> journalEntries = TransactionJournalUtil.readJournalEntries(acct);
        if (journalEntries != null && journalEntries.size() > 0) {
            if (dAcct.getTransactions() != null && dAcct.getTransactions().size() > 0) {
                journalEntries.forEach(s -> dAcct.getTransactions().forEach(transaction -> {
                    if (s.contains(transaction.getTimeStamp().toString()) && s.contains(transaction.getStatus())) {
                        System.out.println("Account transactions exactly matched with Journal entries.");
                    } else {
                        throw new RuntimeException("There is miss match found in Account transaction and journal entries.");
                    }
                }));
            } else {
                throw new RuntimeException("There is miss match found in Account transaction and journal entries.");
            }
        } else {
            if (dAcct.getTransactions() != null && dAcct.getTransactions().size() > 0) {
                throw new RuntimeException("There is miss match found in Account transaction and journal entries.");
            } else  {
                System.out.println("No Entries found in journal, and zero transaction.");
            }
        }

    }

    private Account validateAccount(String accountId) throws TransactionException {
        if (accountInMemoryDatabase.containsKey(accountId)) {
            return accountInMemoryDatabase.get(accountId);
        } else {
            throw new TransactionException("Account not Found.");
        }
    }
}
