package pg.gipter.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class UpgradeServiceTest {

    @Test
    @Disabled
    void whenUpgradeVersion_thenDoSth() {
        UpgradeService upgradeService = new UpgradeService();

        upgradeService.upgradeAndRestartApplication();
    }
}