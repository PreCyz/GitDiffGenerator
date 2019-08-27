package pg.gipter.statistic.dao;

import org.junit.jupiter.api.Test;
import pg.gipter.statistic.dto.Statistics;
import pg.gipter.ui.UploadStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class UserDaoImplTest {

    @Test
    void updateUserStatistics() throws Exception {
        UserDaoImpl userDao = new UserDaoImpl();
        assertThat(userDao.isStatisticsAvailable()).isTrue();

        Statistics user = new Statistics();
        user.setUsername("pawg");
        user.setFirstExecutionDate(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        user.setLastExecutionDate(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        user.setJavaVersion(System.getProperty("java.version"));
        user.setLastUpdateStatus(UploadStatus.N_A);

        System.out.printf("Runtime version %s%n", Runtime.class.getPackage().getImplementationVersion());
        System.out.printf("System property %s%n", System.getProperty("java.version"));

        userDao.updateUserStatistics(user);

        System.out.println(System.getProperty("java.home"));
    }
}