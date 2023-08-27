package pg.gipter.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.webkit.network.CookieManager;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.html.HTMLInputElement;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.utils.SystemUtils;

import java.lang.reflect.Field;
import java.net.CookieHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class FXWebService {

    private static final Logger logger = LoggerFactory.getLogger(FXWebService.class);
    private final ApplicationProperties applicationProperties;
    private WebEngine webEngine;

    public FXWebService(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public void initSSO() {
        logger.info("Initiating SSO");
        WebView webView = new WebView();

        webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);
        webEngine.setUserAgent(String.format("Mozilla/5.0 (%s; %s) Gipter-WebView %s",
                SystemUtils.osName(),
                SystemUtils.processorArchitecture(),
                applicationProperties.version().getVersion())
        );

        StackPane stackPane = new StackPane();
        stackPane.getChildren().add(webView);

        Stage stage = new Stage();
        stage.setScene(new Scene(stackPane, 600, 600));
        stage.show();

        //loadCookies();
        webEngine.setJavaScriptEnabled(true);
        webEngine.getLoadWorker().stateProperty().addListener(changeListener());
        webEngine.load(applicationProperties.toolkitUserFolder());
    }

    private ChangeListener<Worker.State> changeListener() {
        return (observable, oldValue, newValue) -> {
            logger.info("State {}", newValue);
            if (newValue == Worker.State.SUCCEEDED) {
                if (webEngine.getLocation().startsWith("https://goto.netcompany.com/_trust/default.aspx")) {
                    ((HTMLInputElement) webEngine.getDocument().getElementById("azure")).click();
                }
                logger.info(webEngine.getLocation());
                if (webEngine.getLocation().startsWith("https://login.microsoftonline.com/")) {
                    HTMLInputElement idSIButton9 = (HTMLInputElement) webEngine.getDocument().getElementById("idSIButton9");
                    Element emailInput = webEngine.getDocument().getElementById("i0116");
                    if (emailInput != null) {
                        emailInput.setAttribute("value", applicationProperties.toolkitUserEmail());
                        ((HTMLInputElement) emailInput).blur();
                        ((HTMLInputElement) emailInput).focus();
                        idSIButton9.click();
                    }

                    Element passwordInput = webEngine.getDocument().getElementById("i0118");
                    if (applicationProperties.isSSOPassword() && passwordInput != null) {
                        passwordInput.setAttribute("value", applicationProperties.toolkitSSOPassword());
                        ((HTMLInputElement) passwordInput).blur();
                        ((HTMLInputElement) passwordInput).focus();
                        idSIButton9.click();
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        logger.warn("Can not sleep.");
                    }
                }
                if (webEngine.getLocation().contains(applicationProperties.toolkitCopyCase())) {
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

            Files.write(CookiesService.COOKIES_PATH, json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            logger.error("Could not save cookies.", e);
        }
    }
}
