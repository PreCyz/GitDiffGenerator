package pg.gipter.services;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConcurrentService {

    private final ExecutorService executor;

    private static class ExecutorServiceHolder {
        private static final ConcurrentService INSTANCE = new ConcurrentService();
    }

    private ConcurrentService() {
        executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    public static ConcurrentService getInstance() {
        return ExecutorServiceHolder.INSTANCE;
    }

    public ExecutorService executor() {
        return executor;
    }

    public int availableThreads() {
        return Runtime.getRuntime().availableProcessors();
    }

}
