package pg.gipter.core.producers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.ApplicationPropertiesFactory;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.dao.command.CustomCommand;
import pg.gipter.core.dao.configuration.ConfigurationDaoFactory;
import pg.gipter.core.producers.command.DiffCommand;
import pg.gipter.core.producers.command.VersionControlSystem;
import pg.gipter.services.SecurityService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class WindowsDiffProducerTest {

    @AfterEach
    void teardown() {
        ConfigurationDaoFactory.getCachedConfigurationDao().resetCache();
        try {
            Files.deleteIfExists(Paths.get(DaoConstants.CUSTOM_COMMAND_JSON));
        } catch (IOException e) {
            System.err.println("There is something weird going on.");
        }
    }

    @Test
    void given_listOfCommands_when_getFullCommand_then_returnFullCommand() {
        WindowsDiffProducer producer = new WindowsDiffProducer(
                ApplicationPropertiesFactory.getInstance(new String[]{"preferredArgSource=FILE"})
        );

        List<String> actual = producer.getFullCommand(Arrays.asList("c", "\"c2\"", "c3"));

        assertThat(actual).containsExactly("c", "\"c2\"", "c3");
    }

    @Test
    void givenCustomCommand_whenCalculateCommand_thenUseCustomCommand() {
        SecurityService.getInstance().writeCipherDetails(SecurityService.getInstance().generateCipherDetails());
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(
                new String[]{"preferredArgSource=FILE"}
        );
        WindowsDiffProducer producer = new WindowsDiffProducer(applicationProperties);
        CustomCommand customCommand = new CustomCommand(VersionControlSystem.GIT, "git log", true);
        applicationProperties.addCustomCommand(customCommand);
        applicationProperties.save();
        final DiffCommand diffCommandMock = mock(DiffCommand.class);

        final List<String> actual = producer.calculateCommand(diffCommandMock, VersionControlSystem.GIT);

        assertThat(actual).containsExactly("git", "log");
        verifyNoInteractions(diffCommandMock);
    }

    @Test
    void givenNoCustomCommand_whenCalculateCommand_thenUseDiffCommand() {
        WindowsDiffProducer producer = new WindowsDiffProducer(
                ApplicationPropertiesFactory.getInstance(new String[]{"preferredArgSource=FILE"})
        );
        final DiffCommand diffCommandMock = mock(DiffCommand.class);
        when(diffCommandMock.commandAsList()).thenReturn(Arrays.asList("svn", "up"));

        final List<String> actual = producer.calculateCommand(diffCommandMock, VersionControlSystem.GIT);

        assertThat(actual).containsExactly("svn", "up");
        verify(diffCommandMock, times(1)).commandAsList();
    }
}