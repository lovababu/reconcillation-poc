package com.capitalone.kdc.reconcile;

import com.capitalone.kdc.reconcile.exception.TransactionException;
import com.capitalone.kdc.reconcile.model.Account;
import com.capitalone.kdc.reconcile.service.TransactionService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Created by zry285 on 10/5/17.
 */
public class ReconcileApp {

    private static  final TransactionService transactionService = new TransactionService();
    private static Scanner scanner = new Scanner(System.in);
    public static void main(String[] args) {
        System.out.println("************ Menu ******************");
        System.out.println("Transactions   : 1");
        System.out.println("Statement      : 2");
        System.out.println("Reconciliation : 3");
        System.out.println("Exit           : 4");
        while (true) {
            System.out.println("Enter your choice: ");
            int option = scanner.nextInt();
            if (option == 1) {
                transactions();
            } else if (option == 2) {
                statement();
            } else if (option == 3){
                reconciliation();
            } else if (option == 4) {
                System.exit(0);
            }else {
                System.out.println("Invalid Option chosen.");
            }
        }

    }

    private static void transactions() {
        while (true) {
            System.out.print("Enter Account Num: (00001 - 00010) :");
            String acct1 = scanner.next();

            System.out.println();

            System.out.println("Enter Transaction type: (CR, DR, TX) :");
            String txnType = scanner.next();

            System.out.println();

            String acct2 = null;
            if (!Arrays.asList("TX", "CR", "DR").contains(txnType.toUpperCase())) {
                System.out.println("Invalid transaction type.");
                System.exit(0);
            } else {
                if (txnType.equalsIgnoreCase("TX")) {
                    System.out.print("Enter third party Account : ");
                    acct2 = scanner.next();
                }
            }

            System.out.println();

            System.out.print("Enter Amount to transact: ");
            BigDecimal amount = scanner.nextBigDecimal();

            System.out.println();
            boolean isSuccess = false;
            if (txnType.equalsIgnoreCase("CR")) {
                try {
                    isSuccess = transactionService.credit(String.valueOf(acct1), amount);
                } catch (TransactionException | IOException e) {
                    e.printStackTrace();
                }
            } else if (txnType.equalsIgnoreCase("DR")) {
                try {
                    isSuccess = transactionService.draw(String.valueOf(acct1), amount);
                } catch (TransactionException | IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    isSuccess = transactionService.transact(String.valueOf(acct1), acct2, amount);
                } catch (TransactionException | IOException e) {
                    e.printStackTrace();
                }
            }

            if (isSuccess) {
                System.out.println("Transaction success.");
            } else {
                System.out.println("Transaction failed, check the journal log.");
            }

            System.out.println("Press 'C' to continue: ");
            System.out.println("Press 'R' to goto main menu.");
            String c = scanner.next();

            if (c.equalsIgnoreCase("R")) {
                return;
            }

            if (!c.equalsIgnoreCase("C")) {
                System.exit(0);
            }
        }
    }

    private static void statement() {
        System.out.print("Enter Account Num: (00001 - 00010) :");
        String acct = scanner.next();

        System.out.println();

        try {
            Account account = transactionService.get(acct);
            System.out.println(String.format("Account Num: %s, Balance: %.2f, NickName: %s, LastUpdated: %s",
                    account.getId(), account.getBalance(), account.getNickName(), account.getLastUpdatedOn().toString()));

            if (account.getTransactions() != null && account.getTransactions().size()  > 0) {
                account.getTransactions().forEach(tx ->
                        System.out.println(
                                String.format("%s, %s, %.2f, %s", tx.getId(), tx.getTxnType(), tx.getAmount(), tx.getStatus())
                        ));
            } else  {
                System.out.println("No Transactions found.");
            }
        } catch (TransactionException e) {
            e.printStackTrace();
        }
    }

    private static void reconciliation() {
        System.out.print("Enter Account Num: (00001 - 00010) :");
        String acct = scanner.next();

        System.out.println();

        try {
            transactionService.reconcillationOn(acct);
        } catch (TransactionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
