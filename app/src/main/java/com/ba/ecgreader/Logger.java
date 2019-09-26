package com.ba.ecgreader;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Logger {

    public static PrintWriter printWriter = null;
    public static final String LOG_DIR = "bbb";
    public static final String LOG_FILE = "log123.txt";
    //String filename = context.getExternalFilesDir(null).getPath() + File.separator + "my_app.log";
    private static void init() {

        // Check if external media is writable

        if (printWriter == null) {
            try {
                File dir = new File(Environment.getExternalStorageDirectory() + LOG_DIR);
                dir.mkdirs();
                printWriter = new PrintWriter(new FileWriter(new File(dir, LOG_FILE), true));
            } catch (IOException e) {
                Log.e(Logger.class.getName(), "initExternal() -> IOException", e);
            }
        }
    }

    private static synchronized int log(int priority, String tag, String msg) {
        int res = Log.println(priority, tag, msg);

        init(); // May be called just once, depending on your requirements

        printWriter.print(tag + "   ");
        printWriter.print(msg + "\r\n");
        printWriter.flush();
        return res;
    }


    // Duplicates of standard android.util.Log methods:
    public static int v(String tag, String msg) {
        return log(Log.VERBOSE, tag, msg);
    }

    public static int v(String tag, String msg, Throwable tr) {
        return log(Log.VERBOSE, tag, msg + '\n' + Log.getStackTraceString(tr));
    }

    public static int d(String tag, String msg) {
        return log(Log.DEBUG, tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        return log(Log.DEBUG, tag, msg + '\n' + Log.getStackTraceString(tr));
    }


}