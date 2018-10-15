package pg.gipter.toolkit.sharepoint;

import org.apache.http.auth.NTCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

/**Created by Pawel Gawedzki on 12-Oct-2018.*/
@Configuration
public class SharePointConfiguration {

    @Value("${toolkit.username}")
    private String username;
    @Value("${toolkit.password}")
    private String password;
    @Value("${toolkit.domain}")
    private String domain;

    @Bean
    public HttpComponentsMessageSender httpComponentsMessageSender() {
        HttpComponentsMessageSender httpComponentsMessageSender = new HttpComponentsMessageSender();
        NTCredentials credentials = new NTCredentials(username, password, null, domain);
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
    public SharePointSoapClient sharePointClient(WebServiceMessageSender messageSender) {
        return new SharePointSoapClient(webServiceTemplate(marshaller(), messageSender));
    }

}
