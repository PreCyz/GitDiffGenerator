package pg.gipter.services;

import javafx.stage.Stage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({ApplicationExtension.class})
@Disabled
class CookiesServiceTest {

    @Start
    public void start(Stage stage) {
    }

    @Test
    void name(FxRobot robot) {
        CookiesService.getFedAuthString();
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
        assertThat(CookiesService.getFedAuthString()).isEqualTo("aaaa");
    }

    @Test
    void name() {
        ZoneId zoneId = TimeZone.getTimeZone("GMT").toZoneId();
        LocalDateTime creationTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(1723652299527L), zoneId);
        LocalDateTime expiryTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(1723652423000L), zoneId);
        LocalDateTime lastAccessTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(1723652305722L), zoneId);

        System.out.printf("creationTime: %s%n", creationTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        System.out.printf("expiryTime: %s%n", expiryTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        System.out.printf("lastAccessTime: %s%n", lastAccessTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        LocalDateTime gc = LocalDateTime.ofInstant(Instant.ofEpochMilli(1723652305722L), TimeZone.getTimeZone("GMT").toZoneId());
        System.out.printf("gc: %s%n", gc.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
}