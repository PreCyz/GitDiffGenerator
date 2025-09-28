package pg.gipter.services.upgrade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.services.TaskService;
import pg.gipter.utils.SystemUtils;

import java.nio.file.*;
import java.util.Set;

public final class BackupService {
    private static final Logger logger = LoggerFactory.getLogger(BackupService.class);

    private static final Set<String> TO_BACKUP = Set.of(
            "settings.json", "cookies.json", "db.connection", "gif.json", "applicationProperties.json",
            "data.json", "logs"
    );

    private BackupService() {}

    public static void backupAppFiles(TaskService<Void> taskService) throws Exception {
        Path gipterTmp = Files.createDirectory(Paths.get(SystemUtils.tmp(), "Gipter"));

        int counter = 0;

        for (String file : TO_BACKUP) {
            Path source = Paths.get(SystemUtils.javaHome(), file);
            Files.copy(source,
                    Paths.get(gipterTmp.normalize().toString(), file),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.COPY_ATTRIBUTES
            );
            taskService.updateTaskProgress(Double.valueOf(5 * Math.pow(10, 5) + (counter++)).longValue());
            logger.info("[{}] has been copied.", file);
        }
    }

    public static void restoreBackup() {
        try {
            Path destination = Paths.get(SystemUtils.javaHome());

            for (String file : TO_BACKUP) {
                Files.copy(
                        Paths.get(SystemUtils.tmp(), "Gipter", file),
                        destination,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.COPY_ATTRIBUTES
                );
                logger.info("[{}] has been restored.", file);
            }
        } catch (Exception e) {
            logger.error("Can not restore backup: {}", e.getMessage());
        }
    }
}
