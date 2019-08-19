package pg.gipter.ui;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.launcher.Starter;
import pg.gipter.producer.DiffProducer;
import pg.gipter.producer.DiffProducerFactory;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.ApplicationPropertiesFactory;
import pg.gipter.toolkit.DiffUploader;
import pg.gipter.ui.alert.AlertWindowBuilder;
import pg.gipter.ui.alert.ImageFile;
import pg.gipter.ui.alert.WindowType;
import pg.gipter.utils.AlertHelper;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.PropertiesHelper;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Created by Pawel Gawedzki on 15-Jul-2019.
 */
public class FXMultiRunner extends Task<Void> implements Starter {

    private static class UploadResult {
        String configName;
        Boolean success;
        Throwable throwable;

        UploadResult(String configName, Boolean success, Throwable throwable) {
            this.configName = configName;
            this.success = success;
            this.throwable = throwable;
        }

        String logMsg() {
            String cause = "N/A";
            if (throwable != null) {
                cause = throwable.getMessage();
                cause = cause.substring(cause.lastIndexOf(":") + 1);
            }
            return String.format("configName: %s, success: %b, cause: %s", configName, success, cause);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(FXMultiRunner.class);
    private final LinkedList<String> configurationNames;
    private final Executor executor;
    private static Boolean toolkitCredentialsSet = null;
    private Map<String, UploadResult> resultMap = new LinkedHashMap<>();
    private long totalProgress;
    private AtomicLong workDone;
    private ApplicationProperties applicationProperties;

    public FXMultiRunner(Collection<String> configurationNames, Executor executor) {
        this.configurationNames = new LinkedList<>(configurationNames);
        this.executor = executor;
        this.totalProgress = configurationNames.size() * 5;
        this.workDone = new AtomicLong(0);
        this.applicationProperties = null;
    }

    public FXMultiRunner(ApplicationProperties applicationProperties, Executor executor) {
        this(Collections.emptyList(), executor);
        this.applicationProperties = applicationProperties;
        this.totalProgress = 5;
    }

    @Override
    public Void call() {
        start();
        return null;
    }

    public void start() {
        UploadStatus status;
        logger.info("{} started.", this.getClass().getName());
        if (configurationNames.isEmpty()) {
            logger.info("There is no configuration to launch.");
            AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder()
                    .withHeaderText(BundleUtils.getMsg("popup.error.messageWithLog"))
                    .withLink(AlertHelper.logsFolder())
                    .withWindowType(WindowType.LOG_WINDOW)
                    .withAlertType(Alert.AlertType.ERROR)
                    .withImage(ImageFile.ERROR_CHICKEN);
            Platform.runLater(alertWindowBuilder::buildAndDisplayWindow);
        } else {
            try {
                if (applicationProperties != null) {
                    executeForApplicationProperties();
                } else {
                    executeForNames();
                }
            } catch (Exception ex) {
                logger.error("Diff upload failure.", ex);
                resultMap.put(ex.getClass().getName(), new UploadResult(ex.getClass().getName(), Boolean.FALSE, ex));
            } finally {
                status = calculateFinalStatus();
                saveUploadStatus(status);
                displayAlertWindow(status);
            }
        }
    }

    private void executeForNames() throws InterruptedException, java.util.concurrent.ExecutionException {
        List<CompletableFuture<Boolean>> tasks = new LinkedList<>();
        for (String configName : configurationNames) {
            if (isToolkitCredentialsSet()) {
                CompletableFuture<Boolean> withUpload = CompletableFuture.supplyAsync(() -> getApplicationProperties(configName), executor)
                        .thenApply(this::produce).thenApply(this::upload)
                        .handle((isUploaded, throwable) -> handleUploadResult(configName, throwable == null, throwable));
                tasks.add(withUpload);
            } else {
                CompletableFuture<Boolean> withoutUpload = CompletableFuture.supplyAsync(() -> getApplicationProperties(configName), executor)
                        .thenApply(this::produce)
                        .handle((isUploaded, throwable) -> handleUploadResult(configName, Boolean.FALSE, new Throwable("Toolkit credentials not set.")));

                tasks.add(withoutUpload);
            }
        }
        CompletableFuture.allOf(tasks.toArray(new CompletableFuture<?>[0])).get();
    }

    private void executeForApplicationProperties() throws InterruptedException, java.util.concurrent.ExecutionException {
        List<CompletableFuture<Boolean>> tasks = new LinkedList<>();

        String configName = applicationProperties.configurationName();
        if (applicationProperties.isToolkitCredentialsSet()) {
                CompletableFuture<Boolean> withUpload = CompletableFuture.supplyAsync(() -> applicationProperties, executor)
                        .thenApply(this::produce).thenApply(this::upload)
                        .handle((isUploaded, throwable) -> handleUploadResult(configName, throwable == null, throwable));
                tasks.add(withUpload);
            } else {
                CompletableFuture<Boolean> withoutUpload = CompletableFuture.supplyAsync(() -> getApplicationProperties(configName), executor)
                        .thenApply(this::produce)
                        .handle((isUploaded, throwable) -> handleUploadResult(configName, Boolean.FALSE, new Throwable("Toolkit credentials not set.")));

                tasks.add(withoutUpload);
            }

        CompletableFuture.allOf(tasks.toArray(new CompletableFuture<?>[0])).get();
    }

    private boolean isConfirmationWindow() {
        return ApplicationPropertiesFactory.getInstance(
                new PropertiesHelper().loadArgumentArray(configurationNames.getFirst())
        ).isConfirmationWindow();
    }

    private String toolkitUserFolder() {
        return ApplicationPropertiesFactory.getInstance(
                new PropertiesHelper().loadArgumentArray(configurationNames.getFirst())
        ).toolkitUserFolder();
    }

    private boolean isToolkitCredentialsSet() {
        if (toolkitCredentialsSet == null) {
            toolkitCredentialsSet = ApplicationPropertiesFactory.getInstance(
                    new PropertiesHelper().loadArgumentArray(configurationNames.getFirst())
            ).isToolkitCredentialsSet();
        }
        return toolkitCredentialsSet;
    }

    private ApplicationProperties getApplicationProperties(String configurationName) {
        return ApplicationPropertiesFactory.getInstance(new PropertiesHelper().loadArgumentArray(configurationName));
    }

    private ApplicationProperties produce(ApplicationProperties applicationProperties) {
        try {
            DiffProducer diffProducer = DiffProducerFactory.getInstance(applicationProperties);
            updateProgress(workDone.incrementAndGet(), totalProgress);
            updateMessage(BundleUtils.getMsg("progress.generatingDiff"));
            diffProducer.produceDiff();
            updateProgress(workDone.incrementAndGet(), totalProgress);
            updateMessage(BundleUtils.getMsg("progress.diffGenerated"));
            return applicationProperties;
        } catch (Exception ex) {
            logger.info("Diff not generated.", ex);
            throw ex;
        }
    }

    private boolean upload(ApplicationProperties applicationProperties) {
        try {
            DiffUploader diffUploader = new DiffUploader(applicationProperties);
            updateProgress(workDone.incrementAndGet(), totalProgress);
            updateMessage(BundleUtils.getMsg("progress.uploadingToToolkit"));
            diffUploader.uploadDiff();
            updateProgress(workDone.incrementAndGet(), totalProgress);
            updateMessage(BundleUtils.getMsg("progress.itemUploaded"));
            logger.info("Diff upload complete.");
            return true;
        } catch (Exception ex) {
            logger.error("Diff upload failure.", ex);
            throw ex;
        }
    }

    private Boolean handleUploadResult(String configName, Boolean isUploaded, Throwable throwable) {
        if (isUploaded == null || !isUploaded) {
            logger.error("Diff upload for configuration name {} failed.", configName, throwable);
        } else {
            logger.info("Diff upload for configuration name {} uploaded.", configName);
        }
        resultMap.put(configName, new UploadResult(configName, isUploaded, throwable));
        return isUploaded;
    }

    private UploadStatus calculateFinalStatus() {
        UploadStatus status = UploadStatus.N_A;
        if (resultMap.entrySet().stream().allMatch(entry -> entry.getValue().success)) {
            status = UploadStatus.SUCCESS;
        }
        if (resultMap.entrySet().stream().anyMatch(entry -> !entry.getValue().success)) {
            status = UploadStatus.PARTIAL_SUCCESS;
        }
        if (resultMap.entrySet().stream().noneMatch(entry -> entry.getValue().success)) {
            status = UploadStatus.FAIL;
        }
        return status;
    }

    private void saveUploadStatus(UploadStatus status) {
        new PropertiesHelper().saveUploadStatus(status.name());
        updateProgress(totalProgress, totalProgress);
        updateMessage(BundleUtils.getMsg("progress.finished", status.name()));
        logger.info("{} ended.", this.getClass().getName());
    }

    private void displayAlertWindow(UploadStatus status) {
        if (!isConfirmationWindow()) {
            return;
        }
        AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder()
                .withHeaderText(BundleUtils.getMsg("popup.multiRunner." + status.name()));
        String detailedMessage;
        switch (status) {
            case N_A:
            case FAIL:
                detailedMessage = resultMap.values().stream().map(UploadResult::logMsg).collect(Collectors.joining("\n"));
                alertWindowBuilder
                        .withMessage(detailedMessage)
                        .withLink(AlertHelper.logsFolder())
                        .withWindowType(WindowType.LOG_WINDOW)
                        .withAlertType(Alert.AlertType.ERROR)
                        .withImage(ImageFile.ERROR_CHICKEN);
                break;
            case PARTIAL_SUCCESS:
                detailedMessage = resultMap.values().stream().map(UploadResult::logMsg).collect(Collectors.joining("\n"));
                alertWindowBuilder
                        .withMessage(detailedMessage)
                        .withLink(AlertHelper.logsFolder())
                        .withWindowType(WindowType.LOG_WINDOW)
                        .withAlertType(Alert.AlertType.WARNING)
                        .withImage(ImageFile.ALMOST_ALL);
                break;
            default:
                alertWindowBuilder
                        .withLink(toolkitUserFolder())
                        .withWindowType(WindowType.BROWSER_WINDOW)
                        .withAlertType(Alert.AlertType.INFORMATION)
                        .withImage(ImageFile.GOOD_JOB);

        }
        Platform.runLater(alertWindowBuilder::buildAndDisplayWindow);
    }
}
