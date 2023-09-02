package pg.gipter.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.webkit.network.CookieManager;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.html.HTMLInputElement;
import pg.gipter.core.ArgName;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FXWebService {

    private static final Logger logger = LoggerFactory.getLogger(FXWebService.class);
    private WebEngine webEngine;

    public FXWebService() {}

    public void initSSO() {
        initSSO(false);
    }

    public void initSSO(boolean isMain) {
        logger.info("Initiating SSO");
        WebView webView = new WebView();

        webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);
        webEngine.setUserAgent(String.format("Mozilla/5.0 (%s; %s) Gipter-WebView",
                SystemUtils.osName(),
                SystemUtils.processorArchitecture())
        );

        StackPane stackPane = new StackPane();
        stackPane.getChildren().add(webView);

        Stage stage = new Stage();
        stage.setScene(new Scene(stackPane, 600, 600));
        stage.setOnCloseRequest(getOnCloseRequest(isMain));
        stage.show();

        CookiesService.loadCookies();
        webEngine.setJavaScriptEnabled(true);
        webEngine.getLoadWorker().stateProperty().addListener(changeListener());
        String ssoUrl = ArgName.toolkitUserFolder.defaultValue();
        logger.info("SSO launched for the [{}]", ssoUrl);
        webEngine.load(ArgName.toolkitUserFolder.defaultValue());
    }

    private static EventHandler<WindowEvent> getOnCloseRequest(boolean isMain) {
        return windowEvent -> {
            if (isMain) {
                logger.info("Webview opened from Main so main is going to be executed again.");
                try {
                    restartApplication();
                } catch (IOException ex) {
                    logger.error("Could not restart application. Shutting it down.");
                    System.exit(0);
                }
            } else {
                logger.info("Webview was closed.");
            }
        };
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
                        emailInput.setAttribute("value", ArgName.toolkitUsername.defaultValue() + "@netcompany.com");
                        ((HTMLInputElement) emailInput).blur();
                        ((HTMLInputElement) emailInput).focus();
                        idSIButton9.click();
                    }
                }
                if (webEngine.getLocation().contains(ArgName.toolkitCopyCase.defaultValue())) {
                    saveCookies();
                }
            }
        };
    }

    @SuppressWarnings({"rawtypes"})
    private void saveCookies() {
        try {
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
        } catch (Exception e) {
            logger.error("Could not save cookies.", e);
        }
    }

    private static void restartApplication() throws IOException {
        final String javaHome = Paths.get(SystemUtils.javaHome(), "bin", "java").toString();
        Optional<Path> jarPath = JarHelper.getJarPath();

        if (jarPath.isEmpty()) {
            logger.error("Error when restarting application. Could not file jar file.");
            return;
        }
        if ("DEV".equals(System.getenv().get("PROGRAM-PROFILE"))) {
            final String jarFilePath = jarPath.get().toAbsolutePath().toString();
            jarPath = Optional.of(jarFilePath.replaceFirst("classes", "Gipter.jar"))
                    .map(Paths::get);
        }

        if (!Files.exists(jarPath.get()) || !Files.isRegularFile(jarPath.get())) {
            logger.error("Error when restarting application. [{}] is not a file.", jarPath.get().toAbsolutePath());
            throw new IOException();
        }

        final LinkedList<String> command = Stream.of(
                javaHome, "-jar",
                jarPath.get().toAbsolutePath().toString(),
                ArgName.useUI.name() + "=Y"
        ).collect(Collectors.toCollection(LinkedList::new));

        new ProcessBuilder(command).start();

        System.exit(0);
    }
}
