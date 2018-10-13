package pg.gipter.toolkit.configuration;

import org.apache.http.auth.NTCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

/**Created by Pawel Gawedzki on 12-Oct-2018.*/
@Configuration
public class SharePointConfiguration {

    @Autowired
    private Environment environment;

    private String username() {
        return environment.getProperty("toolkit.username");
    }

    private String password() {
        return environment.getProperty("toolkit.password");
    }

    private String domain() {
        return environment.getProperty("toolkit.domain");
    }

    @Bean
    public HttpComponentsMessageSender httpComponentsMessageSender() {
        HttpComponentsMessageSender httpComponentsMessageSender = new HttpComponentsMessageSender();
        NTCredentials credentials = new NTCredentials(username(), password(), null, domain());
        httpComponentsMessageSender.setCredentials(credentials);
        return httpComponentsMessageSender;
    }

    @Bean
    Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setPackagesToScan("pg.gipter.toolkit.ws");
        return marshaller;
    }

    @Bean
    WebServiceTemplate webServiceTemplate(Jaxb2Marshaller marshaller, WebServiceMessageSender messageSender) {
        WebServiceTemplate webServiceTemplate = new WebServiceTemplate(marshaller, marshaller);
        webServiceTemplate.setMessageSender(messageSender);
        return webServiceTemplate;
    }

    @Bean
    public SharePointClient sharePointClient(WebServiceMessageSender messageSender) {
        return new SharePointClient(webServiceTemplate(marshaller(), messageSender));
    }

}
