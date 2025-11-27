package com.khoa.notebooklm.database.dao;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatabaseBackup {
    private static final String MYSQL_DUMP_PATH = "D:/MySQL/MySQL Server 8.0/bin/mysqldump.exe";
    private static final String DB_NAME = "chatbotjava";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "$MinhKhoi123$";

    /**
     * Exports the database to a .sql file in the specified folder.
     * @param saveFolderPath The directory where the file should be saved (e.g., ".")
     * @return true if successful, false if failed
     */
    public boolean exportDatabase(String saveFolderPath) {

        // 1. Check if mysqldump exists at the path
        File dumpTool = new File(MYSQL_DUMP_PATH);
        if (!dumpTool.exists()) {
            System.err.println("❌ ERROR: mysqldump.exe not found at: " + MYSQL_DUMP_PATH);
            System.err.println("Please update the path in DatabaseBackup.java");
            return false;
        }

        // 2. Generate a unique filename (e.g., backup_2023-11-22_10-30.sql)
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String filename = "backup_" + timestamp + ".sql";
        String fullPath = saveFolderPath + File.separator + filename;

        // 3. Build the command
        // Command structure: mysqldump -u [user] -p[password] --databases [db] -r [filepath]
        // Note: No space between -p and the password!
        ProcessBuilder pb = new ProcessBuilder(
                MYSQL_DUMP_PATH,
                "-u" + DB_USER,
                "-p" + DB_PASS,
                "--databases",
                DB_NAME,
                "-r",
                fullPath
        );

        try {
            // 4. Run the process
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("✅ Database exported successfully to: " + fullPath);
                return true;
            } else {
                System.err.println("❌ Backup failed. Exit code: " + exitCode);
                return false;
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Cannot connect to database server " + e.getMessage());
            return false;
        }
    }
}