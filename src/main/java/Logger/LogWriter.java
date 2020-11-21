package Logger;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LogWriter {

    public LogWriter() {
        initialize();
    }


    //region Private Members
    private Logger logger;
    private static volatile LogWriter instance = null;
    private static Object lockObject = new Object();
    //endregion

    // region Public Members

    public static LogWriter getInstance() {
        if (instance == null) {
            synchronized (lockObject) {
                if (instance == null)
                    instance = new LogWriter();
            }
        }
        return instance;
    }
    //endregion

    //region Private Methods
    private void initialize() {
        try {
            logger = LogManager.getLogManager().getLogger("");
        } catch (Exception ex) {
        }
    }
    //endregion

    //region Public Methods

    public Logger getLogger() {
        return logger;
    }

    public void write(Level level, String log) {
        try {
            logger.log(level, log);

        } catch (Exception ex) {
        }
    }

    public void close(){
    }
    //endregion
}
