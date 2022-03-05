package pg.gipter.core.producers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.dao.command.CustomCommand;
import pg.gipter.core.producers.command.*;
import pg.gipter.core.producers.vcs.VCSVersionProducer;
import pg.gipter.core.producers.vcs.VCSVersionProducerFactory;
import pg.gipter.services.ConcurrentService;
import pg.gipter.ui.task.UpdatableTask;
import pg.gipter.utils.SystemUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

import static java.util.stream.Collectors.joining;

abstract class AbstractDiffProducer implements DiffProducer {

    protected final ApplicationProperties applicationProperties;
    protected final Logger logger;
    private final Executor executor;
    private UpdatableTask<Void> task;

    AbstractDiffProducer(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.executor = ConcurrentService.getInstance().executor();
        logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public void produceDiff() {
        try {
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
            createDiffFile(diffDetails);

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
                    .collect(joining(SystemUtils.lineSeparator()))
            );

            final int interval = 500;
            int timeout = 0;
            while(timeout < 1000 * applicationProperties.fetchTimeout() && !future.isDone()) {
                Thread.sleep(interval);
                timeout += interval;
            }
            if (future.isDone()) {
                logger.debug(future.get());
            } else {
                logger.warn("Fetching the [{}] repository was cancelled after [{}] seconds.",
                        projectPath, applicationProperties.fetchTimeout()
                );
                future.cancel(true);
            }

        } catch (InterruptedException | ExecutionException ex) {
            logger.error("Fetching the [{}] repository  was interrupted. Task was taking more than [{}] seconds.",
                    projectPath, applicationProperties.fetchTimeout(), ex
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
        CustomCommand customCommand = applicationProperties.getCustomCommand(vcs);
        if (customCommand.isOverride() && customCommand.containsCommand(vcs)) {
            logger.info("Custom command is used.");
            cmd = customCommand.fullCommand(applicationProperties);
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
        LinkedList<String> fullCommand = new LinkedList<>();
        fullCommand.add("powershell.exe");
        fullCommand.addAll(cmd);
        ProcessBuilder processBuilder = new ProcessBuilder(fullCommand);
        processBuilder.directory(Paths.get(projectPath).toFile());
        processBuilder.environment().put("LANG", "pl_PL.UTF-8");
        Process process = processBuilder.start();

        Path newFilePath = createProjectFile(projectPath);

        try (InputStream is = process.getInputStream();
             Scanner sc = new Scanner(is, StandardCharsets.UTF_8.name())) {

            boolean hasDiff = false;
            while (sc.hasNextLine()) {
                String line = String.format("%s%n", sc.nextLine());
                hasDiff = true;
                Files.write(newFilePath, line.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            }

            if (sc.ioException() != null) {
                throw new IOException(
                        String.format("Scanner is throwing IOException for [%s], when reading from PowerShell.", projectPath)
                );
            }

            if (hasDiff) {
                Files.write(
                        newFilePath,
                        String.format("%nEnd-of-diff-for-%s%n%n%n", projectPath).getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.APPEND
                );
            } else {
                Files.write(
                        newFilePath,
                        String.format("For repository [%s] within period [from %s to %s] diff is unavailable!%n%n",
                                projectPath,
                                applicationProperties.startDate().format(ApplicationProperties.yyyy_MM_dd),
                                applicationProperties.endDate().format(ApplicationProperties.yyyy_MM_dd)
                        ).getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.APPEND
                );
            }
            diffDetails.setFilePath(newFilePath);
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

    private Path createProjectFile(String projectPath) throws IOException {
        String projectName = Paths.get(projectPath).toFile().getName() + "-tmp.txt";

        Path newFilePath = Paths.get(applicationProperties.itemPath());
        if (Files.isDirectory(newFilePath)) {
            newFilePath = Paths.get(applicationProperties.itemPath(), projectName);
        } else {
            newFilePath = Paths.get(newFilePath.getParent().toString(), projectName);
        }
        try {
            Files.deleteIfExists(newFilePath);
            Files.createFile(newFilePath);
            return newFilePath;
        } catch (IOException ex) {
            logger.error("Could not create the file [{}].", newFilePath, ex);
            throw ex;
        }
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

    private void createDiffFile(List<DiffDetails> diffDetails) {
        try {
            Files.deleteIfExists(Paths.get(applicationProperties.itemPath()));
            Files.createFile(Paths.get(applicationProperties.itemPath()));
        } catch (IOException ex) {
            logger.error("Could not delete existing file with item: [{}].", Paths.get(applicationProperties.itemPath()));
        }

        for (DiffDetails details : diffDetails) {
            try (InputStream is = Files.newInputStream(details.getFilePath());
                 Scanner sc = new Scanner(is, StandardCharsets.UTF_8.name())) {

                while (sc.hasNextLine()) {
                    String line = String.format("%s%n", sc.nextLine());
                    Files.write(
                            Paths.get(applicationProperties.itemPath()),
                            line.getBytes(StandardCharsets.UTF_8),
                            StandardOpenOption.APPEND
                    );
                }

                if (sc.ioException() != null) {
                    throw new IOException(
                            String.format("Diff scanner is throwing IOException for [%s].", details.getFilePath().toString())
                    );
                }

            } catch (Exception ex) {
                logger.error("Could not append result from [{}].", details.getFilePath().toString(), ex);
            } finally {
                try {
                    Files.deleteIfExists(details.getFilePath());
                } catch (IOException ex) {
                    logger.error("Problem with deleting file: [{}].", details.getFilePath().toString(), ex);
                }
            }
        }
    }

    protected abstract List<String> getFullCommand(List<String> diffCmd);

}
