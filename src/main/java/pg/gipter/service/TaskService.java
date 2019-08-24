package pg.gipter.service;

import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class TaskService<T> extends Task<T> {

    protected Logger logger;
    private long maxWork;
    private long workDone;

    protected TaskService() {
        logger = LoggerFactory.getLogger(getClass());
        this.maxWork = 100;
        workDone = 0;
    }

    void increaseTotalWorkAndProgress() {
        workDone +=100;
        maxWork += 100;
        updateProgress(workDone, maxWork);
    }

    void increaseProgress() {
        workDone++;
        updateProgress(workDone, maxWork);
    }

    void increaseProgress(long workDone) {
        this.workDone = workDone;
        updateProgress(this.workDone, maxWork);
    }

    void increaseProgressWithNewMax(long maxWork) {
        this.maxWork = maxWork;
        updateProgress(++workDone, this.maxWork);
    }

    void updateMsg(String message) {
        logger.info(message);
        updateMessage(message);
    }

    void workCompleted() {
        updateProgress(maxWork, maxWork);
    }
}
