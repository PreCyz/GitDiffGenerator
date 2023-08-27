package pg.gipter.core.dao.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.dao.configuration.ConfigurationDaoFactory;
import pg.gipter.core.producers.command.VersionControlSystem;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CustomCommandDaoTest {

    @AfterEach
    void teardown() {
        ConfigurationDaoFactory.getCachedConfigurationDao().resetCache();
        try {
            Files.deleteIfExists(Paths.get(DaoConstants.CUSTOM_COMMAND_JSON));
        } catch (IOException e) {
            System.err.println("There is something weird going on.");
        }
    }

    private void writeToFile(CustomCommand customCommand) {
        final Gson gson = new GsonBuilder()
                .setPrettyPrinting()
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
        customCommand.setCommandList(Arrays.asList("git","log"));

        writeToFile(customCommand);

        final Optional<CustomCommand> actual = CustomCommandDao.readCustomCommand();

        assertThat(actual.isPresent()).isTrue();
    }

    @Test
    void givenNotExistingFile_whenReadCustomCommand_thenReturnEmptyOptional() {
        final Optional<CustomCommand> actual = CustomCommandDao.readCustomCommand();

        assertThat(actual.isPresent()).isFalse();
    }

    @Test
    void name() {
        System.out.println(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        System.out.println(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}