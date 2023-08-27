package pg.gipter.services;

import javafx.stage.Stage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import pg.gipter.core.ApplicationPropertiesFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({ApplicationExtension.class})
@Disabled
class CookiesServiceTest {

    private CookiesService cookiesService;

    @Start
    public void start(Stage stage) {
        cookiesService = new CookiesService(ApplicationPropertiesFactory.getInstance(new String[]{"toolkitUsername=PAWG"}));
    }

    @Test
    void name(FxRobot robot) {
        cookiesService.getFedAuthString();
        assertTrue(true);
    }

    @Test
    void fedAuthExpire() {
        LocalDateTime expire = LocalDateTime.ofInstant(Instant.ofEpochMilli(1691347122000L),
                TimeZone.getDefault().toZoneId());
        assertThat(expire.isBefore(LocalDateTime.now())).isFalse();
        System.out.printf(expire.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    @Test
    void getFedAuth() {
        String fedAuth = cookiesService.getFedAuthString();

        assertThat(fedAuth).isEqualTo("aaaa");
    }
}