package pg.gipter.toolkit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.ws.soap.client.SoapFaultClientException;
import pg.gipter.producer.command.UploadType;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.toolkit.helper.ListViewId;
import pg.gipter.toolkit.helper.XmlHelper;
import pg.gipter.toolkit.sharepoint.soap.SharePointConfiguration;
import pg.gipter.toolkit.sharepoint.soap.SharePointSoapClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.util.stream.Collectors.joining;

/**Created by Pawel Gawedzki on 11-Oct-2018.*/
public class DiffUploader {

    private static final Logger logger = LoggerFactory.getLogger(DiffUploader.class);

    private ApplicationContext springContext;
    private ApplicationProperties applicationProperties;

    /**For test purposes only*/
    DiffUploader() { }

    public DiffUploader(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.springContext = initSpringApplicationContext();
    }

    /**For test purposes only*/
    void setSpringContext(ApplicationContext springContext) {
        this.springContext = springContext;
    }

    ApplicationContext initSpringApplicationContext() {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        setToolkitProperties(applicationContext.getEnvironment());
        applicationContext.register(SharePointConfiguration.class);
        applicationContext.refresh();
        return applicationContext;
    }

    void setToolkitProperties(ConfigurableEnvironment environment) {
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
            String listItemId = sharePointSoapClient.updateListItems(listViewId, buildAttributesMap(listViewId.viewId()));
            sharePointSoapClient.addAttachment(listItemId, applicationProperties.fileName(), applicationProperties.itemPath());
        } catch (SoapFaultClientException ex) {
            String wsMsg = XmlHelper.extractErrorMessage(ex.getSoapFault().getSource());
            String errorMsg = String.format("Error during upload diff. %s", wsMsg);
            logger.error(errorMsg, ex);
            throw new RuntimeException(errorMsg, ex);
        } catch (Exception ex) {
            String errorMsg = String.format("Error during upload diff. %s", ex.getMessage());
            logger.error(errorMsg, ex);
            throw new RuntimeException(errorMsg, ex);
        }
    }

    Map<String, String> buildAttributesMap(String viewName) {
        LocalDate endDate = applicationProperties.endDate();
        String fileName = applicationProperties.fileName();
        String title = fileName.substring(0, fileName.indexOf("."));
        String allVcs = applicationProperties.vcsSet().stream().map(Enum::name).collect(joining(","));
        String description = String.format("%s diff file.", allVcs);
        if (applicationProperties.uploadType() == UploadType.STATEMENT) {
            description = String.format("%s file.", UploadType.STATEMENT);
        }
        LocalDateTime submissionDate = LocalDateTime.of(endDate, LocalTime.now());

        Map<String, String> attributes = new HashMap<>();
        attributes.put("Title", title);
        attributes.put("Employee", "-1;#" + applicationProperties.toolkitUsername());
        attributes.put("SubmissionDate", submissionDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")));
        attributes.put("Classification", "12;#Changeset (repository change report)");
        attributes.put("Body", description);

        attributes.put("ViewName", viewName);
        attributes.put("RootFolder", applicationProperties.toolkitUserFolder());
        attributes.put("Cmd", "New");

        return attributes;
    }
}
