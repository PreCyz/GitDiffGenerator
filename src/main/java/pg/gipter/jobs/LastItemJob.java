package pg.gipter.jobs;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.services.ToolkitService;
import pg.gipter.ui.alerts.AlertWindowBuilder;
import pg.gipter.ui.alerts.WebViewService;
import pg.gipter.utils.BundleUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class LastItemJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(LastItemJob.class);
    public static final String NAME = LastItemJob.class.getName();
    public static final String GROUP = NAME + "Group";

    @Override
    public void execute(JobExecutionContext context) {
        logger.info("Executing check upgrade job {}.", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        checkLastUploadedItem(context.getMergedJobDataMap());
    }

    private void checkLastUploadedItem(JobDataMap jobDataMap) {
        final ApplicationProperties applicationProperties =
                (ApplicationProperties) jobDataMap.get(ApplicationProperties.class.getSimpleName());
        final ToolkitService toolkitService = new ToolkitService(applicationProperties);
        Optional<String> userId = toolkitService.getUserId();
        final Optional<String> lastItemUploadDate = toolkitService.lastItemModifiedDate(userId.orElseGet(() -> ""));
        boolean shouldDisplayWindow;
        String msg;
        if (lastItemUploadDate.isPresent()) {
            msg = BundleUtils.getMsg("popup.job.missingItem", lastItemUploadDate.get());
            logger.info("Last item upload date is: {}.", lastItemUploadDate.get());
            LocalDateTime dateTime = LocalDateTime.parse(lastItemUploadDate.get(), DateTimeFormatter.ISO_DATE_TIME);
            shouldDisplayWindow = dateTime.getMonthValue() < LocalDateTime.now().getMonthValue();
        } else {
            msg = BundleUtils.getMsg("popup.job.noItem");
            shouldDisplayWindow = true;
        }

        if (shouldDisplayWindow) {
            final AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder()
                    .withHeaderText(msg)
                    .withAlertType(Alert.AlertType.INFORMATION)
                    .withWebViewDetails(WebViewService.getInstance().pullFailWebView());
            Platform.runLater(alertWindowBuilder::buildAndDisplayWindow);
        }
    }
}
