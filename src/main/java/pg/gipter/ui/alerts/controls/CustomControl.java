package pg.gipter.ui.alerts.controls;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Control;
import pg.gipter.ui.UILauncher;

public interface CustomControl {

    Control create(EventHandler<ActionEvent> actionEventHandler);
    UILauncher getUiLauncher();

}
