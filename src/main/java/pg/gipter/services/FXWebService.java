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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.w3c.dom.html.HTMLInputElement;
import pg.gipter.FlowType;
import pg.gipter.core.ArgName;
import pg.gipter.ui.alerts.AlertWindowBuilder;
import pg.gipter.ui.alerts.ImageFile;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.SystemUtils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class FXWebService {

    private static final Logger logger = LoggerFactory.getLogger(FXWebService.class);
    private final Stage stage;
    private WebEngine webEngine;
    private FlowType flowType;

    public FXWebService() {
        this(new Stage());
    }

    public FXWebService(Stage stage) {
        this.stage = stage;
    }

    public void initSSO() {
        initSSO(FlowType.REGULAR);
    }

    public void initSSO(FlowType flowType) {
        this.flowType = flowType;
        String ssoUrl = ArgName.toolkitUserFolderUrl.defaultValue();
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
        stage.setOnCloseRequest(createOnCloseRequest(flowType));
        stage.setTitle(BundleUtils.getMsg("webview.title"));
        stage.show();

        CookiesService.loadCookies();

        webEngine.getLoadWorker().stateProperty().addListener(changeListener());
        webEngine.load(ssoUrl);
    }

    private EventHandler<WindowEvent> createOnCloseRequest(FlowType flowType) {
        return windowEvent -> runOnCloseActivity(flowType);
    }

    private void runOnCloseActivity(FlowType flowType) {
        if (flowType == FlowType.INIT) {
            logger.info("Webview opened from Main. Application is going to be restarted.");
            final List<String> restartArguments = Stream.of(
                    String.format("%s=%s", ArgName.useUI.name(), ArgName.useUI.defaultValue()),
                    String.format("%s=%s", ArgName.flowType.name(), FlowType.REGULAR)
            ).collect(toList());
            new RestartService().start(restartArguments);
        } else {
            logger.info("Webview opened regularly.");
        }
        logger.info("Webview was closed.");
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
                                .withHeaderText(BundleUtils.getMsg(this.flowType == FlowType.INIT ?
                                        "webview.cookies.restarted" : "webview.cookies.saved"))
                                .withAlertType(Alert.AlertType.INFORMATION)
                                .withImageFile(ImageFile.FINGER_UP_PNG)
                                .buildAndDisplayWindow();
                    } catch (Exception e) {
                        logger.error("Could not save cookies.", e);
                        new AlertWindowBuilder()
                                .withHeaderText(BundleUtils.getMsg("webview.cookies.error"))
                                .withAlertType(Alert.AlertType.ERROR)
                                .withImageFile(ImageFile.ERROR_CHICKEN_PNG)
                                .buildAndDisplayWindow();
                    } finally {
                        stage.close();
                        runOnCloseActivity(flowType);
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
                throw new IOException("Sorry, something went wrong with SSO. " + item.getNodeValue());
            }
        }
    }
}
