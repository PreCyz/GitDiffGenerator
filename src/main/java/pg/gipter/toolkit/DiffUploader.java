package pg.gipter.toolkit;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.toolkit.helper.ListViewId;
import pg.gipter.toolkit.sharepoint.SharePointClient;
import pg.gipter.toolkit.sharepoint.SharePointConfiguration;

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
        setToolkitProperties(applicationContext.getEnvironment());
        applicationContext.register(SharePointConfiguration.class);
        applicationContext.refresh();
        return applicationContext;
    }

    private void setToolkitProperties(ConfigurableEnvironment environment) {
        Properties toolkitProperties = new Properties();
        toolkitProperties.put("toolkit.username", applicationProperties.toolkitUsername());
        toolkitProperties.put("toolkit.password", applicationProperties.toolkitPassword());
        toolkitProperties.put("toolkit.domain", applicationProperties.toolkitDomain());
        environment.getPropertySources().addLast(new PropertiesPropertySource("toolkit", toolkitProperties));
    }

    public void uploadDiff() {
        try {
            SharePointClient sharePointClient = springContext.getBean(SharePointClient.class);
            //System.out.println("Getting list from SharePoint.");
            //sharePointClient.getList();
            System.out.println("Getting list and view from SharePoint.");
            ListViewId ids = sharePointClient.getListAndView();
            String listItemId = sharePointClient.updateListItems(ids);
            sharePointClient.addAttachment(listItemId, applicationProperties.fileName(), applicationProperties.itemPath());
        } catch (Exception ex) {
            System.out.println("Error during upload diff.");
            ex.printStackTrace();
        }
    }
}
