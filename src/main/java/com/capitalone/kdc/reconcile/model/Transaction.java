package com.capitalone.kdc.reconcile.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;

/**
 * Created by zry285 on 10/5/17.
 */
public class Transaction {

    private String id;
    private String dAccountId;
    private String cAccountId;
    private LocalDateTime timeStamp;
    private String txnType;
    private BigDecimal amount;
    private String status;
    private String error;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getdAccountId() {
        return dAccountId;
    }

    public void setdAccountId(String dAccountId) {
        this.dAccountId = dAccountId;
    }

    public String getcAccountId() {
        return cAccountId;
    }

    public void setcAccountId(String cAccountId) {
        this.cAccountId = cAccountId;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String log() {
        if ("TX".equalsIgnoreCase(this.getTxnType())) {
            if ("Completed".equalsIgnoreCase(this.getStatus())) {
                return String.format("%s,%s,%s,%s,%.2f,%s,%s\n", this.getId(), this.getTimeStamp().toString(),
                        this.getdAccountId(), this.getcAccountId(), this.getAmount(), this.getTxnType(),  this.getStatus());
            } else {
                return String.format("%s,%s,%s,%s,%.2f,%s,%s,%s\n", this.getId(), this.getTimeStamp().toString(),
                        this.getdAccountId(), this.getcAccountId(), this.getAmount(), this.getTxnType(),  this.getStatus(), this.getError());
            }

        } else {
            if ("Completed".equalsIgnoreCase(this.getStatus())) {
                return String.format("%s,%s,%s,%.2f,%s,%s\n", this.getId(), this.getTimeStamp().toString(),
                        this.getTxnType().equalsIgnoreCase("CR") ? this.getcAccountId() : this.getdAccountId(),
                        this.getAmount(), this.getTxnType(), this.getStatus());
            } else {
                return String.format("%s,%s,%s,%.2f,%s,%s,%s\n", this.getId(), this.getTimeStamp().toString(),
                        this.getTxnType().equalsIgnoreCase("CR") ? this.getcAccountId() : this.getdAccountId(),
                        this.getAmount(), this.getTxnType(), this.getStatus(), this.getError());
            }
        }
    }

    public static Comparator<Transaction> sortById = Comparator.comparing(Transaction::getId);
}
