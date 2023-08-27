package pg.gipter.converters;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.*;
import pg.gipter.core.dao.DaoConstants;
import pg.gipter.core.dao.DaoFactory;
import pg.gipter.core.model.CipherDetails;
import pg.gipter.core.model.ToolkitConfig;
import pg.gipter.services.SecurityService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class SSOPasswordConverterTest {

    private final static Path A_P = Paths.get(DaoConstants.APPLICATION_PROPERTIES_JSON);
    private SSOPasswordConverter converter;

    @BeforeEach
    void setUp() {
        converter = new SSOPasswordConverter();
    }

    @AfterEach
    void tearDown() {
        try {
            Files.deleteIfExists(A_P);
            DaoFactory.getCachedConfiguration().resetCache();
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private void mockApplicationProperties(String json) throws IOException {
        File ap = A_P.toFile();
        try (FileWriter fw = new FileWriter(ap, StandardCharsets.UTF_8)) {
            fw.write(json);
        }
    }

    @Test
    void givenNoToolkitPassword_whenConvert_thenToolkitSSOPasswordIsEmpty() throws IOException {
        mockApplicationProperties("{ \"toolkitConfig\": {}}");

        converter.convert();

        ToolkitConfig actual = DaoFactory.getCachedConfiguration().loadToolkitConfig();
        assertThat(actual.getToolkitSSOPassword()).isEmpty();
    }

    @Test
    void givenEmptyToolkitPassword_whenConvert_thenToolkitSSOPasswordIsEmpty() throws IOException {
        mockApplicationProperties("{ \"toolkitConfig\": {\"toolkitPassword\": \"\"}}");

        converter.convert();

        ToolkitConfig actual = DaoFactory.getCachedConfiguration().loadToolkitConfig();
        assertThat(actual.getToolkitSSOPassword()).isEmpty();
    }

    @Test
    void name() throws IOException {

        SecurityService securityService = SecurityService.getInstance();
        CipherDetails generatedCipher = securityService.generateCipherDetails();
        String encryptedPass = securityService.encrypt("test", generatedCipher);
        Gson gson = new GsonBuilder().create();
        String json = "{\"cipherDetails\": "+ gson.toJson(generatedCipher) + ", \"toolkitConfig\": {\"toolkitPassword\": \"" + encryptedPass + "\"}}";
        mockApplicationProperties(json);

        converter.convert();
        ToolkitConfig actual = DaoFactory.getCachedConfiguration().loadToolkitConfig();
        assertThat(actual.getToolkitSSOPassword()).isEqualTo(encryptedPass);
    }

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Test
    void namea() {
        Future<?> future = executorService.submit(() -> {
            try {
                extracted();
                fail("It should time out.");
            } catch (InterruptedException ex) {
                System.err.println("Here 1");
                fail("Should neve get here");
            }
        });

        try {
            future.get(50, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            System.err.printf("Here 2 %s", ex.getMessage());
            fail("Should time out");
        } catch (ExecutionException e) {
            System.err.printf("Here 3 %s", e.getMessage());
            fail("Should time out");
        } catch (TimeoutException e) {
            System.err.println("Here 4 ");
        }
        assertThat(future.isDone()).isFalse();
        assertThat(future.isCancelled()).isFalse();
    }

    private static void extracted() throws InterruptedException {
        Random random = new Random();
        List<Integer> integers = Stream.generate(() -> random.nextInt(1000))
                .limit(1_000_000)
                .collect(Collectors.toList());
        integers = integers.stream().map(it -> it + 1).collect(Collectors.toList());
    }
}