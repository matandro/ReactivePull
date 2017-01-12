package bgu.cbs.reactivePull.Impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by matan on 1/3/2017.
 */
public class LogSingleton {
    private PrintWriter pw;
    private SimpleDateFormat sdf;

    private static class SingletonHolder {
        private static LogSingleton instance = new LogSingleton();
    }

    private LogSingleton() {
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        File log = new File("log.txt");
        try {
            if (log.exists())
                log.delete();
            pw = new PrintWriter(new FileOutputStream(log));
        } catch (FileNotFoundException e) {
            pw = null;
            System.err.println("Failed to initiate log");
        }
    }

    public static LogSingleton getInstance() {
        return SingletonHolder.instance;
    }

    private String getPretext() {
        String thread = Thread.currentThread().getName();
        String time = sdf.format(new Date());
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement callingFunc = stackTraceElements[3];

        return time + " [" + thread + "] - [" + callingFunc.getClassName() + "." + callingFunc.getMethodName() + ":" + callingFunc.getLineNumber() + "]: ";
    }

    public void println(String msg) {
        pw.println(getPretext() + msg);
        pw.flush();
    }
}
