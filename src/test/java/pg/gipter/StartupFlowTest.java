package pg.gipter;

import org.junit.jupiter.api.Test;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.model.RunConfig;
import pg.gipter.services.CookiesService;
import pg.gipter.services.ToolkitService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class StartupFlowTest {
    private final ApplicationProperties properties = mock(ApplicationProperties.class);

    @Test
    void freshUiRequiresConfigurationEvenWhenSignInWasRequested() {
        when(properties.isUseUI()).thenReturn(true);
        try (var cookies = mockStatic(CookiesService.class)) {
            assertThat(Main.startupFlow(properties, new String[]{"flowType=INIT"})).isEqualTo(FlowType.CONFIGURE);
            cookies.verifyNoInteractions();
        }
    }

    @Test
    void finishedSetupSignsInWithTheConfiguredAccount() {
        when(properties.isUseUI()).thenReturn(true);
        when(properties.getRunConfigMap()).thenReturn(Map.of("first", new RunConfig()));
        when(properties.toolkitUserFolderUrl()).thenReturn("https://example.com/users/employee");
        when(properties.toolkitHostUrl()).thenReturn("https://example.com");
        when(properties.toolkitCopyCase()).thenReturn("/cases/test");
        when(properties.toolkitUserEmail()).thenReturn("employee@example.com");
        try (var cookies = mockStatic(CookiesService.class)) {
            cookies.when(() -> CookiesService.configure(properties)).thenCallRealMethod();
            assertThat(Main.startupFlow(properties, new String[0])).isEqualTo(FlowType.INIT);
            cookies.verify(() -> CookiesService.configure("https://example.com/users/employee",
                    "https://example.com/cases/test/_api/web/currentuser", "employee@example.com"));
        }
    }

    @Test
    void configuredUiReusesAWorkingSession() {
        when(properties.isUseUI()).thenReturn(true);
        when(properties.getRunConfigMap()).thenReturn(Map.of("first", new RunConfig()));
        try (var cookies = mockStatic(CookiesService.class);
             var toolkit = mockConstruction(ToolkitService.class, (service, context) ->
                     when(service.isCookieWorking()).thenReturn(true));
             var settings = mockStatic(ProgramSettings.class)) {
            cookies.when(CookiesService::isCookiesFileExist).thenReturn(true);
            assertThat(Main.startupFlow(properties, new String[0])).isEqualTo(FlowType.REGULAR);
            assertThat(toolkit.constructed()).hasSize(1);
            settings.verify(() -> ProgramSettings.initProgramSettings(Environment.PROD));
        }
    }

    @Test
    void commandLineWithoutSavedConfigurationsCanSignInDirectly() {
        when(properties.isUseUI()).thenReturn(false);
        try (var cookies = mockStatic(CookiesService.class)) {
            assertThat(Main.startupFlow(properties, new String[0])).isEqualTo(FlowType.INIT);
            cookies.verify(() -> CookiesService.configure(properties));
        }
    }

    @Test
    void disabledSsoSkipsTheSessionCheckAfterSetup() {
        when(properties.isUseUI()).thenReturn(true);
        when(properties.getRunConfigMap()).thenReturn(Map.of("first", new RunConfig()));
        when(properties.isNoSSO()).thenReturn(true);
        try (var cookies = mockStatic(CookiesService.class);
             var settings = mockStatic(ProgramSettings.class)) {
            assertThat(Main.startupFlow(properties, new String[]{"flowType=INIT"})).isEqualTo(FlowType.NO_UPLOAD);
            cookies.verify(CookiesService::isCookiesFileExist, never());
            settings.verify(() -> ProgramSettings.initProgramSettings(Environment.PROD));
        }
    }
}
