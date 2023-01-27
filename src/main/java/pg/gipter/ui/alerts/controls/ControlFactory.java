package pg.gipter.ui.alerts.controls;

import pg.gipter.ui.UILauncher;

public final class ControlFactory {

    private ControlFactory() {}

    public static CustomControl createUpgradeButton(UILauncher uiLauncher) {
        return new ButtonControl(uiLauncher);
    }
}
