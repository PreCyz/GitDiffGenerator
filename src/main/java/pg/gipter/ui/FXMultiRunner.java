package pg.gipter.ui;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.launcher.Starter;
import pg.gipter.producer.DiffProducer;
import pg.gipter.producer.DiffProducerFactory;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.settings.ApplicationPropertiesFactory;
import pg.gipter.toolkit.DiffUploader;
import pg.gipter.ui.alert.AlertWindowBuilder;
import pg.gipter.ui.alert.WindowType;
import pg.gipter.utils.AlertHelper;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.PropertiesHelper;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Created by Pawel Gawedzki on 15-Jul-2019.
 */
public class FXMultiRunner extends Task<Void> implements Starter {

    private enum Status {SUCCESS, PARTIAL_SUCCESS, FAIL, N_A}

    private static final Logger logger = LoggerFactory.getLogger(FXMultiRunner.class);
    private final LinkedList<String> configurationNames;
    private final Executor executor;
    private static Boolean toolkitCredentialsSet = null;
    private Map<String, Boolean> resultMap = new LinkedHashMap<>();

    public FXMultiRunner(Collection<String> configurationNames, Executor executor) {
        this.configurationNames = new LinkedList<>(configurationNames);
        this.executor = executor;
    }

    @Override
    public Void call() {
        start();
        return null;
    }

    public void start() {
        logger.info("{} started.", this.getClass().getName());
        if (configurationNames.isEmpty()) {
            logger.info("There is no configuration to launch.");
            Platform.runLater(() -> new AlertWindowBuilder()
                    .withHeaderText(BundleUtils.getMsg("popup.error.messageWithLog"))
                    .withLink(AlertHelper.logsFolder())
                    .withWindowType(WindowType.LOG_WINDOW)
                    .withAlertType(Alert.AlertType.ERROR)
                    .withImage()
                    .buildAndDisplayWindow()
            );
        } else {
            try {
                List<CompletableFuture<Boolean>> tasks = new LinkedList<>();
                for (String configName : configurationNames) {
                    if (isToolkitCredentialsSet()) {
                        CompletableFuture<Boolean> withUpload = CompletableFuture.supplyAsync(() -> getApplicationProperties(configName), executor)
                                .thenApply(this::produce).thenApply(this::upload)
                                .handle((isUploaded, throwable) -> handleUploadResult(configName, isUploaded, throwable));
                        tasks.add(withUpload);
                    } else {
                        CompletableFuture<Boolean> withoutUpload = CompletableFuture.supplyAsync(() -> getApplicationProperties(configName), executor)
                                .thenApply(this::produce)
                                .handle((s, t) -> Boolean.FALSE);

                        tasks.add(withoutUpload);
                    }
                }
                CompletableFuture.allOf(tasks.toArray(new CompletableFuture<?>[0])).get();
            } catch (Exception ex) {
                logger.error("Diff upload failure.", ex);
                Platform.runLater(() -> new AlertWindowBuilder()
                        .withHeaderText(BundleUtils.getMsg("popup.error.messageWithLog", ex.getMessage()))
                        .withLink(AlertHelper.logsFolder())
                        .withWindowType(WindowType.LOG_WINDOW)
                        .withAlertType(Alert.AlertType.ERROR)
                        .withImage()
                        .buildAndDisplayWindow()
                );
            } finally {
                Status status = Status.N_A;
                if (resultMap.entrySet().stream().allMatch(Map.Entry::getValue)) {
                    status = Status.SUCCESS;
                }
                if (resultMap.entrySet().stream().anyMatch(entry -> !entry.getValue())) {
                    status = Status.PARTIAL_SUCCESS;
                }
                if (resultMap.entrySet().stream().noneMatch(Map.Entry::getValue)) {
                    status = Status.FAIL;
                }
                new PropertiesHelper().saveUploadStatus(status.name());
                updateProgress(5, 5);
                updateMessage(BundleUtils.getMsg("progress.finished", status.name()));
                logger.info("{} ended.", this.getClass().getName());
            }
            if (!resultMap.entrySet().stream().allMatch(Map.Entry::getValue) && isConfirmationWindow()) {
                Platform.runLater(() -> new AlertWindowBuilder()
                        .withHeaderText(BundleUtils.getMsg("popup.confirmation.message"))
                        .withLink(toolkitUserFolder())
                        .withWindowType(WindowType.BROWSER_WINDOW)
                        .withAlertType(Alert.AlertType.INFORMATION)
                        .withImage()
                        .buildAndDisplayWindow()
                );
            }
        }
    }

    @NotNull
    private Boolean handleUploadResult(String configName, Boolean isUploaded, Throwable throwable) {
        if (!isUploaded) {
            logger.error("Diff upload failure.", throwable);
        } else {
            logger.info("Diff upload for configuration {} ended.", configName);
        }
        resultMap.put(configName, isUploaded);
        return isUploaded;
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
        DiffProducer diffProducer = DiffProducerFactory.getInstance(applicationProperties);
        updateProgress(1, 5);
        updateMessage(BundleUtils.getMsg("progress.generatingDiff"));
        diffProducer.produceDiff();
        updateProgress(2, 5);
        updateMessage(BundleUtils.getMsg("progress.diffGenerated"));
        return applicationProperties;
    }

    private ApplicationProperties checkToolkitCredentials(ApplicationProperties applicationProperties) {
        if (!applicationProperties.isToolkitCredentialsSet()) {
            String errorMessage = "Toolkit details not set. Check your settings.";
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        return applicationProperties;
    }

    private boolean upload(ApplicationProperties applicationProperties) {
        boolean done = true;
        try {
            DiffUploader diffUploader = new DiffUploader(applicationProperties);
            updateProgress(3, 5);
            updateMessage(BundleUtils.getMsg("progress.uploadingToToolkit"));
            diffUploader.uploadDiff();
            updateProgress(4, 5);
            updateMessage(BundleUtils.getMsg("progress.itemUploaded"));
            logger.info("Diff upload complete.");
        } catch (Exception ex) {
            logger.error("Diff upload failure.", ex);
            done = false;
        }
        return done;
    }
}
