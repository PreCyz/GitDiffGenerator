package pg.gipter.ui.task;

import javafx.concurrent.Task;

import java.util.concurrent.atomic.AtomicLong;

public abstract class UpdatableTask<T> extends Task<T> {

    protected final int NUMBER_OF_STEPS = 5;
    protected final long INCREMENT_FACTOR = 2L;
    private long max;
    private final AtomicLong workDone;
    private boolean doubleIncrement;

    public UpdatableTask() {
        max = 100L;
        workDone = new AtomicLong(0);
    }

    protected void setMax(long max) {
        this.max = max;
    }

    public void setDoubleIncrement(boolean doubleIncrement) {
        this.doubleIncrement = doubleIncrement;
    }

    protected long getMax() {
        return max;
    }

    public void incrementProgress() {
        super.updateProgress(workDone.incrementAndGet(), max);
        if (doubleIncrement) {
            super.updateProgress(workDone.incrementAndGet(), max);
        }
    }

    public void incrementProgress(long value) {
        workDone.set(value);
        super.updateProgress(workDone.get(), max);
    }

}
