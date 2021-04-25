package pg.gipter.ui;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

public class ScreenService {

    public enum Resolution {
        S_1920x1080(1920, 1080),
        S_1680x1050(1680, 1050),
        S_1600x900(1600, 900),
        S_1280x1024(1280, 1024),
        S_1280x720(1280, 720),
        S_1024x768(1024, 768),
        S_800x600(800, 600);

        private final int width;
        private final int height;

        Resolution(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    void getScreenSize() {
        final Rectangle2D bounds = Screen.getPrimary().getBounds();
    }

    public double getPrimaryWidth() {
        return Screen.getPrimary().getBounds().getWidth();
    }

    public double getPrimaryHeight() {
        return Screen.getPrimary().getBounds().getHeight();
    }
}
