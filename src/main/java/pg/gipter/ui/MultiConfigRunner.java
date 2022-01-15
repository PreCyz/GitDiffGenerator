package pg.gipter.ui;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.ApplicationPropertiesFactory;
import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.dao.configuration.ConfigurationDao;
import pg.gipter.core.dao.data.DataDao;
import pg.gipter.core.producers.DiffProducer;
import pg.gipter.core.producers.DiffProducerFactory;
import pg.gipter.launchers.Starter;
import pg.gipter.statistics.ExceptionDetails;
import pg.gipter.statistics.dto.RunDetails;
import pg.gipter.statistics.services.StatisticService;
import pg.gipter.toolkit.DiffUploader;
import pg.gipter.ui.alerts.*;
import pg.gipter.ui.task.UpdatableTask;
import pg.gipter.utils.BundleUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toCollection;

/** Created by Pawel Gawedzki on 15-Jul-2019. */
public class MultiConfigRunner extends UpdatableTask<Void> implements Starter {

    private static final Logger logger = LoggerFactory.getLogger(MultiConfigRunner.class);

    private final LinkedList<String> configurationNames;
    private final Executor executor;
    private static Boolean toolkitCredentialsSet = null;
    private final Map<String, UploadResult> resultMap = new LinkedHashMap<>();
    private Collection<ApplicationProperties> applicationPropertiesCollection;
    private final ConfigurationDao configurationDao;
    private final DataDao dataDao;
    private final RunType runType;
    private final LocalDate startDate;
    private WebViewService webViewService;

    public MultiConfigRunner(Set<String> configurationNames, Executor executor, RunType runType) {
        this(configurationNames, executor, runType, null);
    }

    public MultiConfigRunner(Set<String> configurationNames, Executor executor, RunType runType, LocalDate startDate) {
        super();
        this.configurationNames = new LinkedList<>(configurationNames);
        this.executor = executor;
        this.applicationPropertiesCollection = Collections.emptyList();
        this.configurationDao = DaoFactory.getCachedConfiguration();
        this.dataDao = DaoFactory.getDataDao();
        this.runType = runType;
        this.startDate = startDate;
    }

    public MultiConfigRunner(Collection<ApplicationProperties> applicationPropertiesCollection, Executor executor, RunType runType) {
        this(
                applicationPropertiesCollection.stream()
                        .map(ApplicationProperties::configurationName)
                        .collect(toCollection(LinkedHashSet::new)),
                executor,
                runType
        );
        this.applicationPropertiesCollection = applicationPropertiesCollection;
    }

    @Override
    public Void call() {
        start();
        return null;
    }

    @Override
    public void start() {
        webViewService = WebViewService.getInstance();
        UploadStatus status;
        logger.info("{} started.", this.getClass().getName());
        if (configurationNames.isEmpty()) {
            logger.info("There is no configuration to launch.");
            AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder()
                    .withMessage(BundleUtils.getMsg("popup.error.messageWithLog"))
                    .withLinkAction(new LogLinkAction())
                    .withAlertType(Alert.AlertType.ERROR)
                    .withWebViewDetails(WebViewService.getInstance().pullFailWebView());
            Platform.runLater(alertWindowBuilder::buildAndDisplayWindow);
        } else {
            try {
                if (applicationPropertiesCollection.isEmpty()) {
                    executeForNames();
                } else {
                    int totalNumberOfProjects = applicationPropertiesCollection.stream()
                            .mapToInt(ap -> ap.projectPaths().size())
                            .sum();
                    setMaxProgress(INCREMENT_FACTOR * totalNumberOfProjects + NUMBER_OF_STEPS);
                    executeForApplicationProperties();
                }
            } catch (Exception ex) {
                logger.error("Diff upload failure.", ex);
                resultMap.put(ex.getClass().getName(), new UploadResult(ex.getClass().getName(), Boolean.FALSE, ex));
            } finally {
                status = calculateFinalStatus();
                saveUploadStatus(status);
                final List<ExceptionDetails> exceptionDetails = calculateErrorDetails();
                updateStatistics(status, exceptionDetails);
                displayAlertWindow(status);
            }
        }
    }

    private void executeForNames() throws InterruptedException, ExecutionException {
        List<CompletableFuture<Boolean>> tasks = new LinkedList<>();
        for (String configName : configurationNames) {
            if (isToolkitCredentialsSet()) {
                CompletableFuture<Boolean> withUpload = CompletableFuture
                        .supplyAsync(() -> getApplicationProperties(configName), executor)
                        .thenApply(this::produce)
                        .thenApply(this::upload)
                        .handle((isUploaded, throwable) -> handleUploadResult(
                                configName, throwable == null, throwable)
                        );

                tasks.add(withUpload);
            } else {
                CompletableFuture<Boolean> withoutUpload = CompletableFuture
                        .supplyAsync(() -> getApplicationProperties(configName), executor)
                        .thenApply(this::produce)
                        .handle((isUploaded, throwable) -> handleUploadResult(
                                configName, Boolean.FALSE, new Throwable("Toolkit credentials not set.")
                        ));

                tasks.add(withoutUpload);
            }
        }
        CompletableFuture.allOf(tasks.toArray(new CompletableFuture<?>[0])).get();
    }

    private void executeForApplicationProperties() throws InterruptedException, ExecutionException {
        List<CompletableFuture<Boolean>> tasks = new LinkedList<>();

        for (ApplicationProperties applicationProperties : applicationPropertiesCollection) {
            String configName = applicationProperties.configurationName();
            if (applicationProperties.isToolkitCredentialsSet()) {
                CompletableFuture<Boolean> withUpload = CompletableFuture
                        .supplyAsync(() -> applicationProperties, executor)
                        .thenApply(this::produce)
                        .thenApply(this::upload)
                        .handle((isUploaded, throwable) -> handleUploadResult(configName, throwable == null, throwable));

                tasks.add(withUpload);
            } else {
                CompletableFuture<Boolean> withoutUpload = CompletableFuture
                        .supplyAsync(() -> getApplicationProperties(configName), executor)
                        .thenApply(this::produce)
                        .handle((isUploaded, throwable) -> handleUploadResult(
                                configName, Boolean.FALSE, new Throwable("Toolkit credentials not set.")
                        ));

                tasks.add(withoutUpload);
            }
            CompletableFuture.allOf(tasks.toArray(new CompletableFuture<?>[0])).get();
        }
    }

    private boolean isConfirmationWindow() {
        return ApplicationPropertiesFactory.getInstance(
                configurationDao.loadArgumentArray(configurationNames.getFirst())
        ).isConfirmationWindow();
    }

    private String toolkitUserFolder() {
        return ApplicationPropertiesFactory.getInstance(configurationDao.loadArgumentArray(configurationNames.getFirst()))
                .toolkitUserFolder();
    }

    private boolean isToolkitCredentialsSet() {
        if (toolkitCredentialsSet == null) {
            toolkitCredentialsSet = ApplicationPropertiesFactory.getInstance(
                    configurationDao.loadArgumentArray(configurationNames.getFirst())
            ).isToolkitCredentialsSet();
        }
        return toolkitCredentialsSet;
    }

    private ApplicationProperties getApplicationProperties(String configurationName) {
        final ApplicationProperties instance = ApplicationPropertiesFactory.getInstance(configurationDao.loadArgumentArray(configurationName));
        if (startDate != null) {
            instance.getRunConfigMap().values().forEach(rc -> rc.setStartDate(startDate));
        }
        return instance;
    }

    private ApplicationProperties produce(ApplicationProperties applicationProperties) {
        try {
            DiffProducer diffProducer = DiffProducerFactory.getInstance(applicationProperties);
            incrementProgress();
            updateMessage(BundleUtils.getMsg("progress.generatingDiff"));
            diffProducer.produceDiff(this);
            incrementProgress();
            updateMessage(BundleUtils.getMsg("progress.diffGenerated"));
            return applicationProperties;
        } catch (Exception ex) {
            logger.info("Diff not generated.", ex);
            throw ex;
        }
    }

    private boolean upload(ApplicationProperties applicationProperties) {
        try {
            if (applicationProperties.isUploadItem()) {
                DiffUploader diffUploader = new DiffUploader(applicationProperties);
                incrementProgress();
                updateMessage(BundleUtils.getMsg("progress.uploadingToToolkit"));
                diffUploader.uploadDiff();
                incrementProgress();
                updateMessage(BundleUtils.getMsg("progress.itemUploaded"));
                logger.info("Diff upload complete.");
                return true;
            } else {
                throw new IllegalArgumentException("Item upload switched off.");
            }
        } catch (Exception ex) {
            logger.error("Diff upload failure.", ex);
            throw ex;
        }
    }

    private Boolean handleUploadResult(String configName, Boolean isUploaded, Throwable throwable) {
        if (isUploaded == null || !isUploaded) {
            logger.error("Diff upload for configuration name [{}] failed.", configName, throwable);
        } else {
            logger.info("Diff upload for configuration name [{}] uploaded.", configName);
        }
        resultMap.put(configName, new UploadResult(configName, isUploaded, throwable));
        return isUploaded;
    }

    private void updateStatistics(final UploadStatus status, final List<ExceptionDetails> exceptionDetails) {
        executor.execute(() -> {
            StatisticService statisticService = new StatisticService();
            LinkedList<ApplicationProperties> appProps = new LinkedList<>(applicationPropertiesCollection);
            if (appProps.isEmpty()) {
                for (String configName : configurationNames) {
                    appProps.add(ApplicationPropertiesFactory.getInstance(configurationDao.loadArgumentArray(configName)));
                }
            }
            statisticService.updateStatistics(new RunDetails(appProps, status, runType, exceptionDetails));
        });
    }

    private UploadStatus calculateFinalStatus() {
        UploadStatus status = UploadStatus.N_A;
        if (resultMap.entrySet().stream().allMatch(entry -> entry.getValue().getSuccess())) {
            status = UploadStatus.SUCCESS;
        }
        if (resultMap.entrySet().stream().anyMatch(entry -> !entry.getValue().getSuccess())) {
            status = UploadStatus.PARTIAL_SUCCESS;
        }
        if (resultMap.entrySet().stream().noneMatch(entry -> entry.getValue().getSuccess())) {
            status = UploadStatus.FAIL;
        }
        return status;
    }

    private List<ExceptionDetails> calculateErrorDetails() {
        return resultMap.values()
                .stream()
                .filter(uploadResult -> !uploadResult.getSuccess())
                .map(uploadResult -> new ExceptionDetails(
                        uploadResult.logMsg(),
                        Optional.ofNullable(uploadResult.getCause())
                                .map(Throwable::getMessage)
                                .orElseGet(uploadResult::getThrowableMsg),
                        LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                )
                .collect(Collectors.toList());
    }

    private void saveUploadStatus(UploadStatus status) {
        dataDao.saveUploadStatus(status);
        updateProgress(getMax(), getMax());
        updateMessage(BundleUtils.getMsg("progress.finished", status.name()));
        logger.info("{} ended.", this.getClass().getName());
    }

    private void displayAlertWindow(final UploadStatus status) {
        if (!isConfirmationWindow()) {
            return;
        }

        AlertWindowBuilder alertWindowBuilder = new AlertWindowBuilder()
                .withHeaderText(BundleUtils.getMsg("popup.multiRunner." + status.name()))
                .withWebViewDetails(webViewService.pullWebView(status));
        switch (status) {
            case N_A:
            case FAIL:
                alertWindowBuilder
                        .withUploadResultMap(resultMap)
                        .withLinkAction(new LogLinkAction())
                        .withAlertType(Alert.AlertType.ERROR);
                break;
            case PARTIAL_SUCCESS:
                alertWindowBuilder
                        .withUploadResultMap(resultMap)
                        .withLinkAction(new LogLinkAction(), new BrowserLinkAction(toolkitUserFolder()))
                        .withAlertType(Alert.AlertType.WARNING);
                break;
            default:
                alertWindowBuilder
                        .withLinkAction(new BrowserLinkAction(toolkitUserFolder()))
                        .withAlertType(Alert.AlertType.INFORMATION);

        }
        Platform.runLater(alertWindowBuilder::buildAndDisplayWindow);
    }
}
