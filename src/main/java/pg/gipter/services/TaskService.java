package pg.gipter.services;

import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TaskService<T> extends Task<T> {

    protected Logger logger;
    private long maxWork;
    private long workDone;
    private static final int NUMBER_OF_STEPS = 3;
    private final long defaultValue = Double.valueOf(Math.pow(10, 6)).longValue();

    protected TaskService() {
        logger = LoggerFactory.getLogger(getClass());
        workDone = 0;
        maxWork = defaultValue;
    }

    public void initProgress(long maxWork) {
        if (maxWork == 0) {
            maxWork = 20 * defaultValue;
        }
        this.maxWork = maxWork + (NUMBER_OF_STEPS - 1) * defaultValue;
    }

    public void increaseProgress() {
        workDone++;
        updateProgress(workDone, maxWork);
    }

    public void increaseProgress(long workDone) {
        this.workDone = workDone;
        updateProgress(this.workDone, maxWork);
    }

    public void updateTaskProgress(long workDone) {
        this.workDone += workDone;
        updateProgress(this.workDone, maxWork);
    }

    public void updateMsg(String message) {
        logger.info(message);
        updateMessage(message);
    }

    public void workCompleted() {
        updateProgress(maxWork, maxWork);
    }
}
