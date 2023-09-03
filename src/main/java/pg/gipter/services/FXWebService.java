package pg.gipter.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.webkit.network.CookieManager;
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
import org.w3c.dom.Element;
import org.w3c.dom.html.HTMLInputElement;
import pg.gipter.InitSource;
import pg.gipter.core.ArgName;
import pg.gipter.jobs.UploadItemJob;
import pg.gipter.ui.alerts.AlertWindowBuilder;
import pg.gipter.ui.alerts.WebViewService;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.JarHelper;
import pg.gipter.utils.SystemUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.CookieHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FXWebService {

    private static final Logger logger = LoggerFactory.getLogger(FXWebService.class);
    private final ExecutorService executorService;
    private final Stage stage;
    private final JobDataMap jobDataMap;
    private WebEngine webEngine;
    private InitSource initSource;

    public FXWebService() {
        this(new Stage());
    }

    public FXWebService(JobDataMap jobDataMap) {
        this(new Stage(), jobDataMap);
    }

    public FXWebService(Stage stage) {
        this(stage, new JobDataMap());
    }

    private FXWebService(Stage stage, JobDataMap jobDataMap) {
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.stage = stage;
        this.jobDataMap = jobDataMap;
    }

    public void initSSO() {
        initSSO(InitSource.REGULAR);
    }

    public void initSSO(InitSource initSource) {
        this.initSource = initSource;
        WebViewService.getInstance();
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
        stage.show();

        CookiesService.loadCookies();

        webEngine.setJavaScriptEnabled(true);
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

        if (jarPath.isEmpty()) {
            logger.error("Error when restarting application. Could not file jar file.");
            System.exit(-1);
        }
        if ("DEV".equals(System.getenv().get("PROGRAM-PROFILE"))) {
            final String classesPath = jarPath.get().toAbsolutePath().toString();
            jarPath = Optional.of(classesPath.replaceFirst("classes", "Gipter.jar"))
                    .map(Paths::get);
        }

        if (!Files.exists(jarPath.get()) || !Files.isRegularFile(jarPath.get())) {
            logger.error("Error when restarting application. [{}] is not a file.", jarPath.get().toAbsolutePath());
            System.exit(-2);
        }

        final LinkedList<String> command = Stream.of(
                javaHome, "-jar",
                jarPath.get().toAbsolutePath().toString(),
                ArgName.useUI.name() + "=Y"
        ).collect(Collectors.toCollection(LinkedList::new));

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
                        saveCookies();
                        new AlertWindowBuilder()
                                .withHeaderText(BundleUtils.getMsg("webview.cookies.saved"))
                                .withAlertType(Alert.AlertType.INFORMATION)
                                .withWebViewDetails(WebViewService.getInstance().pullSuccessWebView())
                                .buildAndDisplayWindow();
                    } catch (IOException | NoSuchFieldException | ClassNotFoundException | IllegalAccessException e) {
                        logger.error("Could not save cookies.", e);
                        new AlertWindowBuilder()
                                .withHeaderText(BundleUtils.getMsg("webview.cookies.error"))
                                .withAlertType(Alert.AlertType.ERROR)
                                .withWebViewDetails(WebViewService.getInstance().pullFailWebView())
                                .buildAndDisplayWindow();
                    } finally {
                        stage.close();
                        runOnCloseActivity(initSource);
                    }
                }
            }
        };
    }

    @SuppressWarnings({"rawtypes"})
    private void saveCookies() throws IOException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        CookieManager cookieManager = (CookieManager) CookieHandler.getDefault();
        Field f = cookieManager.getClass().getDeclaredField("store");
        f.setAccessible(true);
        Object cookieStore = f.get(cookieManager);

        Field bucketsField = Class.forName("com.sun.webkit.network.CookieStore").getDeclaredField("buckets");
        bucketsField.setAccessible(true);
        Map buckets = (Map) bucketsField.get(cookieStore);
        f.setAccessible(true);
        Map<String, Collection> cookiesToSave = new LinkedHashMap<>();
        for (Object o : buckets.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            String domain = (String) entry.getKey();
            Map cookies = (Map) entry.getValue();
            cookiesToSave.put(domain, cookies.values());
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(cookiesToSave);

        if (!json.isEmpty() && !"{}".equals(json)) {
            Files.write(CookiesService.COOKIES_PATH, json.getBytes(StandardCharsets.UTF_8));
        }
        logger.info("Cookies saved in [{}]", CookiesService.COOKIES_PATH);
    }
}
