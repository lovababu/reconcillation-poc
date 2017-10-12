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

    public static void journalEntry(Transaction tx) throws IOException {
        if (createFileIfnotExist()) {
            System.out.println("File not exist, so created new one.");
        } else {
            System.out.println("File exist, so appending logs.");
        }
        Files.write(Paths.get(JOURNAL_PATH), tx.log().getBytes(),
                StandardOpenOption.APPEND);
    }

    public static List<String> readJournalEntries(String acct) throws IOException {
        return Files.readAllLines(Paths.get(JOURNAL_PATH), Charset.defaultCharset())
                .stream()
                .filter(s -> s.contains(acct))
                .collect(Collectors.toList());
    }

    /**
     * Returns true if the file created now, else false.
     *
     * @return boolean
     * @throws IOException
     */
    private static boolean createFileIfnotExist() throws IOException {
        File file = Paths.get(JOURNAL_PATH).toFile();
        if (!file.exists()) {
            return file.createNewFile();
        } else {
            return false;
        }
    }
}
