package pg.gipter.jobs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;

import static org.assertj.core.api.Assertions.assertThat;

class UpgradeJobCreatorTest {

    private UpgradeJobCreator upgradeJobCreator;

    @BeforeEach
    void setUp() {
        upgradeJobCreator = new UpgradeJobCreator();
    }

    @Test
    void whenCreate_thenReturnJobDetail() {
        JobDetail jobDetail = upgradeJobCreator.create();

        assertThat(jobDetail.getJobClass().getName()).isEqualTo(UpgradeJob.class.getName());
        assertThat(jobDetail.getKey().getName()).isEqualTo(UpgradeJob.NAME);
        assertThat(jobDetail.getKey().getGroup()).isEqualTo(UpgradeJob.GROUP);
    }

    @Test
    void whenCreateTrigger_thenReturnProperTrigger() throws Exception {
        upgradeJobCreator.createTrigger();

        Trigger trigger = upgradeJobCreator.getTrigger();

        assertThat(trigger.getKey()).isEqualTo(UpgradeJobCreator.UPGRADE_TRIGGER_KEY);
        assertThat(trigger.getKey().getName()).isEqualTo(UpgradeJobCreator.UPGRADE_TRIGGER_KEY.getName());
        assertThat(trigger.getKey().getGroup()).isEqualTo(UpgradeJobCreator.UPGRADE_TRIGGER_KEY.getGroup());
        assertThat(trigger.getScheduleBuilder()).isInstanceOf(CronScheduleBuilder.class);
        assertThat(trigger.getScheduleBuilder()).isInstanceOf(CronScheduleBuilder.class);
        assertThat(trigger.getStartTime()).isNotNull();
    }

    @Test
    void whenGetJobKey_thenReturnJobKey() {
        JobKey jobKey = upgradeJobCreator.getJobKey();

        assertThat(jobKey.getName()).isEqualTo(UpgradeJob.NAME);
        assertThat(jobKey.getGroup()).isEqualTo(UpgradeJob.GROUP);
    }

    @Test
    void whenGetTriggerKey_thenReturnProperTriggerKey() {
        assertThat(upgradeJobCreator.getTriggerKey()).isEqualTo(UpgradeJobCreator.UPGRADE_TRIGGER_KEY);
    }
}