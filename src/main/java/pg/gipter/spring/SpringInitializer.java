package pg.gipter.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.toolkit.sharepoint.soap.SharePointConfiguration;

import java.util.Properties;

public final class SpringInitializer {

    private static ApplicationContext springContext;

    private SpringInitializer() { }

    public static ApplicationContext getSpringContext(ApplicationProperties applicationProperties) {
        return initSpringContext(applicationProperties);
    }

    private static ApplicationContext initSpringContext(ApplicationProperties applicationProperties) {
        if (springContext == null) {
            AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
            setToolkitProperties(applicationContext.getEnvironment(), applicationProperties);
            applicationContext.register(SharePointConfiguration.class);
            applicationContext.refresh();
            springContext = applicationContext;
        }
        return springContext;
    }

    static void setToolkitProperties(ConfigurableEnvironment environment, ApplicationProperties applicationProperties) {
        Properties toolkitProperties = new Properties();
        toolkitProperties.put("toolkit.username", applicationProperties.toolkitUsername());
        toolkitProperties.put("toolkit.password", applicationProperties.toolkitPassword());
        toolkitProperties.put("toolkit.domain", applicationProperties.toolkitDomain());
        toolkitProperties.put("toolkit.WSUrl", applicationProperties.toolkitWSUrl());
        toolkitProperties.put("toolkit.copyListName", applicationProperties.toolkitCopyListName());
        environment.getPropertySources().addLast(new PropertiesPropertySource("toolkit", toolkitProperties));
    }

    static void destroyContext() {
        springContext = null;
    }
}
