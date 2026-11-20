package pg.gipter.ui;

import org.junit.jupiter.api.Test;
import pg.gipter.services.GithubService;
import pg.gipter.services.platforms.AppManager;
import pg.gipter.services.platforms.AppManagerFactory;
import pg.gipter.utils.JarHelper;

import java.nio.file.Path;
import java.util.Optional;

import static org.mockito.Mockito.*;

class PortableExeUpgradeTest {

    @Test
    void upgradeOpensLatestReleaseAndKeepsTheApplicationWindowOpen() {
        UILauncher launcher = mock(UILauncher.class, CALLS_REAL_METHODS);
        AppManager manager = mock(AppManager.class);
        try (var source = mockStatic(JarHelper.class);
             var apps = mockStatic(AppManagerFactory.class)) {
            source.when(JarHelper::getJarPath).thenReturn(Optional.of(Path.of("Gipter.exe")));
            apps.when(AppManagerFactory::getInstance).thenReturn(manager);

            launcher.requestUpgrade();

            verify(manager).launchDefaultBrowser(GithubService.LATEST_RELEASE_URL);
            verify(launcher, never()).hideMainWindow();
        }
    }
}
