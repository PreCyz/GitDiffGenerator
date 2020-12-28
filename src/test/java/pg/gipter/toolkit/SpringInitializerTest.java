package pg.gipter.toolkit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.*;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.ApplicationPropertiesFactory;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.dao.configuration.ConfigurationDao;
import pg.gipter.core.model.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class SpringInitializerTest {

    @BeforeEach
    void setup() {
        try {
            SpringInitializer.destroyContext();
            final ConfigurationDao configurationDao = DaoFactory.getCachedConfiguration();
            configurationDao.saveConfiguration(new Configuration());
            Files.deleteIfExists(Paths.get(DaoConstants.APPLICATION_PROPERTIES_JSON));
        } catch (IOException e) {
            System.out.println("There is something weird going on.");
        }
    }

    @Test
    void whenInitSpringApplicationContext_thenReturnApplicationContext() {
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{"toolkitUsername=userName", "toolkitPassword=password"}
        );

        ApplicationContext actual = SpringInitializer.getSpringContext(applicationProperties);

        assertThat(actual).isNotNull();
        assertThat(actual.getBeanDefinitionNames()).contains(
                "sharePointConfiguration",
                "httpComponentsMessageSender",
                "marshaller",
                "webServiceTemplate",
                "sharePointSoapClient"
        );
        Environment actualEnv = actual.getEnvironment();
        assertThat(actualEnv).isNotNull();
        assertThat(actualEnv.getProperty("toolkit.username")).isEqualTo("USERNAME");
        assertThat(actualEnv.getProperty("toolkit.password")).isEqualTo("password");
        assertThat(actualEnv.getProperty("toolkit.domain")).isEqualTo("NCDMZ");
        assertThat(actualEnv.getProperty("toolkit.WSUrl")).isEqualTo("https://goto.netcompany.com/cases/GTE106/NCSCOPY/_vti_bin/lists.asmx");
        assertThat(actualEnv.getProperty("toolkit.copyListName")).isEqualTo("WorkItems");
    }

    @Test
    void givenSpringEnvironment_whenSetToolkitProperties_thenAddPropertiesToEnvironment() {
        ApplicationProperties properties = ApplicationPropertiesFactory.getInstance(new String[]{
                "toolkitUsername=xxx",
                "toolkitPassword=pass",
        });
        ConfigurableEnvironment env = mock(ConfigurableEnvironment.class);
        MutablePropertySources mps = spy(new MutablePropertySources());
        when(env.getPropertySources()).thenReturn(mps);
        SpringInitializer.getSpringContext(properties);

        SpringInitializer.setToolkitProperties(env, properties);

        verify(env, times(1)).getPropertySources();
        verify(mps, times(1)).addLast(argThat(propertySource -> {
            assertThat(propertySource).isInstanceOf(PropertiesPropertySource.class);
            assertThat(propertySource.getName()).isEqualTo("toolkit");
            assertThat(propertySource.getProperty("toolkit.username")).isEqualTo("XXX");
            assertThat(propertySource.getProperty("toolkit.password")).isEqualTo("pass");
            assertThat(propertySource.getProperty("toolkit.domain")).isEqualTo("NCDMZ");
            assertThat(propertySource.getProperty("toolkit.WSUrl"))
                    .isEqualTo("https://goto.netcompany.com/cases/GTE106/NCSCOPY/_vti_bin/lists.asmx");
            assertThat(propertySource.getProperty("toolkit.copyListName")).isEqualTo("WorkItems");
            return true;
        }));
    }

}