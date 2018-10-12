package pg.gipter.toolkit;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.toolkit.configuration.SharePointClient;
import pg.gipter.toolkit.configuration.SharePointConfiguration;

/**Created by Pawel Gawedzki on 11-Oct-2018.*/
public class DiffUploader {

    private final ToolkitClient toolkitClient;
    private final ApplicationContext springContext;
    private final ApplicationProperties applicationProperties;

    public DiffUploader(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.toolkitClient = new ToolkitClient();
        this.springContext = initSpringApplicationContext();
    }

    private ApplicationContext initSpringApplicationContext() {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

        /*applicationContext.refresh();
        NetworkCredentials bean = applicationContext.getBean(CredentialsConfig.class).networkCredentials();
        bean.setUsername("PAWG");
        bean.setPassword("");
        bean.setDomain("NCDMZ");*/
        applicationContext.register(SharePointConfiguration.class);
        applicationContext.refresh();
        return applicationContext;
    }

    public void uploadDiff() {
        try {
            SharePointClient sharePointClient = springContext.getBean(SharePointClient.class);
            sharePointClient.getListAndView();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
