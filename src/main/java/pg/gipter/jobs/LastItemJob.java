package pg.gipter.jobs;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.services.ToolkitService;
import pg.gipter.ui.alerts.*;
import pg.gipter.utils.BundleUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class LastItemJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(LastItemJob.class);
    public static final String NAME = LastItemJob.class.getName();
    public static final String GROUP = NAME + "Group";

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("Executing check upgrade job {}.", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        checkLastUploadedItem(context.getMergedJobDataMap());
    }

    private void checkLastUploadedItem(JobDataMap jobDataMap) {
        final ApplicationProperties applicationProperties =
                (ApplicationProperties) jobDataMap.get(ApplicationProperties.class.getSimpleName());
        final ToolkitService toolkitService = new ToolkitService(applicationProperties);
        final Optional<String> lastItemUploadDate = toolkitService.lastItemUploadDate();
        if (lastItemUploadDate.isPresent()) {
            logger.info("New version available: {}.", lastItemUploadDate.get());
            LocalDateTime dateTime = LocalDateTime.parse(lastItemUploadDate.get(), DateTimeFormatter.ISO_DATE_TIME);

            if (dateTime.getMonthValue() < LocalDateTime.now().getMonthValue()) {
                Platform.runLater(() -> new AlertWindowBuilder()
                        .withHeaderText(BundleUtils.getMsg("popup.upgrade.message", lastItemUploadDate.get()))
                        .withWindowType(WindowType.BROWSER_WINDOW)
                        .withAlertType(Alert.AlertType.INFORMATION)
                        .withImage(ImageFile.randomFailImage())
                        .buildAndDisplayWindow()
                );
            }
        }
    }
}
