package pg.gipter.toolkit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.toolkit.helper.ListViewId;
import pg.gipter.toolkit.sharepoint.SharePointConfiguration;
import pg.gipter.toolkit.sharepoint.SharePointSoapClient;

import java.util.Properties;

/**Created by Pawel Gawedzki on 11-Oct-2018.*/
public class DiffUploader {

    private static final Logger logger = LoggerFactory.getLogger(DiffUploader.class);

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
        toolkitProperties.put("toolkit.url", applicationProperties.toolkitUrl());
        toolkitProperties.put("toolkit.listName", applicationProperties.toolkitListName());
        environment.getPropertySources().addLast(new PropertiesPropertySource("toolkit", toolkitProperties));
    }

    public void uploadDiff() {
        try {
            SharePointSoapClient sharePointSoapClient = springContext.getBean(SharePointSoapClient.class);
            //System.out.println("Getting list from SharePoint.");
            //sharePointClient.getList();
            logger.info("Getting list and view from SharePoint.");
            ListViewId ids = sharePointSoapClient.getListAndView();
            String listItemId = sharePointSoapClient.updateListItems(ids, applicationProperties.fileName(), applicationProperties.toolkitUsername());
            sharePointSoapClient.addAttachment(listItemId, applicationProperties.fileName(), applicationProperties.itemPath());
        } catch (Exception ex) {
            logger.error("Error during upload diff.", ex);
        }
    }
}
