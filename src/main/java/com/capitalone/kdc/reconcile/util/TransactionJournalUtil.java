package com.capitalone.kdc.reconcile.util;

import com.capitalone.kdc.reconcile.model.Transaction;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Transaction Journal utility calls logs all the  transaction in a file, each line represents a single transaction.
 * Created by zry285 on 10/5/17.
 */
public class TransactionJournalUtil {
    private static final String JOURNAL_PATH = "./transaction.log";
    private static final String RECONCILE_LOG = "./reconcile.log";

    public static void journalEntry(Transaction tx) throws IOException {
        createFileIfnotExist(JOURNAL_PATH);
        Files.write(Paths.get(JOURNAL_PATH), tx.log().getBytes(),
                StandardOpenOption.APPEND);
    }

    public static List<String> readJournalEntries(String acct) throws IOException {
        return Files.readAllLines(Paths.get(JOURNAL_PATH), Charset.defaultCharset())
                .stream()
                .filter(s -> s.contains(acct))
                .collect(Collectors.toList());
    }

    public static void writeReconcileLog(String log) throws IOException {
        createFileIfnotExist(RECONCILE_LOG);
        Files.write(Paths.get(RECONCILE_LOG), log.getBytes(),
                StandardOpenOption.APPEND);
    }

    /**
     * Returns true if the file created now, else false.
     *
     * @return boolean
     * @throws IOException
     */
    private static boolean createFileIfnotExist(String path) throws IOException {
        File file = Paths.get(path).toFile();
        if (!file.exists()) {
            return file.createNewFile();
        } else {
            return false;
        }
    }
}
