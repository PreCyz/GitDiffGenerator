package pg.gipter.toolkit.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**Created by Pawel Gawedzki on 12-Oct-2018.*/
@Configuration
public class CredentialsConfig {

    @Bean
    public NetworkCredentials networkCredentials() {
        return new NetworkCredentials();
    }
}
