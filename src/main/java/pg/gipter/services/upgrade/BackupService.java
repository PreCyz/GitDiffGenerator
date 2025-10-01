package pg.gipter.services.upgrade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.services.TaskService;
import pg.gipter.utils.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public final class BackupService {
    private static final Logger logger = LoggerFactory.getLogger(BackupService.class);

    static final Set<String> EXCLUSIONS = Set.of("runtime", "logs", "Gipter.exe", "Gipter.jar", "app");

    private BackupService() {}

    public static Path gipterTmp() {
        return Paths.get(SystemUtils.tmp(), "Gipter-backup");
    }

    public static void backupAppFiles(TaskService<Void> taskService) throws Exception {
        if (!Files.exists(gipterTmp())) {
            Files.createDirectory(gipterTmp());
        }

        final AtomicInteger counter = new AtomicInteger(0);
        Path sourceDirectory = Paths.get(".").toAbsolutePath().normalize();
        try (Stream<Path> paths = Files.walk(sourceDirectory)) {
            paths.forEach(source -> {
                Path destination = Paths.get(
                        gipterTmp().toString(),
                        source.toString().substring(sourceDirectory.toString().length())
                );
                try {
                    boolean shouldCopy = BackupService.EXCLUSIONS
                            .stream()
                            .noneMatch(exclusion -> exclusion.equals(destination.getFileName().toString()));
                    shouldCopy &= BackupService.EXCLUSIONS
                            .stream()
                            .noneMatch(exclusion -> source.toString().contains(exclusion));
                    if (shouldCopy) {
                        Files.copy(source, destination);
                        taskService.updateTaskProgress(Double.valueOf(5 * Math.pow(10, 5) + (counter.incrementAndGet())).longValue());
                        logger.info("[{}] has been copied.", source);
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            });
        }
    }

    public static void restoreBackup() {
        if (Files.exists(gipterTmp())) {
            logger.info("Backup available under [{}]. Trying to restore files.", gipterTmp());
            try (Stream<Path> paths = Files.walk(gipterTmp())) {
                paths.forEach(source -> {
                    Path destination = Paths.get(
                                    ".",
                                    source.toString().substring(gipterTmp().toString().length()))
                            .toAbsolutePath().normalize();
                    boolean shouldCopy = !source.getFileName().toString().endsWith(".exe");
                    shouldCopy &= !source.getFileName().toString().endsWith(".jar");
                    shouldCopy &= !source.getFileName().toString().endsWith(".msi");
                    shouldCopy &= !Files.exists(destination);
                    if (shouldCopy) {
                        try {
                            Files.copy(
                                    source,
                                    destination,
                                    StandardCopyOption.REPLACE_EXISTING,
                                    StandardCopyOption.COPY_ATTRIBUTES
                            );

                            logger.info("[{}] restored to [{}].", source, destination);
                        } catch (IOException e) {
                            logger.error("Could not restore [{}]: {}", source, e.getMessage());
                        }
                    } else {
                        logger.info("[{}] already present in home directory or filtered out.", source.getFileName().toString());
                    }
                });
            } catch (IOException io) {
                logger.error("Problem with accessing backup [{}]. {}", gipterTmp(), io.getMessage());
            } finally {
                try {
                    try (Stream<Path> paths = Files.walk(gipterTmp())) {
                        paths.sorted(Comparator.reverseOrder())
                                .map(Path::toFile)
                                .forEach(File::delete);
                    }
                    logger.info("Backup folder deleted: [{}].", !Files.exists(gipterTmp()));
                } catch (IOException e) {
                    logger.warn("Can not delete backup folder [{}]: {}", gipterTmp(), e.getMessage());
                }
            }
        }
    }
}
