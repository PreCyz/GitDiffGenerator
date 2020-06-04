package pg.gipter.testfx;

import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.model.*;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.services.SecurityService;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public final class UITestUtils {

    private UITestUtils() {
    }

    public static Set<SharePointConfig> generateSharePointConfig(int size) {
        return Stream.generate(() -> {
            final SharePointConfig spc = new SharePointConfig(
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString(),
                    UUID.randomUUID().toString()
            );
            spc.setProject(UUID.randomUUID().toString());
            spc.setListNames(Stream.generate(() -> UUID.randomUUID().toString()).limit(size).collect(toSet()));
            return spc;
        })
                .limit(size)
                .collect(toSet());
    }

    public static void generateConfigurationWithSPC(int spcSize) {
        Configuration configuration = new Configuration(
                new ApplicationConfig(), new ToolkitConfig(), null, SecurityService.getInstance().generateCipherDetails()
        );
        configuration.setRunConfigs(Stream.of(
                new RunConfigBuilder()
                        .withConfigurationName("testConfiguration")
                        .withSharePointConfigs(generateSharePointConfig(spcSize))
                        .create()
        ).collect(toList()));
        DaoFactory.getCachedConfiguration().saveConfiguration(configuration);
    }

    public static void generateDefaultConfig() {
        Configuration configuration = new Configuration(
                new ApplicationConfig(), new ToolkitConfig(), null, SecurityService.getInstance().generateCipherDetails()
        );
        DaoFactory.getCachedConfiguration().saveConfiguration(configuration);
    }

    public static void generateSimpleConfigurations(int runConfigSize) {
        Configuration configuration = new Configuration(
                new ApplicationConfig(), new ToolkitConfig(), null, SecurityService.getInstance().generateCipherDetails()
        );
        configuration.setRunConfigs(
                Stream.generate(() -> new RunConfigBuilder()
                        .withConfigurationName(UUID.randomUUID().toString().substring(0, 8))
                        .withItemType(ItemType.SIMPLE)
                        .create()
                )
                        .limit(runConfigSize)
                        .collect(toList())
        );
        DaoFactory.getCachedConfiguration().saveConfiguration(configuration);
    }
}
