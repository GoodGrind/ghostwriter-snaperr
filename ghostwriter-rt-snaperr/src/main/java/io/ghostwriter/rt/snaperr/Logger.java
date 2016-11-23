package io.ghostwriter.rt.snaperr;

import org.slf4j.LoggerFactory;

/**
 * This class as an abstraction is only for safety in case we have to switch logging framework.
 * Most probably it will be eliminated in favor of slf4j
 *
 * @author pal
 */
public class Logger {

    private final org.slf4j.Logger slf4jLogger;

    private Logger(String name) {
        slf4jLogger = LoggerFactory.getLogger(name);
    }

    public static Logger getLogger(String name) {
        return new Logger(name);
    }

    public void error(String msg) {
        error(msg, null);
    }

    public void error(String msg, Throwable e) {
        log(LogLevel.ERROR, msg, e);
    }

    public void info(String msg) {
        info(msg, null);
    }

    public void info(String msg, Throwable e) {
        log(LogLevel.INFO, msg, e);
    }

    public void warn(String msg) {
        warn(msg, null);
    }

    public void warn(String msg, Throwable e) {
        log(LogLevel.WARN, msg, e);
    }

    public void debug(String msg) {
        debug(msg, null);
    }

    public void debug(String msg, Throwable e) {
        log(LogLevel.DEBUG, msg, e);
    }

    private void log(LogLevel level, String msg, Throwable e) {
        switch (level) {
            case DEBUG:
                if (e == null) {
                    slf4jLogger.debug(msg);
                } else {
                    slf4jLogger.debug(msg, e);
                }
                break;
            case INFO:
                if (e == null) {
                    slf4jLogger.info(msg);
                } else {
                    slf4jLogger.info(msg, e);
                }
                break;
            case WARN:
                if (e == null) {
                    slf4jLogger.warn(msg);
                } else {
                    slf4jLogger.warn(msg, e);
                }
                break;
            case ERROR:
                if (e == null) {
                    slf4jLogger.error(msg);
                } else {
                    slf4jLogger.error(msg, e);
                }
                break;
        }
    }

    public static enum LogLevel {
        ERROR, WARN, DEBUG, INFO
    }
}
