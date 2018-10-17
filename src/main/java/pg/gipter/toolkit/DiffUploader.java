package pg.gipter.toolkit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.ws.soap.client.SoapFaultClientException;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.toolkit.helper.ListViewId;
import pg.gipter.toolkit.helper.XmlHelper;
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
        toolkitProperties.put("toolkit.WSUrl", applicationProperties.toolkitWSUrl());
        toolkitProperties.put("toolkit.listName", applicationProperties.toolkitListName());
        environment.getPropertySources().addLast(new PropertiesPropertySource("toolkit", toolkitProperties));
    }

    public void uploadDiff() {
        try {
            SharePointSoapClient sharePointSoapClient = springContext.getBean(SharePointSoapClient.class);
            logger.info("Getting list and view from SharePoint.");
            ListViewId listViewId = sharePointSoapClient.getListAndView();

            String fileName = applicationProperties.fileName();
            String title = fileName.substring(0, fileName.indexOf("."));
            String listItemId = sharePointSoapClient.updateListItems(
                    listViewId,
                    title,
                    applicationProperties.toolkitUserEmail(),
                    applicationProperties.toolkitUserFolder()
            );

            sharePointSoapClient.addAttachment(listItemId, fileName, applicationProperties.itemPath());
        } catch (Exception ex) {
            if (ex instanceof SoapFaultClientException) {
                SoapFaultClientException sfce = (SoapFaultClientException) ex;
                String errorMsg = XmlHelper.extractErrorMessage(sfce.getSoapFault().getSource());
                logger.error("Error during upload diff. {}", errorMsg);
            } else {
                logger.error("Error during upload diff.", ex);
            }
            throw new RuntimeException(ex);
        }
    }
}
