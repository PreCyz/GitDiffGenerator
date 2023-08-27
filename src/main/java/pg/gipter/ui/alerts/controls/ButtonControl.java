package pg.gipter.ui.alerts.controls;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.ui.UILauncher;
import pg.gipter.utils.BundleUtils;
import pg.gipter.utils.ResourceUtils;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

public class ButtonControl implements CustomControl {

    public static final Logger logger = LoggerFactory.getLogger(ButtonControl.class);
    private final UILauncher uiLauncher;
    private final Button button;

    public ButtonControl(UILauncher uiLauncher) {
        this.uiLauncher = uiLauncher;
        this.button = new Button();
    }

    @Override
    public Control create(EventHandler<ActionEvent> actionEventHandler) {
        button.setText(BundleUtils.getMsg("main.menu.help.upgradeApplication"));
        button.setOnAction(actionEventHandler);
        button.setFont(Font.font("System", FontWeight.BOLD, FontPosture.REGULAR, 15));
        final String cssFile = "buttons.css";
        final String cssStyle = "green";
        Optional<URL> cssResource = ResourceUtils.getCssResource(cssFile);
        try {
            button.getStylesheets().add(cssResource.get().toURI().toString());
            button.getStyleClass().add(cssStyle);
        } catch (URISyntaxException e) {
            logger.error("Could not add style to button. File [{}], style [{}]", cssFile, cssStyle);
        }
        return button;
    }

    @Override
    public UILauncher getUiLauncher() {
        return uiLauncher;
    }
}
