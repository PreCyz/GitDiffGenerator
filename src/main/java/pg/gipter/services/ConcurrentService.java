package pg.gipter.services;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ConcurrentService {

    private final Executor executor;

    private static class ExecutorServiceHolder {
        private static final ConcurrentService INSTANCE = new ConcurrentService();
    }

    private ConcurrentService() {
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public static ConcurrentService getInstance() {
        return ExecutorServiceHolder.INSTANCE;
    }

    public Executor executor() {
        return executor;
    }

    public int availableThreads() {
        return Runtime.getRuntime().availableProcessors();
    }

}
