package me.besser;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DIETLogger {
    private static Logger logger;

    public static void initialize(TAPI plugin) {
        logger = plugin.getLogger();
        // TODO: Import chat colors, so its not needed in each file, and so we can just call the color
        // Also add logger level param when creating this class in Teto.java.
    }

    /**
     * Helper method to shorten the usage of {@code log}.
     * Automatically severity imports levels.
     * @param level   the logging level, such as INFO, WARNING, SEVERE, etc.
     * @param message the message to be logged, can be any type.
     */
    public static void log(Level level, Object message) {
        if (logger != null) {
            logger.log(level, String.valueOf(message));
        } else {
            System.out.println("Logger not initialized: " + message);
        }
    }

    /**
     * Helper method to shorten the usage of {@code log}.
     * Automatically severity imports levels.
     * @param level     the logging level, such as INFO, WARNING, SEVERE, etc.
     * @param message   the message to be logged, can be any type.
     * @param throwable a throwable error message.
     *
     */
    public static void log(Level level, Object message, Throwable throwable) {
        if (logger != null) {
            logger.log(level, String.valueOf(message), throwable);
        } else {
            System.out.println("Logger not initialized: " + message);
        }
    }

    // So we don't have to import the levels
    public static final Level INFO = Level.INFO;
    public static final Level WARNING = Level.WARNING;
    public static final Level SEVERE = Level.SEVERE;

    public static final Level CONFIG = Level.CONFIG;
    public static final Level FINE = Level.FINE;
    public static final Level FINER = Level.FINER;
    public static final Level FINEST = Level.FINEST;
}
