package pg.gipter.platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;

class WindowsAppManager implements AppManager {

    private static final Logger logger = LoggerFactory.getLogger(WindowsAppManager.class);

    @Override
    public void launchFileManagerForLogs() {
        try {
            LinkedList<String> command = new LinkedList<>();
            command.addFirst("explorer");
            command.add(".\\logs");
            new ProcessBuilder(command).start();
        } catch (IOException ex) {
            logger.warn("Can not open file manager.", ex);
        }
    }

    @Override
    public void launchDefaultBrowser(String url) {
        try {
            LinkedList<String> command = new LinkedList<>();
            command.addFirst("explorer");
            command.add(url);
            new ProcessBuilder(command).start();
        } catch (IOException ex) {
            logger.warn("Can not open browser.", ex);
        }
    }
}
