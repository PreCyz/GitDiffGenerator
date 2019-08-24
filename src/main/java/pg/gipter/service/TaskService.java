package pg.gipter.service;

import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class TaskService<T> extends Task<T> {

    protected Logger logger;
    private long maxWork;

    protected TaskService() {
        logger = LoggerFactory.getLogger(getClass());
        this.maxWork = 100;
    }

    void taskUpdateProgress(long workDone) {
        updateProgress(workDone, maxWork);
    }

    void taskUpdateProgress(long workDone, long maxWork) {
        this.maxWork = maxWork;
        updateProgress(workDone, this.maxWork);
    }

    void taskUpdateMessage(String message) {
        logger.info(message);
        updateMessage(message);
    }

    void workCompleted() {
        updateProgress(maxWork, maxWork);
    }
}
