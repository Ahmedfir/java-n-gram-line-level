package utils;


import java.util.logging.Level;
import java.util.logging.Logger;

public final class LogFactory {

    private static final String LEVEL_STR = System.getProperty("log");//"I");
    private static final Level LEVEL = "I".equals(LEVEL_STR)? Level.INFO: "N".equals(LEVEL_STR)? Level.OFF: Level.ALL;

    private LogFactory() throws IllegalAccessException {
        throw new IllegalAccessException("Utility class : static access only.");
    }

    public static Logger logger(Class<?> clazz){
        Logger l = Logger.getLogger(clazz.getSimpleName());
        l.setLevel(LEVEL);
        return l;
    }
}
