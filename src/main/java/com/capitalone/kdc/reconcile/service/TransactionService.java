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
        List<String> journalEntries = TransactionJournalUtil.readJournalEntries(acct);
        if (dAcct.getTransactions() != null && journalEntries != null) {
            dAcct.getTransactions().sort(Transaction.sortById);
            sortJournalEntries(journalEntries);
            boolean missMatchFound = false;
            int jCount = journalEntries.size();
            int txCount = dAcct.getTransactions().size();

            for (int i = 0; i < (jCount < txCount ? jCount : txCount); i++) {
                String jEntry = journalEntries.get(i);
                Transaction tx = dAcct.getTransactions().get(i);

                if (!(jEntry.contains(tx.getTimeStamp().toString())
                        && jEntry.contains(tx.getAmount().toString())
                        && jEntry.contains(tx.getStatus()))) {
                    TransactionJournalUtil.writeReconcileLog(jEntry);
                    missMatchFound = true;
                }
            }

            if (jCount > txCount) {
                for (int i = txCount; i < jCount; i++) {
                    TransactionJournalUtil.writeReconcileLog(journalEntries.get(i));
                }
                missMatchFound = true;
            }

            if (txCount > jCount) {
                for (int i = jCount; i < txCount; i++) {
                    TransactionJournalUtil.writeReconcileLog(dAcct.getTransactions().get(i).log());
                }
                missMatchFound = true;
            }

            if (missMatchFound) {
                throw new RuntimeException("There is miss match found in Account transaction and journal entries.");
            } else {
                System.out.println("Account transactions exactly matched with Journal entries.");
            }
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
