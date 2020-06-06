package pg.gipter.core.producers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import pg.gipter.core.ApplicationPropertiesFactory;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.dao.command.CustomCommand;
import pg.gipter.core.dao.configuration.ConfigurationDaoFactory;
import pg.gipter.core.producers.command.DiffCommand;
import pg.gipter.core.producers.command.VersionControlSystem;
import pg.gipter.jobs.upload.json.LocalDateTimeAdapter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class WindowsDiffProducerTest {

    @AfterEach
    private void teardown() {
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

    private void writeToFile(CustomCommand customCommand) {
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter().nullSafe())
                .create();
        String json = gson.toJson(customCommand, CustomCommand.class);
        try (OutputStream os = new FileOutputStream(DaoConstants.CUSTOM_COMMAND_JSON);
             Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)
        ) {
            writer.write(json);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error when writing custom command into json.");
        }
    }

    @Test
    void givenCustomCommand_whenCalculateCommand_thenUseCustomCommand() {
        WindowsDiffProducer producer = new WindowsDiffProducer(
                ApplicationPropertiesFactory.getInstance(new String[]{"preferredArgSource=FILE"})
        );
        CustomCommand customCommand = new CustomCommand();
        customCommand.setVcs(VersionControlSystem.GIT);
        customCommand.setCommand("git log");
        writeToFile(customCommand);
        final DiffCommand diffCommandMock = mock(DiffCommand.class);

        final List<String> actual = producer.calculateCommand(diffCommandMock, VersionControlSystem.GIT);

        assertThat(actual).containsExactly("git", "log");
        verifyZeroInteractions(diffCommandMock);
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