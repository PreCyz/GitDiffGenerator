package pg.gipter.service;

import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class TaskService<T> extends Task<T> {

    protected Logger logger;
    private long maxWork;
    private long workDone;
    private final int numberOfSteps = 3;

    protected TaskService() {
        logger = LoggerFactory.getLogger(getClass());
        workDone = 0;
        maxWork = (long) Math.pow(10, 6);
    }

    void init(long maxWork) {
        this.maxWork = maxWork + (numberOfSteps - 1) * (long) Math.pow(10, 6);
    }

    void increaseProgress() {
        workDone++;
        updateProgress(workDone, maxWork);
    }

    void increaseProgress(long workDone) {
        this.workDone = workDone;
        updateProgress(this.workDone, maxWork);
    }

    void updateTaskProgress(long workDone) {
        this.workDone += workDone;
        updateProgress(this.workDone, maxWork);
    }

    void updateMsg(String message) {
        logger.info(message);
        updateMessage(message);
    }

    void workCompleted() {
        updateProgress(maxWork, maxWork);
    }
}
