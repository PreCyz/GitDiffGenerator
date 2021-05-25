package pg.gipter.core.producers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.dao.command.CustomCommand;
import pg.gipter.core.dao.command.CustomCommandDao;
import pg.gipter.core.producers.command.*;
import pg.gipter.core.producers.vcs.VCSVersionProducer;
import pg.gipter.core.producers.vcs.VCSVersionProducerFactory;
import pg.gipter.ui.task.UpdatableTask;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

import static java.util.stream.Collectors.joining;

abstract class AbstractDiffProducer implements DiffProducer {

    protected final ApplicationProperties applicationProperties;
    protected final Logger logger;
    private final Executor executor;
    private UpdatableTask<Void> task;

    AbstractDiffProducer(ApplicationProperties applicationProperties, Executor executor) {
        this.applicationProperties = applicationProperties;
        this.executor = executor;
        logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public void produceDiff() {
        try (FileWriter fw = new FileWriter(Paths.get(applicationProperties.itemPath()).toFile())) {
            List<Callable<DiffDetails>> diffs = new LinkedList<>();
            Set<VersionControlSystem> vcsSet = new HashSet<>();

            for (String projectPath : applicationProperties.projectPaths()) {
                logger.info("Project path: {}", projectPath);
                VersionControlSystem vcs = VersionControlSystem.valueFrom(Paths.get(projectPath));
                VCSVersionProducer VCSVersionProducer = VCSVersionProducerFactory.getInstance(vcs, projectPath);
                logger.info("Discovered '{}' version control system.", VCSVersionProducer.getVersion());

                diffs.add(createDiffCallable(projectPath, vcs));
                vcsSet.add(vcs);
            }
            applicationProperties.setVcs(vcsSet);

            List<DiffDetails> diffDetails = processCallable(diffs);
            writeDiffToFile(fw, diffDetails);

            boolean noDiff = diffDetails.stream().noneMatch(DiffDetails::isDiff);
            if (noDiff) {
                String errMsg = String.format("Diff could not be produced [from %s to %s].",
                        applicationProperties.startDate().format(ApplicationProperties.yyyy_MM_dd),
                        applicationProperties.endDate().format(ApplicationProperties.yyyy_MM_dd)
                );
                logger.warn(errMsg);
                throw new IllegalArgumentException(errMsg);
            }
            logger.info("Diff file generated and saved as: {}.", applicationProperties.itemPath());
        } catch (Exception ex) {
            logger.error("Error when producing diff.", ex);
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }

    @Override
    public void produceDiff(UpdatableTask<Void> task) {
        this.task = task;
        this.task.setDoubleIncrement(!applicationProperties.isFetchAll());
        produceDiff();
    }

    private Callable<DiffDetails> createDiffCallable(String projectPath, VersionControlSystem vcs) {
        return () -> {
            final DiffCommand diffCommand = DiffCommandFactory.getInstance(vcs, applicationProperties);
            if (applicationProperties.isFetchAll()) {
                updateRepositories(projectPath, diffCommand);
            }
            List<String> cmd = calculateCommand(diffCommand, vcs);
            return createDiffDetails(projectPath, cmd);
        };
    }

    private void updateRepositories(String projectPath, DiffCommand diffCommand) throws IOException {
        logger.info("Updating the repository [{}] with command [{}].", projectPath, String.join(" ", diffCommand.updateRepositoriesCommand()));
        ProcessBuilder processBuilder = new ProcessBuilder(diffCommand.updateRepositoriesCommand());
        processBuilder.directory(Paths.get(projectPath).toFile());
        Process process = processBuilder.start();

        try (InputStream is = process.getInputStream();
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr)) {

            Future<String> future = new ExecutorCompletionService<String>(executor).submit(() -> br.lines()
                    .filter(Objects::nonNull)
                    .collect(joining(System.getProperty("line.separator")))
            );

            final int interval = 500;
            int timeout = 0;
            while(timeout < 1000 * applicationProperties.fetchWaitTime() && !future.isDone()) {
                Thread.sleep(interval);
                timeout += interval;
            }
            if (future.isDone()) {
                logger.debug(future.get());
            } else {
                logger.warn("Fetching repository cancelled after {}[s].", applicationProperties.fetchWaitTime());
                future.cancel(true);
            }

        } catch (InterruptedException | ExecutionException ex) {
            logger.error("Fetching was interrupted. Task was taking more then [{}] seconds.",
                    applicationProperties.fetchWaitTime(), ex
            );
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new IOException(ex);
        } finally {
            if (task != null) {
                task.incrementProgress();
            }
        }
    }

    protected List<String> calculateCommand(DiffCommand diffCommand, VersionControlSystem vcs) {
        List<String> cmd;
        final Optional<CustomCommand> customCommand = CustomCommandDao.readCustomCommand();
        if (customCommand.isPresent() && customCommand.get().containsCommand(vcs)) {
            logger.info("Custom command is used.");
            cmd = customCommand.get().fullCommand(applicationProperties);
            logger.info("{} command: {}", vcs.name(), String.join(" ", cmd));
        } else {
            cmd = diffCommand.commandAsList();
            logger.info("{} command: {}", vcs.name(), String.join(" ", cmd));
            cmd = getFullCommand(cmd);
        }

        logger.info("Platform full command: {}", String.join(" ", cmd));
        return cmd;
    }

    private DiffDetails createDiffDetails(String projectPath, List<String> cmd) throws IOException {
        DiffDetails diffDetails = new DiffDetails(projectPath);
        StringBuilder stringBuilder = new StringBuilder();
        LinkedList<String> fullCommand = new LinkedList<>();
        fullCommand.add("powershell.exe");
        fullCommand.addAll(cmd);
        ProcessBuilder processBuilder = new ProcessBuilder(fullCommand);
        processBuilder.directory(Paths.get(projectPath).toFile());
        processBuilder.environment().put("LANG", "pl_PL.UTF-8");
        Process process = processBuilder.start();

        try (InputStream is = process.getInputStream();
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr)) {

            boolean hasDiff = false;
            String line;
            while ((line = br.readLine()) != null) {
                stringBuilder.append(String.format("%s%n", line));
                hasDiff = true;
            }

            if (hasDiff) {
                stringBuilder.append(String.format("%nEnd-of-diff-for-%s%n%n%n", projectPath));
            } else {
                stringBuilder.append(String.format("For repository [%s] within period [from %s to %s] diff is unavailable!%n",
                        projectPath,
                        applicationProperties.startDate().format(ApplicationProperties.yyyy_MM_dd),
                        applicationProperties.endDate().format(ApplicationProperties.yyyy_MM_dd)
                ));
            }
            diffDetails.setContent(stringBuilder.toString());
            diffDetails.setDiff(hasDiff);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new IOException(ex);
        } finally {
            if (task != null) {
                task.incrementProgress();
            }
        }
        return diffDetails;
    }

    private List<DiffDetails> processCallable(List<Callable<DiffDetails>> diffs) {
        CompletionService<DiffDetails> completionService = new ExecutorCompletionService<>(executor);
        diffs.forEach(completionService::submit);

        List<DiffDetails> result = new ArrayList<>(diffs.size());
        for (int i = 0; i < diffs.size(); i++) {
            try {
                result.add(completionService.take().get());
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error when calculating diff.", e);
            }
        }
        return result;
    }

    private void writeDiffToFile(FileWriter fw, List<DiffDetails> diffDetails) throws IOException {
        for (DiffDetails details : diffDetails) {
            fw.write(details.getContent());
        }
    }

    protected abstract List<String> getFullCommand(List<String> diffCmd);

}
