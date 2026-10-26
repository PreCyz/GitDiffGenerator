package pg.gipter.services;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pg.gipter.FlowType;
import pg.gipter.ui.alerts.AlertWindowBuilder;
import pg.gipter.ui.alerts.ImageFile;
import pg.gipter.utils.BundleUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/** JavaFX status/continuation facade. Authentication itself runs in the Windows WebView2 host. */
public class FXWebService {
    private static final AtomicReference<CompletableFuture<Boolean>> CURRENT = new AtomicReference<>();
    private final Stage stage;
    private Label expiryLabel;
    private Runnable onAuthenticated;

    public FXWebService() {
        this(new Stage());
    }

    public FXWebService(Stage stage) {
        this.stage = stage;
    }

    public FXWebService onAuthenticated(Runnable action) {
        this.onAuthenticated = action;
        return this;
    }

    public static boolean isRunning() {
        return CURRENT.get() != null;
    }

    public void initSSO() {
        initSSO(FlowType.REGULAR);
    }

    public void initSSO(Label label) {
        expiryLabel = label;
        initSSO(FlowType.REGULAR);
    }

    /*public FXWebService initMinimizedSSO(FlowType flowType) {
        initSSO(flowType);
        return this;
    }*/

    public void initSSO(FlowType flowType) {
        authenticate(flowType);
    }

    public static CompletableFuture<Boolean> requestAuthentication(FlowType flowType) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        Runnable start = () -> new FXWebService().authenticate(flowType).whenComplete((success, error) -> {
            if (error != null) result.completeExceptionally(error);
            else result.complete(success);
        });
        if (Platform.isFxApplicationThread()) start.run();
        else Platform.runLater(start);
        return result;
    }

    public CompletableFuture<Boolean> authenticate(FlowType flowType) {
        if (!Platform.isFxApplicationThread())
            throw new IllegalStateException("Sign-in UI must start on the JavaFX thread.");
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        if (!CURRENT.compareAndSet(null, result)) {
            CompletableFuture<Boolean> existing = CURRENT.get();
            existing.thenAccept(success -> {
                if (success) {
                    if (expiryLabel != null)
                        expiryLabel.setText(BundleUtils.getMsg("toolkit.panel.cookieExpires", CookiesService.expiryDate()));
                    if (onAuthenticated != null) onAuthenticated.run();
                }
            });
            return existing;
        }
        Label status = new Label(BundleUtils.getMsg("webview2.signin.wait"));
        status.setWrapText(true);
        ProgressIndicator progress = new ProgressIndicator();
        AtomicReference<Thread> worker = new AtomicReference<>();
        Runnable cancelLogin = () -> {
            CookiesService.cancelAuthentication();
            Thread thread = worker.get();
            if (thread != null) thread.interrupt();
        };
        Button cancel = new Button(BundleUtils.getMsg("webview2.signin.cancel"));
        Button retry = new Button(BundleUtils.getMsg("webview2.signin.retry"));
        retry.setVisible(false);
        retry.setOnAction(_ -> authenticate(flowType));
        cancel.setOnAction(_ -> {
            status.setText(BundleUtils.getMsg("webview2.signin.cancelling"));
            cancelLogin.run();
        });
        VBox content = new VBox(14, status, progress, cancel, retry);
        content.setPadding(new Insets(24));
        stage.setTitle(BundleUtils.getMsg("webview.title"));
        stage.setScene(new Scene(content, 460, 240));
        stage.setOnCloseRequest(event -> {
            if (!result.isDone()) {
                event.consume();
                cancelLogin.run();
            }
        });
        stage.show();
        Thread vThread = Thread.ofVirtual().name("webview2-sign-in").unstarted(() -> {
            String failure = null;
            try {
                CookiesService.authenticate();
            } catch (Exception ex) {
                failure = ex.getMessage() == null ? "WebView2 sign-in failed." : ex.getMessage();
            }
            String error = failure;
            Platform.runLater(() -> {
                CURRENT.compareAndSet(result, null);
                progress.setVisible(false);
                if (error == null) {
                    if (expiryLabel != null)
                        expiryLabel.setText(BundleUtils.getMsg("toolkit.panel.cookieExpires", CookiesService.expiryDate()));
                    if (flowType != FlowType.JOB && flowType != FlowType.MISSED_JOB) {
                        new AlertWindowBuilder()
                                .withHeaderText(BundleUtils.getMsg(flowType == FlowType.INIT ?
                                        "webview.cookies.continuing" : "webview.cookies.saved"))
                                .withAlertType(Alert.AlertType.INFORMATION)
                                .withImageFile(ImageFile.FINGER_UP_PNG)
                                .buildAndDisplayWindow();
                    }
                    // INIT continues in this JVM so session-only cookies are not lost in a restart.
                    if (onAuthenticated != null) onAuthenticated.run();
                    result.complete(true);
                    if (flowType != FlowType.INIT) stage.close();
                } else {
                    status.setText(error);
                    retry.setVisible(true);
                    cancel.setText(BundleUtils.getMsg("webview2.signin.close"));
                    cancel.setOnAction(_ -> {
                        stage.close();
                        if (flowType == FlowType.INIT) Platform.exit();
                    });
                    result.complete(false);
                    new AlertWindowBuilder()
                            .withHeaderText(BundleUtils.getMsg("webview.cookies.error"))
                            .withMessage(error)
                            .withAlertType(Alert.AlertType.ERROR)
                            .withImageFile(ImageFile.ERROR_CHICKEN_PNG)
                            .buildAndDisplayWindow();
                }
            });
        });
        worker.set(vThread);
        vThread.start();
        return result;
    }
}
