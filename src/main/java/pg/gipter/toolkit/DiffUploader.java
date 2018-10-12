package pg.gipter.toolkit;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.toolkit.configuration.SharePointClient;
import pg.gipter.toolkit.configuration.SharePointConfiguration;

import java.util.Properties;

/**Created by Pawel Gawedzki on 11-Oct-2018.*/
public class DiffUploader {

    private final ApplicationContext springContext;
    private final ApplicationProperties applicationProperties;

    public DiffUploader(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.springContext = initSpringApplicationContext();
    }

    private ApplicationContext initSpringApplicationContext() {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        environment.getPropertySources().addLast(toolkitProperties());
        applicationContext.register(SharePointConfiguration.class);
        applicationContext.refresh();
        return applicationContext;
    }

    private PropertiesPropertySource toolkitProperties() {
        Properties toolkitProperties = new Properties();
        toolkitProperties.put("toolkit.username", applicationProperties.toolkitUsername());
        toolkitProperties.put("toolkit.password", applicationProperties.toolkitPassword());
        toolkitProperties.put("toolkit.domain", applicationProperties.toolkitDomain());
        return new PropertiesPropertySource("toolkit", toolkitProperties);
    }

    public void uploadDiff() {
        try {
            SharePointClient sharePointClient = springContext.getBean(SharePointClient.class);
            System.out.println("Getting list from SharePoint.");
            sharePointClient.getList();
            System.out.println("Getting list and view from SharePoint.");
            sharePointClient.getListAndView();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
