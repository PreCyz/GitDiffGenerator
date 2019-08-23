package pg.gipter.service;

import org.junit.jupiter.api.Test;

class UpgradeServiceTest {

    @Test
    void whenUpgradeVersion_thenDoSth() {
        UpgradeService upgradeService = new UpgradeService();

        upgradeService.upgradeAndRestartApplication();
    }
}