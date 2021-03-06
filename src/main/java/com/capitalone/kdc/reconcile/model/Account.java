package com.capitalone.kdc.reconcile.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Created by zry285 on 10/5/17.
 */
public class Account {

    private String id;
    private String nickName;
    private String status;
    private BigDecimal balance;
    private String type;
    private LocalDateTime lastUpdatedOn;

    //Transaction history.
    private List<Transaction> transactions;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getLastUpdatedOn() {
        return lastUpdatedOn;
    }

    public void setLastUpdatedOn(LocalDateTime lastUpdatedOn) {
        this.lastUpdatedOn = lastUpdatedOn;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void addTransaction(Transaction txn) {
        if (transactions == null) {
            transactions = new ArrayList<>();
        }
        transactions.add(txn);
    }
}
