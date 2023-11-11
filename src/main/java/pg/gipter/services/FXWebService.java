package pg.gipter.services;

import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLInputElement;
import pg.gipter.InitSource;
import pg.gipter.core.ArgName;
import pg.gipter.jobs.UploadItemJob;
import pg.gipter.ui.alerts.AlertWindowBuilder;
import pg.gipter.ui.alerts.ImageFile;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.JarHelper;
import pg.gipter.utils.SystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

public class FXWebService {

    private static final Logger logger = LoggerFactory.getLogger(FXWebService.class);
    private final ExecutorService executorService;
    private final Stage stage;
    private final Map<String, ?> jobDataMap;
    private WebEngine webEngine;
    private InitSource initSource;

    public FXWebService() {
        this(new Stage());
    }

    public FXWebService(JobDataMap jobDataMap) {
        this(new Stage(), jobDataMap);
    }

    public FXWebService(Stage stage) {
        this(stage, Collections.emptyMap());
    }

    private FXWebService(Stage stage, Map<String, ?> jobDataMap) {
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.stage = stage;
        this.jobDataMap = jobDataMap;
    }

    public void initSSO() {
        initSSO(InitSource.REGULAR);
    }

    public void initSSO(InitSource initSource) {
        this.initSource = initSource;
        String ssoUrl = ArgName.toolkitUserFolder.defaultValue();
        logger.info("Launching SSO for [{}]", ssoUrl);

        WebView webView = new WebView();
        webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);
        webEngine.setUserAgent(String.format("Mozilla/5.0 (%s; %s) Gipter-WebView",
                SystemUtils.osName(),
                SystemUtils.processorArchitecture())
        );

        StackPane stackPane = new StackPane();
        stackPane.getChildren().add(webView);

        stage.setScene(new Scene(stackPane, 600, 600));
        stage.setOnCloseRequest(createOnCloseRequest(initSource));
        stage.setTitle(BundleUtils.getMsg("webview.title"));
        stage.show();

        CookiesService.loadCookies();

        webEngine.getLoadWorker().stateProperty().addListener(changeListener());
        webEngine.load(ssoUrl);
    }

    private EventHandler<WindowEvent> createOnCloseRequest(InitSource initSource) {
        return windowEvent -> runOnCloseActivity(initSource);
    }

    private void runOnCloseActivity(InitSource initSource) {
        switch (initSource) {
            case MAIN:
                logger.info("Webview opened from Main. Application is going to be restarted.");
                restartApplication();
                break;
            case JOB:
                logger.info("Webview opened from JOB. Continuing job.");
                executorService.submit(this::continueJob, Void.class);
                break;
            default:
                logger.info("Webview opened regularly.");
        }
        logger.info("Webview was closed.");
    }

    private void restartApplication() {
        final String javaHome = Paths.get(SystemUtils.javaHome(), "bin", "java").toString();
        Optional<Path> jarPath = JarHelper.getJarPath();

        if (!jarPath.isPresent()) {
            logger.error("Error when restarting application. Could not file jar file.");
            System.exit(-1);
        }
        if ("DEV".equals(System.getenv().get("PROGRAM-PROFILE"))) {
            final String classesPath = jarPath.get().toAbsolutePath().toString();
            jarPath = Optional.of(classesPath.replaceFirst("classes", "Gipter.jar")).map(Paths::get);
        }

        if (!Files.exists(jarPath.get()) || !Files.isRegularFile(jarPath.get())) {
            logger.error("Error when restarting application. [{}] is not a file.", jarPath.get().toAbsolutePath());
            System.exit(-2);
        }

        final LinkedList<String> command = Stream.of(
                javaHome, "-jar",
                jarPath.get().toAbsolutePath().toString(),
                ArgName.useUI.name() + "=Y"
        ).collect(toCollection(LinkedList::new));

        try {
            new ProcessBuilder(command).start();
        } catch (IOException e) {
            logger.error("Could not restart application gracefully. Shutting it down.");
        }
        System.exit(0);
    }

    private void continueJob() {
        try {
            new UploadItemJob().runJob(jobDataMap);
        } catch (JobExecutionException e) {
            logger.error("Could not finish job", e);
        }
    }

    private ChangeListener<Worker.State> changeListener() {
        return (observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                if (webEngine.getLocation().startsWith("https://goto.netcompany.com/_trust/default.aspx")) {
                    ((HTMLInputElement) webEngine.getDocument().getElementById("azure")).click();
                }
                if (webEngine.getLocation().startsWith("https://login.microsoftonline.com/")) {
                    HTMLInputElement idSIButton9 = (HTMLInputElement) webEngine.getDocument().getElementById("idSIButton9");
                    Element emailInput = webEngine.getDocument().getElementById("i0116");
                    if (emailInput != null) {
                        emailInput.setAttribute("value", ArgName.toolkitUsername.defaultValue() + ArgName.emailDomain.defaultValue());
                        ((HTMLInputElement) emailInput).blur();
                        ((HTMLInputElement) emailInput).focus();
                        idSIButton9.click();
                    }
                }
                if (webEngine.getLocation().contains(ArgName.toolkitCopyCase.defaultValue())) {
                    try {
                        checkProblem(webEngine.getDocument());
                        CookiesService.extractAndSaveCookies();
                        new AlertWindowBuilder()
                                .withHeaderText(BundleUtils.getMsg(this.initSource == InitSource.MAIN ?
                                        "webview.cookies.restarted" : "webview.cookies.saved"))
                                .withAlertType(Alert.AlertType.INFORMATION)
                                .withImageFile(ImageFile.FINGER_UP_PNG)
                                .buildAndDisplayWindow();
                    } catch (IOException | NoSuchFieldException | ClassNotFoundException | IllegalAccessException e) {
                        logger.error("Could not save cookies.", e);
                        new AlertWindowBuilder()
                                .withHeaderText(BundleUtils.getMsg("webview.cookies.error"))
                                .withAlertType(Alert.AlertType.ERROR)
                                .withImageFile(ImageFile.ERROR_CHICKEN_PNG)
                                .buildAndDisplayWindow();
                    } finally {
                        stage.close();
                        runOnCloseActivity(initSource);
                    }
                }
            }
        };
    }

    private void checkProblem(Document document) throws IOException {
        NodeList childNodes = document.getChildNodes();
        checkSomethingWrong(childNodes);
    }

    private void checkSomethingWrong(NodeList childNodes) throws IOException {
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item.getChildNodes().getLength() > 0) {
                checkSomethingWrong(item.getChildNodes());
            }
            if (item.getNodeValue() != null && item.getNodeValue().contains("Sorry, something went wrong")) {
                throw new IOException("Sorry, something went wrong with SSO.");
            }
        }
    }
}
