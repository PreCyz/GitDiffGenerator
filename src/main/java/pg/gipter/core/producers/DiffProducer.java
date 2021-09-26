package pg.gipter.core.producers;

import pg.gipter.ui.task.UpdatableTask;

public interface DiffProducer {

    void produceDiff();
    void produceDiff(UpdatableTask<Void> task);

}
