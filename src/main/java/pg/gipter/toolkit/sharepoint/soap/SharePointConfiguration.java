package pg.gipter.toolkit.sharepoint.soap;

import org.apache.http.auth.NTCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

/** Created by Pawel Gawedzki on 12-Oct-2018. */
public class SharePointConfiguration {

    @Bean
    public HttpComponentsMessageSender httpComponentsMessageSender(@Value("${toolkit.username}") String username,
                                                                   @Value("${toolkit.password}") String password,
                                                                   @Value("${toolkit.domain}") String domain) {
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
    public SharePointSoapClient sharePointSoapClient(@Value("${toolkit.WSUrl}") String wsUrl,
                                                     @Value("${toolkit.copyListName}") String copyListName,
                                                     WebServiceTemplate webServiceTemplate) {
        return new SharePointSoapClient(webServiceTemplate, wsUrl, copyListName);
    }

}
