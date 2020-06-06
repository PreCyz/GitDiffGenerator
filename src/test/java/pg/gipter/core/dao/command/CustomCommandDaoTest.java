package pg.gipter.core.dao.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.dao.configuration.ConfigurationDaoFactory;
import pg.gipter.core.producers.command.VersionControlSystem;
import pg.gipter.jobs.upload.json.LocalDateTimeAdapter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CustomCommandDaoTest {

    @AfterEach
    private void teardown() {
        ConfigurationDaoFactory.getCachedConfigurationDao().resetCache();
        try {
            Files.deleteIfExists(Paths.get(DaoConstants.CUSTOM_COMMAND_JSON));
        } catch (IOException e) {
            System.err.println("There is something weird going on.");
        }
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
    void givenExistingFile_whenReadCustomCommand_thenReturnCustomCommandObject() {
        CustomCommand customCommand = new CustomCommand();
        customCommand.setVcs(VersionControlSystem.SVN);
        customCommand.setCommand("git log");

        writeToFile(customCommand);

        final Optional<CustomCommand> actual = CustomCommandDao.readCustomCommand();

        assertThat(actual.isPresent()).isTrue();
    }

    @Test
    void givenNotExistingFile_whenReadCustomCommand_thenReturnEmptyOptional() {
        CustomCommand customCommand = new CustomCommand();
        customCommand.setVcs(VersionControlSystem.SVN);
        customCommand.setCommand("git log");

        final Optional<CustomCommand> actual = CustomCommandDao.readCustomCommand();

        assertThat(actual.isPresent()).isFalse();
    }
}