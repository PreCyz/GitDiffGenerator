package pg.gipter.converters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import pg.gipter.MockitoExtension;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.ApplicationPropertiesFactory;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.dao.command.CustomCommand;
import pg.gipter.core.dao.configuration.ConfigurationDaoFactory;
import pg.gipter.core.model.CipherDetails;
import pg.gipter.core.producers.command.VersionControlSystem;
import pg.gipter.jobs.json.LocalDateTimeAdapter;
import pg.gipter.services.SecurityService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

@ExtendWith({MockitoExtension.class})
class CustomCommandConverterTest {

    private CustomCommandConverter converter;

    @AfterEach
    void teardown() {
        ConfigurationDaoFactory.getCachedConfigurationDao().resetCache();
        try {
            Files.deleteIfExists(Paths.get(DaoConstants.APPLICATION_PROPERTIES_JSON));
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
    void givenNoCustomCommand_whenConvert_thenReturnFalse() {
        converter = spy(new CustomCommandConverter(ApplicationPropertiesFactory.getInstance(new String[]{})));

        assertThat(converter.convert()).isFalse();
    }

    @Test
    void givenGITCustomCommand_whenConvert_thenReturnTrue() {
        CipherDetails generatedCipher = SecurityService.getInstance().generateCipherDetails();
        SecurityService.getInstance().writeCipherDetails(generatedCipher);
        ApplicationProperties applicationProperties = ApplicationPropertiesFactory.getInstance(new String[]{});
        CustomCommand customCommand = new CustomCommand(VersionControlSystem.GIT);
        customCommand.setCommand("git log");
        customCommand.setCommandList(Arrays.asList("git", "log"));
        writeToFile(customCommand);

        converter = spy(new CustomCommandConverter(applicationProperties));

        assertThat(converter.convert()).isTrue();
        CustomCommand actual = applicationProperties.getCustomCommand(VersionControlSystem.GIT);
        assertThat(actual).isNotNull();
        assertThat(actual.getVcs()).isEqualTo(VersionControlSystem.GIT);
        assertThat(actual.getCommand()).isEqualTo("git log");
        assertThat(actual.getCommandList()).containsExactly("git", "log");
        assertThat(actual.isOverride()).isTrue();
        assertThat(Files.notExists(Paths.get(DaoConstants.CUSTOM_COMMAND_JSON))).isTrue();
    }
}